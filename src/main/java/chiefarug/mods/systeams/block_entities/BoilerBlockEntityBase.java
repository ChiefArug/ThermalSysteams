package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.core.network.packet.client.TileStatePacket;
import cofh.core.util.helpers.AugmentDataHelper;
import cofh.core.util.helpers.AugmentableHelper;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.api.StorageGroup;
import cofh.lib.api.block.entity.ITickableTile;
import cofh.lib.api.fluid.IFluidStackHolder;
import cofh.lib.api.inventory.IItemStackHolder;
import cofh.lib.fluid.FluidStorageCoFH;
import cofh.lib.util.Constants;
import cofh.lib.util.Utils;
import cofh.lib.util.constants.NBTTags;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.lib.common.ThermalAugmentRules;
import cofh.thermal.lib.tileentity.ThermalTileAugmentable;
import cofh.thermal.lib.util.managers.IFuelManager;
import cofh.thermal.lib.util.recipes.IThermalInventory;
import cofh.thermal.lib.util.recipes.internal.IDynamoFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static chiefarug.mods.systeams.SysteamsConfig.BASE_STEAM_GENERATION;
import static chiefarug.mods.systeams.SysteamsConfig.WATER_TO_STEAM_RATIO;
import static cofh.lib.util.Constants.AUG_SCALE_MAX;
import static cofh.lib.util.Constants.AUG_SCALE_MIN;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_ENERGY;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_POWER;

public abstract class BoilerBlockEntityBase extends ThermalTileAugmentable implements ITickableTile.IServerTickable, IThermalInventory {

	private final Predicate<FluidStack> isWater = fluid -> filter.valid(fluid) && SysteamsRegistry.Fluids.WATER.contains(fluid.getFluid());
	public final FluidStorageCoFH waterTank = new FluidStorageCoFH(Constants.TANK_MEDIUM, isWater);
	private final Predicate<FluidStack> isSteam = fluid -> fluid.getFluid() == SysteamsRegistry.Fluids.STEAM.stillFluid.get();
	public final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_LARGE, isSteam);

	private Direction facing;
	/**
	 * The amount of energy the last consumed fuel has left. Decrements each tick as more steam is generated
	 */
	protected int fuelRemaining;
	/**
	 * The amount of energy the last consumed fuel could generate (used to work out the size of the flame in the gui)
	 */
	protected int fuelMax = 0;

	public BoilerBlockEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		tankInv.addTank(waterTank, StorageGroup.INPUT);
		tankInv.addTank(steamTank, StorageGroup.OUTPUT);

		addAugmentSlots(ThermalCoreConfig.dynamoAugments);

		facing = state.getValue(FACING_ALL);
	}

	// ticking 🐊
	@Override
	public void tickServer() {
		final boolean startedActive = isActive;
		if (startedActive) {
			// dispense some steam
			if (!steamTank.isEmpty())
				transferSteamOut();
			// if we are disabled by redstone, or don't have enough fuel/water to continue, then deactivate
			if (!redstoneControl.getState() || (!isCurrentFuelValid() && fuelRemaining < 1) || waterTank.isEmpty()) {
				isActive = false;
			// otherwise we have fuel (either in the slot/tank, or in the buffer)
			} else if (isCurrentFuelValid() || fuelRemaining > 0){
				// generate steam

				// we don't have stuff in the buffer
				if (fuelRemaining < 1) {
					if (cacheRenderFluid())
						TileStatePacket.sendToClient(this);
					//consume some fuel from the slot/tank, and add it to the buffer
					int fuelToAdd = consumeFuel();
					fuelRemaining += fuelToAdd;
					fuelMax = fuelToAdd;
				}

				// get how much energy we would be making this tick (if we were a dynamo)
				int energy = (int) (BASE_STEAM_GENERATION.get() * energyMod);

				// consume fuel from the buffer
				fuelRemaining -= energy;
				// if we went over the amount in our fuel buffer, then generate less steam this tick, and reset fuelRemaining to 0 so it doesnt potentially cause errors later on
				if (fuelRemaining < 0) {
					energy = -fuelRemaining;
					fuelRemaining = 0;
				}

				// turn that into a steam and water amount. Take into account fuel eff here, so that the water amount is also affected
				int steam = (int) Math.round(energy * getEnergyToSteamRatio() * fuelEff);
				if (WATER_TO_STEAM_RATIO.get() == 0) // catch it before it tries to divide by 0, to give a clearer exception
					throw new IllegalStateException("water_to_steam_ratio can't be 0! Change it in saves/worldname/serverconfig/systeams-server.toml");
				int water = (int) Math.ceil(steam / WATER_TO_STEAM_RATIO.get());

				// drain from the water tank, and set the steam amount based on how much was drained (dont want to make more steam than the water could actually make
				int waterDrained = waterTank.drain(water, IFluidHandler.FluidAction.EXECUTE).getAmount();
				steam = (int) (waterDrained * WATER_TO_STEAM_RATIO.get());

				if (steam > 0)
				// now add to the steam tank
					steamTank.fill(new FluidStack(SysteamsRegistry.Fluids.STEAM.getStill(), steam), IFluidHandler.FluidAction.EXECUTE);
				else
					// if we didn't end up generating steam, then deactivate
					isActive = false;
			}
		} else if (Utils.timeCheckQuarter()) {
            if (redstoneControl.getState() && isCurrentFuelValid() && !waterTank.isEmpty()) {
				isActive = true;
			}
        }
        updateActiveState(startedActive);
	}

	/**
	 * Consumes fuel from the internal buffer
	 * @return How much energy would have been generated by the fuel, had it been in a regular dynamo
	 */
	protected abstract int consumeFuel();

	/**
	 * Checks if this inventory has a valid fuel
	 * @return If this inventory has valid fuel
	 */
	protected boolean isCurrentFuelValid() {
		return getCurrentFuel() != null;
	}

	protected IDynamoFuel getCurrentFuel() {
		return getFuelManager().getFuel(this);
	}

	protected abstract double getEnergyToSteamRatio();

	protected abstract IFuelManager getFuelManager();

	@Override
	public int getScaledDuration(int scale) {
		if (fuelMax <= 0 || fuelRemaining <= 0) {
            return 0;
        }
        return scale * fuelRemaining / fuelMax;
	}

	@Override
	protected int getBaseEnergyStorage() {
		return 0; // we don't store no energy here in boiler town
	}

	// region AUGMENTS
    protected float energyMod = 1.0F;
	protected float fuelEff = 1.0f;

    @Override
    protected Predicate<ItemStack> augValidator() {

        BiPredicate<ItemStack, List<ItemStack>> validator = tankInv.hasTanks() ? ThermalAugmentRules.DYNAMO_VALIDATOR : ThermalAugmentRules.DYNAMO_NO_FLUID_VALIDATOR;
        return item -> AugmentDataHelper.hasAugmentData(item) && validator.test(item, getAugmentsAsList());
    }

    @Override
    protected void resetAttributes() {
        super.resetAttributes();

		energyMod = 1.0F;
		fuelEff = 1.0F;

        AugmentableHelper.setAttribute(augmentNBT, TAG_AUGMENT_DYNAMO_POWER, energyMod);
		AugmentableHelper.setAttribute(augmentNBT, TAG_AUGMENT_DYNAMO_ENERGY, fuelEff);
	}

    @Override
    protected void setAttributesFromAugment(CompoundTag augmentData) {
        super.setAttributesFromAugment(augmentData);

        AugmentableHelper.setAttributeFromAugmentAdd(augmentNBT, augmentData, TAG_AUGMENT_DYNAMO_POWER);
		AugmentableHelper.setAttributeFromAugmentAdd(augmentNBT, augmentData, TAG_AUGMENT_DYNAMO_ENERGY);

		energyMod *= AugmentableHelper.getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_DYNAMO_POWER, 1.0F);
		fuelEff *= AugmentableHelper.getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_DYNAMO_ENERGY, 1.0F);
    }

    @Override
    protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {
        super.finalizeAttributes(enchantmentMap);

        energyMod = MathHelper.clamp(energyMod, AUG_SCALE_MIN, AUG_SCALE_MAX);
		fuelEff = MathHelper.clamp(fuelEff, AUG_SCALE_MIN, AUG_SCALE_MAX);
    }
    // endregion

	// NBTIO
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        fuelMax = nbt.getInt(NBTTags.TAG_FUEL_MAX);
        fuelRemaining = nbt.getInt(NBTTags.TAG_FUEL);
