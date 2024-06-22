package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.core.util.helpers.AugmentDataHelper;
import cofh.lib.api.StorageGroup;
import cofh.lib.energy.EmptyEnergyStorage;
import cofh.lib.fluid.FluidStorageCoFH;
import cofh.lib.inventory.ItemStorageCoFH;
import cofh.lib.util.Constants;
import cofh.lib.util.Utils;
import cofh.thermal.lib.block.entity.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.function.Predicate;

public abstract class SteamMachineBlockEntity extends MachineBlockEntity {

    protected final FluidStorageCoFH steamStorage;
    protected ItemStorageCoFH fillSteamSlot = new ItemStorageCoFH(item -> {
        if (item.isEmpty()) return false;
        LazyOptional<IFluidHandlerItem> cap = item.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (!cap.isPresent()) return false;
        IFluidHandlerItem fluidTanks = cap.resolve().get();
        int tanks = fluidTanks.getTanks();
        for (int i = 0;i < tanks; i++) {
            if (SysteamsRegistry.Fluids.IS_STEAM.test(fluidTanks.getFluidInTank(i)))
                return true;
        }
        return false;
    });

    public SteamMachineBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        // pfft who needs energy
        this.energyStorage = EmptyEnergyStorage.INSTANCE;
        // steam is where it is at!
        this.steamStorage = new FluidStorageCoFH(Constants.TANK_MEDIUM, SysteamsRegistry.Fluids.IS_STEAM);
        this.tankInv.addTank(steamStorage, StorageGroup.INPUT);
    }

    @Override
    public void tickServer() {
        boolean curActive = isActive;
        if (isActive) {
            processTick();
            if (canProcessFinish()) {
                processFinish();
                transferOutput();
                transferInput();
                if (!redstoneControl.getState() || !canProcessStart()) {
//                    steamStorage.modify(-process); // this line causes crashes. cool.
                    processOff();
                } else {
                    processStart();
                }
            } else if (steamStorage.getStored() < processTick) {
                processOff();
            }
        } else if (redstoneControl.getState()) {
            if (Utils.timeCheck()) {
                transferOutput();
                transferInput();
            }
            if (Utils.timeCheckQuarter() && canProcessStart()) {
                processStart();
                processTick();
                isActive = true;
            }
        }
        updateActiveState(curActive);
        stealFluidFromInputFluidSlot();
    }

    protected void stealFluidFromInputFluidSlot() {
        if (fillSteamSlot.isEmpty() || steamStorage.isFull()) return;
        fillSteamSlot.getItemStack().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluid -> {
            int tanks = fluid.getTanks();
            for (int i = 0; i < tanks; i++) {
                FluidStack stack = fluid.getFluidInTank(i);
                if (SysteamsRegistry.Fluids.IS_STEAM.test(stack)) {
                    FluidStack drained = fluid.drain(stack, IFluidHandler.FluidAction.SIMULATE);
                    int filled = steamStorage.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                    if (filled == 0) return;

                    FluidStack toDrain = drained.copy();
                    toDrain.setAmount(filled);
                    FluidStack actualDrained = fluid.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                    steamStorage.fill(actualDrained, IFluidHandler.FluidAction.EXECUTE);
                    // this ensures that if the container is replaced (ie it's a bucket) it actually gets replaced
                    fillSteamSlot.setItemStack(fluid.getContainer());
                }
            }
        });
    }

    @Override
    protected Predicate<ItemStack> augValidator() {
        return item -> AugmentDataHelper.hasAugmentData(item) && SysteamsRegistry.STEAM_MACHINE_VALIDATORS.test(item, getAugmentsAsList());
    }

    @Override
    protected boolean canProcessStart() {
        if (steamStorage.getStored() - process < processTick || !validateInputs()) return false;
        return validateOutputs();
    }

    @Override
    protected int processTick() {
        if (process <= 0) return 0;
        steamStorage.modify(-processTick);
        process -= processTick;
        return processTick;
    }

}
