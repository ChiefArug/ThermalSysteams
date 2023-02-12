package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
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
import cofh.thermal.lib.block.entity.AugmentableBlockEntity;
import cofh.thermal.lib.common.ThermalAugmentRules;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static chiefarug.mods.systeams.SysteamsConfig.WATER_TO_STEAM_RATIO;
import static cofh.lib.util.Constants.AUG_SCALE_MAX;
import static cofh.lib.util.Constants.AUG_SCALE_MIN;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_ENERGY;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_POWER;
import static cofh.lib.util.constants.NBTTags.TAG_PROCESS_TICK;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class BoilerBlockEntityBase extends AugmentableBlockEntity implements ITickableTile.IServerTickable, IThermalInventory {

	private final Predicate<FluidStack> isWater = fluid -> filter.valid(fluid) && SysteamsRegistry.Fluids.WATER_TAG.contains(fluid.getFluid());
	public final FluidStorageCoFH waterTank = new FluidStorageCoFH(Constants.TANK_MEDIUM, isWater);
	private final Predicate<FluidStack> isSteam = fluid -> SysteamsRegistry.Fluids.STEAM_TAG.contains(fluid.getFluid());
	public final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_LARGE, isSteam);

	private Direction facing;
	/**
	 * The amount of steam the last consumed fuel has left. Decrements each tick as more steam is generated
	 */
	protected int fuelRemaining;
	/**
	 * The amount of steam the last consumed fuel could generate (used to work out the size of the flame in the gui)
	 */
	protected int fuelMax = 0;

	protected int baseEnergyPerTick = getBaseProcessTick();
	protected int steamPerTick = energyToSteam(baseEnergyPerTick);
	protected int waterPerTick = steamToWater(steamPerTick);

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
		boolean curActive = isActive;
		if (isActive) {
			processTick();
			if (canProcessFinish()) {
				processFinish();
				if (!redstoneControl.getState() || !canProcessStart()) {
					isActive = false;
				} else {
					processStart();
				}
			}
		} else if (Utils.timeCheckQuarter()) {
			if (redstoneControl.getState() && canProcessStart()) {
				processStart();
				processTick();
				isActive = true;
			}
		}
		updateActiveState(curActive);
	}

	protected boolean canProcessStart() {
		return (getCurrentEnergy() > 0 || fuelRemaining > 0) && waterTank.getAmount() >= waterPerTick && !steamTank.isFull();
	}

	protected void processStart() {
		if (fuelRemaining <= 0) {
			int fuelToAdd = energyToSteam((int) (consumeFuel() * efficiencyModifier));
			fuelRemaining += fuelToAdd;
			fuelMax = fuelToAdd;
		}

		transferSteamOut();
	}

	protected void processFinish() {
		transferSteamOut();
	}

	protected boolean canProcessFinish() {
		return fuelRemaining <= 0 || waterTank.getAmount() <= waterPerTick || steamTank.isFull();
	}

	protected void processTick() {
		transferSteamOut();
		generateSteam();
	}

	/**
	 * Calculates, consumes water, consumes fuel and fills steam
	 * These are all done together to avoid doing the maths over and over again.
	 */
	private void generateSteam() {
		fuelRemaining -= steamPerTick;
		// fill steam
		steamTank.fill(new FluidStack(SysteamsRegistry.Fluids.STEAM.getStill(), steamPerTick), EXECUTE);
		// and drain water
		waterTank.drain((int) (double) waterPerTick, EXECUTE);
	}

	/**
	 * Consumes fuel from the internal buffer
	 *
	 * @return How much energy would have been generated by the fuel, had it been in a regular dynamo
	 */
	protected abstract int consumeFuel();

	protected int getCurrentEnergy() {
		IDynamoFuel fuel = getFuelManager().getFuel(this);
		return fuel == null ? 0 : fuel.getEnergy();
	}

	protected abstract double getEnergyToSteamRatio();

	protected abstract IFuelManager getFuelManager();

	@Override
	public int getScaledDuration(int scale) {
		if (fuelMax <= 0 || fuelRemaining <= 0) {
			return 0;
		}
		return (int) (scale * (double) fuelRemaining / fuelMax);
	}

	@Override
	protected int getBaseEnergyStorage() {
		return 0; // we don't store no energy here in boiler town
	}

	// region AUGMENTS
	protected float generationModifier = 1.0F;
	protected float efficiencyModifier = 1.0f;

	@Override
	protected Predicate<ItemStack> augValidator() {
		BiPredicate<ItemStack, List<ItemStack>> validator = tankInv.hasTanks() ? ThermalAugmentRules.DYNAMO_VALIDATOR : ThermalAugmentRules.DYNAMO_NO_FLUID_VALIDATOR;
		return item -> AugmentDataHelper.hasAugmentData(item) && validator.test(item, getAugmentsAsList());
	}

	@Override
	protected void resetAttributes() {
		super.resetAttributes();

		generationModifier = 1.0F;
		efficiencyModifier = 1.0F;

		AugmentableHelper.setAttribute(augmentNBT, TAG_AUGMENT_DYNAMO_POWER, generationModifier);
		AugmentableHelper.setAttribute(augmentNBT, TAG_AUGMENT_DYNAMO_ENERGY, efficiencyModifier);
	}

	// Fuel Eff: TAG_AUGMENT_DYNAMO_ENERGY
	// Gen Speed: TAG_AUGMENT_DYNAMO_POWER

	/**
	 * Sets the BlockEntities attributes from the inserted augment
	 * Is run once per augment inserted
	 * @param augmentData The augment data
	 */
	@Override
	protected void setAttributesFromAugment(CompoundTag augmentData) {
		super.setAttributesFromAugment(augmentData);

		AugmentableHelper.setAttributeFromAugmentAdd(augmentNBT, augmentData, TAG_AUGMENT_DYNAMO_POWER);
		AugmentableHelper.setAttributeFromAugmentAdd(augmentNBT, augmentData, TAG_AUGMENT_DYNAMO_ENERGY);

		generationModifier += AugmentableHelper.getAttributeModWithDefault(augmentData, TAG_AUGMENT_DYNAMO_POWER, 0.0F);
		efficiencyModifier *= AugmentableHelper.getAttributeModWithDefault(augmentData, TAG_AUGMENT_DYNAMO_ENERGY, 1.0F);
	}

	@Override
	protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {
		super.finalizeAttributes(enchantmentMap);

		generationModifier = MathHelper.clamp(generationModifier * AugmentableHelper.getAttributeModWithDefault(augmentNBT, NBTTags.TAG_AUGMENT_BASE_MOD, 1.0F), AUG_SCALE_MIN, AUG_SCALE_MAX);
		efficiencyModifier = MathHelper.clamp(efficiencyModifier, AUG_SCALE_MIN, AUG_SCALE_MAX);

		baseEnergyPerTick = Math.round(getBaseProcessTick() * generationModifier);

		steamPerTick = energyToSteam(baseEnergyPerTick);
		waterPerTick = steamToWater(steamPerTick);
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
        steamPerTick = nbt.getInt(TAG_PROCESS_TICK);

		updateHandlers();
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {

		super.saveAdditional(nbt);

		nbt.putInt(NBTTags.TAG_FUEL_MAX, fuelMax);
		nbt.putInt(NBTTags.TAG_FUEL, fuelRemaining);
//        nbt.putInt(TAG_COOLANT_MAX, coolantMax);
//        nbt.putInt(TAG_COOLANT, coolant);
        nbt.putInt(TAG_PROCESS_TICK, steamPerTick);
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


	@Override
    public double getEfficiency() {
        if (getFuelEfficiencyMod() <= 0) {
            return Double.MIN_VALUE;
        }
        return getFuelEfficiencyMod();
    }

	private float getFuelEfficiencyMod() {
		return efficiencyModifier;
	}

	@Override
	public int getCurSpeed() {
		return isActive ? steamPerTick : 0;
	}

	public int getWaterConsumption() {
		return isActive ? waterPerTick : 0;
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

	private int energyToSteam(int energy) {
		return (int) Math.round(energy * getEnergyToSteamRatio());
	}

	private int steamToWater(int steam) {
		return (int) Math.round(steam / WATER_TO_STEAM_RATIO.get());
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
