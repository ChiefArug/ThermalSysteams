package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SNBTTags;
import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.compat.mekanism.SysteamsMekanismCompat;
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
import cofh.lib.util.helpers.BlockHelper;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static cofh.lib.util.Constants.AUG_SCALE_MAX;
import static cofh.lib.util.Constants.AUG_SCALE_MIN;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_ENERGY;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_POWER;
import static cofh.lib.util.constants.NBTTags.TAG_PROCESS_TICK;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public abstract class BoilerBlockEntityBase extends AugmentableBlockEntity implements ITickableTile.IServerTickable, IThermalInventory {
	private static final int TRANSFER_PER_TICK = 1000;

	private final Predicate<FluidStack> isWater = fluid -> filter.valid(fluid) && SysteamsRegistry.Fluids.WATER_TAG.contains(fluid.getFluid());
	public final FluidStorageCoFH waterTank = new FluidStorageCoFH(Constants.TANK_MEDIUM, isWater);
	private final Predicate<FluidStack> isSteam = fluid -> SysteamsRegistry.Fluids.STEAM_TAG.contains(fluid.getFluid());
	public final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_LARGE, isSteam);

	private LazyOptional<?> steamCap = LazyOptional.empty();

	private Direction facing;

	protected static final class Fuel {
		protected int maxBuffer;
		protected double remainingBuffer;
		protected double perTick;

		protected Fuel(int maxBuffer, double perTick) {
			this(maxBuffer, maxBuffer, perTick);
		}

		protected Fuel(int maxBuffer, double remainingBuffer, double perTick) {
			this.maxBuffer = maxBuffer;
			this.remainingBuffer = remainingBuffer;
			this.perTick = perTick;
		}

		protected double calculatePerTick() {
			return perTick = 1.0;
		}

		protected boolean tick() {
			return (remainingBuffer -= perTick) <= 0;
		}

		protected int refuel() {
			int required = (int) (maxBuffer - remainingBuffer);
			remainingBuffer += required;
			return required;
		}

		protected boolean hasEnoughToTick() {
			return remainingBuffer >= perTick;
		}


	}

	protected static final int FUEL_MAGIC_NUMBER = 20;
	protected Fuel steam;
	protected static final int WATER_MAGIC_NUMBER = 20;
	protected Fuel water;

	protected int baseEnergyPerTick = getBaseProcessTick();
	public boolean gasMode = false;

	public BoilerBlockEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		tankInv.addTank(waterTank, StorageGroup.INPUT);
		tankInv.addTank(steamTank, StorageGroup.OUTPUT);

		facing = state.getValue(FACING_ALL);

		steam = new Fuel();
		water = new Fuel();
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
		return (water.hasEnoughToTick() || waterTank.getAmount() > currentWaterConsumption()) &&
				(steam.hasEnoughToTick() || getFuelEnergy() > );
	}


	protected void processStart() {

	}

	protected void processFinish() {}

	protected boolean canProcessFinish() {
		return ;
	}

	protected void processTick() {

	}



	/**
	 * @return Returns how much energy would be generated by the fuel this has.
	 */
	protected int getFuelEnergy() {
		IDynamoFuel fuel = getFuelManager().getFuel(this);
		return fuel == null ? 0 : fuel.getEnergy();
	}

	protected abstract double getEnergyToSteamRatio();

	protected abstract IFuelManager getFuelManager();

	protected abstract double getSpeedMultiplier();

	@Override
	public int getScaledDuration(int scale) {
		if (steam.maxBuffer <= 0 || steam.remainingBuffer <= 0) {
			return 0;
		}
		return (int) (scale * steam.remainingBuffer / steam.maxBuffer);
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

		steamPerTick = ; //TODO
		waterPerTick = ;
	}
	// endregion

	// NBTIO
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);

		this.steam = new Fuel(
				nbt.getInt(NBTTags.TAG_FUEL_MAX),
				nbt.getDouble(NBTTags.TAG_FUEL),
				nbt.getDouble(TAG_PROCESS_TICK)
		);
		this.water = new Fuel(
				nbt.getInt(SNBTTags.TAG_WATER_MAX),
				nbt.getDouble(SNBTTags.TAG_WATER),
				nbt.getDouble(SNBTTags.TAG_WATER_PER_TICK)
		);

//        coolantMax = nbt.getInt(TAG_COOLANT_MAX);
//        coolant = nbt.getInt(TAG_COOLANT);

		updateHandlers();
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {

		super.saveAdditional(nbt);

		nbt.putInt(NBTTags.TAG_FUEL_MAX, steam.maxBuffer);
		nbt.putDouble(NBTTags.TAG_FUEL, steam.remainingBuffer);
        nbt.putDouble(TAG_PROCESS_TICK, steam.perTick);
		nbt.putInt(SNBTTags.TAG_WATER_MAX, water.maxBuffer);
		nbt.putDouble(SNBTTags.TAG_WATER, water.remainingBuffer);
		nbt.putDouble(SNBTTags.TAG_WATER_PER_TICK, water.perTick);

//        nbt.putInt(TAG_COOLANT_MAX, coolantMax);
//        nbt.putInt(TAG_COOLANT, coolant);
	}

	// Networking
	@Override
	public FriendlyByteBuf getGuiPacket(FriendlyByteBuf buffer) {
		super.getGuiPacket(buffer);

		buffer.writeInt(steam.maxBuffer);
		buffer.writeDouble(steam.remainingBuffer);
		buffer.writeBoolean(gasMode);

		return buffer;
	}

	@Override
	public void handleGuiPacket(FriendlyByteBuf buffer) {
		super.handleGuiPacket(buffer);

		this.steam.maxBuffer = buffer.readInt();
		this.steam.remainingBuffer = buffer.readDouble();
		gasMode = buffer.readBoolean();
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

	protected void transferSteamOut() {
		if (FluidHelper.insertIntoAdjacent(this, steamTank, TRANSFER_PER_TICK, getFacing())) {
			gasMode = false; // if we extract liquid then we are no longer in gas mode.
		}
	}

//	private int calcSteam(int energy) {
//		return (int) Math.round(energy * getEnergyToSteamRatio() * getSpeedMultiplier());
//	}
//
//	private int calcWater(int steam) {
//		return (int) Math.round(steam / WATER_TO_STEAM_RATIO.get());
//	}


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
		if (side!= null && side.equals(getFacing())) {
			LazyOptional<IFluidHandler> fluidCap = this.getFluidHandlerCapability(side);
			gasMode = true; // we have just been queried for the steam cap in gas form
			return SysteamsMekanismCompat.wrapLiquidCapability(fluidCap).cast();
		}
		return LazyOptional.empty();
	}

	// Copied from CoFH's FluidHelper class because theirs doesn't support simulating the insert
	private boolean simulateInsertToAdjacent(BlockEntity tile, FluidStorageCoFH tank, @SuppressWarnings("SameParameterValue") int amount, Direction side) {
		IFluidHandler.FluidAction action = SIMULATE;
		if (tank.isEmpty()) {
			return false;
		}
		amount = Math.min(amount, tank.getAmount());

		BlockEntity adjTile = BlockHelper.getAdjacentTileEntity(tile, side);
		Direction opposite = side.getOpposite();

		IFluidHandler handler = FluidHelper.getFluidHandlerCap(adjTile, opposite);
		if (handler == EmptyFluidHandler.INSTANCE) {
			return false;
		}
		int fillAmount = handler.fill(new FluidStack(tank.getFluidStack(), amount), action);
		if (fillAmount > 0) {
			tank.drain(fillAmount, action).getAmount();
			return true;
		}
		return false;
	}
}
