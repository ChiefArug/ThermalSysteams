package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SNBTTags;
import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.compat.mekanism.SysteamsMekanismCompat;
import chiefarug.mods.systeams.recipe.BoilingRecipeManager;
import cofh.core.util.helpers.AugmentDataHelper;
import cofh.core.util.helpers.AugmentableHelper;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.api.StorageGroup;
import cofh.lib.api.block.entity.ITickableTile;
import cofh.lib.api.fluid.IFluidStackHolder;
import cofh.lib.api.inventory.IItemStackHolder;
import cofh.lib.common.fluid.FluidStorageCoFH;
import cofh.lib.util.Constants;
import cofh.lib.util.Utils;
import cofh.lib.util.constants.NBTTags;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermal.lib.common.block.entity.AugmentableBlockEntity;
import cofh.thermal.lib.util.ThermalAugmentRules;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static cofh.core.util.helpers.AugmentableHelper.getAttributeModWithDefault;
import static cofh.lib.util.Constants.AUG_SCALE_MAX;
import static cofh.lib.util.Constants.AUG_SCALE_MIN;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_BASE_MOD;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_ENERGY;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_POWER;
import static cofh.lib.util.constants.NBTTags.TAG_PROCESS_TICK;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class BoilerBlockEntityBase extends AugmentableBlockEntity implements ITickableTile.IServerTickable, IThermalInventory {
	private static final int TRANSFER_PER_TICK = 1000;

	private final Predicate<FluidStack> isWater = fluid -> filter.valid(fluid) && BoilingRecipeManager.instance().canBoil(fluid);
	public final FluidStorageCoFH waterTank = new FluidStorageCoFH(Constants.TANK_MEDIUM, isWater);
	private final Predicate<FluidStack> isSteam = fluid -> SysteamsRegistry.Fluids.STEAMISH_TAG.contains(fluid.getFluid());
	public final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_LARGE, isSteam);

	private LazyOptional<?> steamCap = LazyOptional.empty();

	private Direction facing;

	protected int maxEnergyBuffer;
	protected int remainingEnergyBuffer;
	protected int energyPerTick;
	protected int steamPerTick;

	protected double remainingwWaterBuffer;
	protected double waterPerTick;

	protected BoilingRecipeManager.BoiledFluid cachedOutput;

	protected int baseEnergyPerTick = getBaseProcessTick();
	public boolean gasMode = false;

	public BoilerBlockEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		tankInv.addTank(waterTank, StorageGroup.INPUT);
		tankInv.addTank(steamTank, StorageGroup.OUTPUT);

		facing = state.getValue(FACING_ALL);

		recalculateEnergy();
		recalculateWater();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setBlockState(BlockState pBlockState) {
		super.setBlockState(pBlockState);
		updateFacing();
	}

	// ticking 🐊
	@Override
	public void tickServer() {
		boolean curActive = isActive;
		if (isActive) {
 			processTick();
			if (canProcessFinish()) {
				processFinish();
				isActive = false; // ensure that isActive is false when recalculateEnergy is called in canProcessStart()
                if (redstoneControl.getState() && canProcessStart()) {
                    isActive = true;
                    processStart();
                }
            }
		} else if (Utils.timeCheckQuarter()) {
			if (!steamTank.isEmpty())
				// slowly transfer steam out if it's not turned on, so we don't get stuck with a full tank.
				transferSteamOut(TRANSFER_PER_TICK / 10);
			if (redstoneControl.getState() && canProcessStart()) {
				isActive = true; // setting it here is important as we check isActive in recalculateEnergy
				processStart();
				processTick();
			}
		}
		updateActiveState(curActive);
	}


	protected boolean canProcessStart() {
		if (!cacheBoilingRecipe())
			return false;

		return tryHaveWaterToTick() && tryHaveEnergyToTick() && steamTank.getSpace() >= steamPerTick;
	}

	protected void processStart() {
		// make sure that these are definitely set properly
		recalculateEnergy();
		recalculateWater();
	}

	protected void processFinish() {
		if (energyPerTick < remainingEnergyBuffer) // set max to -1 (so that the flame displays as empty) if we finished because we are out of energy buffer.
			maxEnergyBuffer = -1;
	}

	protected boolean canProcessFinish() { // these use has not tryHave because they will auto refill in the tick method
		return !hasWaterToTick() || !hasEnergyToTick() || steamTank.getSpace() < steamPerTick;
	}

	protected void processTick() {
		if (this.waterTank.isEmpty() && this.cachedOutput == null) {
			this.remainingwWaterBuffer = 0;
			return; // edge case when the water tank empties but there is still stuff left in the water buffer and we were saved to disk in that time.
		}

		// new steam. this is up here so that we can yoink the cached output before the water runs out.
		FluidStack newSteam = new FluidStack(cachedOutput.fluidOut(), steamPerTick);
		// water
		remainingwWaterBuffer -= waterPerTick;
		tryHaveWaterToTick();
		// energy
		remainingEnergyBuffer -= energyPerTick;
		tryHaveEnergyToTick();
		// steam. note this is lossy if there is not enough space in the tank. that is fine, because if someone is letting that happen too often that is their fault.
		steamTank.fill(newSteam, EXECUTE);

		transferSteamOut(TRANSFER_PER_TICK);
	}

	/**
	 * @return Returns how much energy would be generated by the fuel this has.
	 */
	protected int getFuelEnergy() {
		IDynamoFuel fuel = getFuelManager().getFuel(this);
		return fuel == null ? 0 : fuel.getEnergy();
	}

	/**
	 * Consumes fuel from the internal buffer.
	 *
	 * @return How much energy would have been generated by the fuel, had it been in a regular dynamo
	 */
	protected abstract int consumeFuel();

	protected abstract double getEnergyToSteamRatio();

	protected abstract IFuelManager getFuelManager();

	protected abstract double getSpeedMultiplier();


	protected boolean cacheBoilingRecipe() {
		// dont change the cached recipe if the tank is empty.
		// this ensures that when the tank is finally emptied after ticking but there is water in the buffer it wont crash.
		if (waterTank.isEmpty()) return this.cachedOutput != null;
		return (this.cachedOutput = BoilingRecipeManager.instance().boil(waterTank.getFluidStack())) != null;
	}

	protected void recalculateEnergy() {
		if (isActive) // if its not active leave it as it is, at -1
			maxEnergyBuffer = Math.max(maxEnergyBuffer, remainingEnergyBuffer);
		baseEnergyPerTick = getBaseProcessTick();
		energyPerTick = (int) (baseEnergyPerTick * generationModifier);
		steamPerTick = (int) (energyPerTick * getEnergyToSteamRatio());
	}

	protected boolean hasEnergyToTick() {
		return energyPerTick <= remainingEnergyBuffer;
	}

	protected boolean tryHaveEnergyToTick() {
		if (hasEnergyToTick())
			return true;
		refillEnergy();
		return hasEnergyToTick();
	}

	protected void refillEnergy() {

		int refill = (int) (consumeFuel() * efficiencyModifier);
		if (refill == 0) return;

		remainingEnergyBuffer += refill;
		recalculateEnergy();
	}

	protected void recalculateWater() {
		if (cacheBoilingRecipe()) {
			waterPerTick = cachedOutput.getInPerTick(steamPerTick);
		}
	}

	protected boolean hasWaterToTick() {
		return waterPerTick <= remainingwWaterBuffer;
	}

	protected boolean tryHaveWaterToTick() {
		if (hasWaterToTick())
			return true;
		refillWater();
		return hasWaterToTick();
	}

	protected void refillWater() {
		if (!cacheBoilingRecipe()) return;

		int refill = this.waterTank.drain((int) Math.ceil(waterPerTick), IFluidHandler.FluidAction.EXECUTE).getAmount();
		if (refill == 0) return;

		remainingwWaterBuffer += refill;
		recalculateWater();
	}


	@Override
	protected int getBaseProcessTick() {
		return (int) (super.getBaseProcessTick() * getSpeedMultiplier());
	}

	@Override
	public int getScaledDuration(int scale) {
		if (maxEnergyBuffer <= 0 || remainingEnergyBuffer <= 0) {
			return 0;
		}
		return scale * remainingEnergyBuffer / maxEnergyBuffer;
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

		generationModifier += AugmentableHelper.getAttributeModWithDefault(augmentData, TAG_AUGMENT_DYNAMO_POWER, 0.0F);
		efficiencyModifier *= AugmentableHelper.getAttributeModWithDefault(augmentData, TAG_AUGMENT_DYNAMO_ENERGY, 1.0F);
	}

	@Override
	protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {
		super.finalizeAttributes(enchantmentMap);

        generationModifier *= getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_BASE_MOD, 1.0F);

        efficiencyModifier = MathHelper.clamp(efficiencyModifier, AUG_SCALE_MIN, AUG_SCALE_MAX);

		recalculateEnergy();
		recalculateWater();
	}
	// endregion

	// region NBTIO
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);

		maxEnergyBuffer = nbt.getInt(NBTTags.TAG_FUEL_MAX);
		remainingEnergyBuffer = nbt.getInt(NBTTags.TAG_FUEL);
		energyPerTick = nbt.getInt(TAG_PROCESS_TICK);
		recalculateEnergy();

		remainingwWaterBuffer = nbt.getDouble(SNBTTags.TAG_WATER);
		waterPerTick = nbt.getDouble(SNBTTags.TAG_WATER_PER_TICK);