//        coolantMax = nbt.getInt(TAG_COOLANT_MAX);
//        coolant = nbt.getInt(TAG_COOLANT);
//        processTick = nbt.getInt(TAG_PROCESS_TICK);

        updateHandlers();
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {

        super.saveAdditional(nbt);

        nbt.putInt(NBTTags.TAG_FUEL_MAX, fuelMax);
        nbt.putInt(NBTTags.TAG_FUEL, fuelRemaining);
//        nbt.putInt(TAG_COOLANT_MAX, coolantMax);
//        nbt.putInt(TAG_COOLANT, coolant);
//        nbt.putInt(TAG_PROCESS_TICK, processTick);
    }

	// Networking
	@Override
    public FriendlyByteBuf getGuiPacket(FriendlyByteBuf buffer) {
        super.getGuiPacket(buffer);

        buffer.writeInt(fuelMax);
        buffer.writeInt(fuelRemaining);

        return buffer;
    }

    @Override
    public void handleGuiPacket(FriendlyByteBuf buffer) {
        super.handleGuiPacket(buffer);

        fuelMax = buffer.readInt();
        fuelRemaining = buffer.readInt();
    }


	// IThermalInventory
	@Override
	public List<? extends IItemStackHolder> inputSlots() {
		return inventory.getInputSlots();
	}

	@Override
	public List<? extends IFluidStackHolder> inputTanks() {
		return tankInv.getInputTanks();
	}


	// Steam helpers

	protected void transferSteamOut() {
		FluidHelper.insertIntoAdjacent(this, steamTank, 1000, getFacing());
    }


	// General helpers
    protected Direction getFacing() {
        if (facing == null) {
            updateFacing();
        }
        return facing;
    }

    protected void updateFacing() {
        facing = getBlockState().getValue(FACING_ALL);
        updateHandlers();
    }


	// Capabilities
    @Override
    protected <T> LazyOptional<T> getItemHandlerCapability(@Nullable Direction side) {
        if (side != null && side.equals(getFacing())) {
            return LazyOptional.empty();
        }
        return super.getItemHandlerCapability(side);
    }

    @Override
    protected <T> LazyOptional<T> getFluidHandlerCapability(@Nullable Direction side) {
        if (side != null && side.equals(getFacing())) {
            return LazyOptional.empty();
        }
        return super.getFluidHandlerCapability(side);
    }
}
