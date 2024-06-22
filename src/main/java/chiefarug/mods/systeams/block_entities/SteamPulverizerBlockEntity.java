package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.SteamPulverizerContainer;
import cofh.lib.api.StorageGroup;
import cofh.lib.client.sounds.ConditionalSoundInstance;
import cofh.lib.inventory.ItemStorageCoFH;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.core.item.SlotSealItem;
import cofh.thermal.core.util.managers.machine.PulverizerRecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static cofh.core.util.helpers.AugmentableHelper.getAttributeMod;
import static cofh.core.util.helpers.ItemHelper.itemsEqualWithTags;
import static cofh.lib.api.StorageGroup.CATALYST;
import static cofh.lib.api.StorageGroup.INPUT;
import static cofh.lib.api.StorageGroup.OUTPUT;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_FEATURE_CYCLE_PROCESS;
import static cofh.thermal.expansion.init.TExpSounds.SOUND_MACHINE_PULVERIZER;

public class SteamPulverizerBlockEntity extends SteamMachineBlockEntity {
    public SteamPulverizerBlockEntity(BlockPos pos, BlockState state) {
        super(SysteamsRegistry.SteamMachines.PULVERIZER.blockEntity(), pos, state);

        inventory.addSlot(inputSlot, INPUT);
        inventory.addSlot(catalystSlot, CATALYST);
        inventory.addSlots(OUTPUT, 4);
        inventory.addSlot(fillSteamSlot, StorageGroup.INTERNAL);
        // half the aug slots! we movin slower than a snail.
        addAugmentSlots(ThermalCoreConfig.machineAugments / 2);
        initHandlers();
    }

    protected ItemStorageCoFH inputSlot = new ItemStorageCoFH(item -> filter.valid(item) && PulverizerRecipeManager.instance().validRecipe(item));
    protected ItemStorageCoFH catalystSlot = new ItemStorageCoFH(item -> item.getItem() instanceof SlotSealItem || PulverizerRecipeManager.instance().validCatalyst(item));

    @Override
    protected int getBaseProcessTick() {
        return PulverizerRecipeManager.instance().getBasePower();
    }

    @Override
    protected boolean cacheRecipe() {
        curRecipe = PulverizerRecipeManager.instance().getRecipe(this);
        curCatalyst = PulverizerRecipeManager.instance().getCatalyst(catalystSlot);
        if (curRecipe != null) {
            itemInputCounts = curRecipe.getInputItemCounts(this);
        }
            return curRecipe != null;
    }

    @Override
    protected void resolveInputs() {
        inputSlot.modify(-itemInputCounts.get(0));

        if (cyclicProcessingFeature && !catalystSlot.isEmpty() && !catalystSlot.isFull()) {
            ItemStack catalyst = catalystSlot.getItemStack();
            outputSlots().stream()
                    .filter(slot -> itemsEqualWithTags(slot.getItemStack(), catalyst))
                    .findFirst().ifPresent(slot -> {
                        slot.modify(-1);
                        catalystSlot.modify(1);
                    });
        }
        int decrement = itemInputCounts.size() > 1 ? itemInputCounts.get(1) : 0;
        if (decrement > 0) {
            if (catalystSlot.getItemStack().isDamageableItem()) {
                if (catalystSlot.getItemStack().hurt(decrement, MathHelper.RANDOM, null)) {
                    catalystSlot.modify(-1);
                }
            } else {
                catalystSlot.modify(-decrement);
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        assert this.level != null;
        return new SteamPulverizerContainer(i, this.level, this.getBlockPos(), player.getInventory(), player);
    }


    @Override
    protected Object getSound() {
        return new ConditionalSoundInstance(SOUND_MACHINE_PULVERIZER.get(), SoundSource.AMBIENT, this, () -> !remove && isActive);
    }

    // region OPTIMIZATION
    @Override
    protected boolean validateInputs() {
        if (!cacheRecipe()) {
            return false;
        }
        return inputSlot.getCount() >= itemInputCounts.get(0);
//        return !cacheRecipe() && inputSlot.getCount() >= itemInputCounts.get(0);
    }
    // endregion

    // region AUGMENTS
    protected boolean cyclicProcessingFeature = false;

    @Override
    protected void resetAttributes() {
        super.resetAttributes();

        cyclicProcessingFeature = false;
    }

    @Override
    protected void setAttributesFromAugment(CompoundTag augmentData) {
        super.setAttributesFromAugment(augmentData);

        cyclicProcessingFeature |= getAttributeMod(augmentData, TAG_AUGMENT_FEATURE_CYCLE_PROCESS) > 0;
    }
    // endregion
}