//        coolantMax = nbt.getInt(TAG_COOLANT_MAX);
//        coolant = nbt.getInt(TAG_COOLANT);

		recalculateEnergy();
		recalculateWater();

		updateHandlers();
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {

		super.saveAdditional(nbt);

		nbt.putInt(NBTTags.TAG_FUEL_MAX, maxEnergyBuffer);
		nbt.putDouble(NBTTags.TAG_FUEL, remainingEnergyBuffer);
        nbt.putDouble(TAG_PROCESS_TICK, energyPerTick);
		nbt.putDouble(SNBTTags.TAG_WATER, remainingwWaterBuffer);
		nbt.putDouble(SNBTTags.TAG_WATER_PER_TICK, waterPerTick);

//        nbt.putInt(TAG_COOLANT_MAX, coolantMax);
//        nbt.putInt(TAG_COOLANT, coolant);
	}
	// endregion

	// region NETWORK
	@Override
	public FriendlyByteBuf getGuiPacket(FriendlyByteBuf buffer) {
		super.getGuiPacket(buffer);

		buffer.writeInt(maxEnergyBuffer);
		buffer.writeInt(remainingEnergyBuffer);
		buffer.writeBoolean(gasMode);
		buffer.writeInt(steamPerTick);

		return buffer;
	}

	@Override
	public void handleGuiPacket(FriendlyByteBuf buffer) {
		super.handleGuiPacket(buffer);

		this.maxEnergyBuffer = buffer.readInt();
		this.remainingEnergyBuffer = buffer.readInt();
		gasMode = buffer.readBoolean();
		steamPerTick = buffer.readInt();
	}
	// endregion


	@Override
    public double getEfficiency() {
		if (efficiencyModifier <= 0) {
            return Double.MIN_VALUE;
        }
		return efficiencyModifier;
	}

	@Override
	public int getCurSpeed() {
		return isActive ? steamPerTick : 0;
	}

	public double currentWaterConsumption() {
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

	protected void transferSteamOut(int rate) {
		if (FluidHelper.insertIntoAdjacent(this, steamTank, rate, getFacing())) {
			gasMode = false; // if we extract liquid then we are no longer in gas mode.
		}
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


	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == Systeams.GAS_HANDLER_CAPABILITY) return getGasHandlerCapability(side);
		return super.getCapability(cap, side);
	}

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
			if (!steamCap.isPresent()) {
				steamCap = LazyOptional.of(() -> tankInv.getHandler(StorageGroup.OUTPUT));
			}
			gasMode = false; // we have just been queried for the steam cap in liquid form, we are no longer outputting gas
			return steamCap.cast();
		}
		return super.getFluidHandlerCapability(side);
	}

	protected <T> LazyOptional<T> getGasHandlerCapability(@Nullable Direction side) {
		if (side != null && side.equals(getFacing())) {
			LazyOptional<IFluidHandler> fluidCap = this.getFluidHandlerCapability(side);
			gasMode = true; // we have just been queried for the steam cap in gas form
			return SysteamsMekanismCompat.wrapLiquidCapability(fluidCap).cast();
		}
		return LazyOptional.empty();
	}
}
