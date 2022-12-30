package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
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
import cofh.lib.util.helpers.MathHelper;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.lib.common.ThermalAugmentRules;
import cofh.thermal.lib.tileentity.ThermalTileAugmentable;
import cofh.thermal.lib.util.managers.IFuelManager;
import cofh.thermal.lib.util.recipes.IThermalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

import static cofh.lib.util.Constants.AUG_SCALE_MAX;
import static cofh.lib.util.Constants.AUG_SCALE_MIN;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_BASE_MOD;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_ENERGY;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DYNAMO_POWER;

public abstract class BoilerBlockEntityBase extends ThermalTileAugmentable implements ITickableTile.IServerTickable, IThermalInventory {

	private final Predicate<FluidStack> isWater = fluid -> filter.valid(fluid) && SysteamsRegistry.Fluids.WATER.contains(fluid.getFluid());
	public final FluidStorageCoFH waterTank = new FluidStorageCoFH(Constants.TANK_MEDIUM, isWater);
	private final Predicate<FluidStack> isSteam = fluid -> fluid.getFluid() == SysteamsRegistry.Fluids.STEAM.stillFluid.get();
	public final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_LARGE, isSteam);

	private Direction facing;
	protected int fuelRemaining;

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
		boolean startedActive = isActive;
		if (startedActive) {
			// dispense some steam
			if (!steamTank.isEmpty())
				transferSteamOut();
			// try to fuel.
			boolean generatedSteam = false;
			if (tryToFuel())
				generatedSteam = generateSteam(steamPerTick());
			if (!redstoneControl.getState() || !generatedSteam)
				isActive = false;
		} else if (Utils.timeCheckQuarter()) {
            if (redstoneControl.getState() && tryToFuel()) {
				isActive = true;
			}
        }
        updateActiveState(startedActive);
	}

	/**
	 * Consumes some fuel from the internal buffer. If no fuel is found there then attempts to consume fuel from the input slot.
	 * Also checks for water, and will fail if no water is found
	 * @return If the boiler was successfully fueled
	 */
	protected boolean tryToFuel() {
		if (waterTank.getAmount() < 1) return false;
		if (fuelRemaining >= 1) {
			// we have some fuel stored in the internal buffer, consume that
			fuelRemaining -= fuelConsumptionRate();
			return true;
		}
		// no fuel left. try to consume some
		return consumeFuel() > 0;
	}

	/**
	 * Attempts to consume fuel from the input slot (be it fluid, item or some other form)
	 *
	 * @return The amount the internal fuel buffer was filled by. 0 if nothing was added
	 */
	protected int consumeFuel() {
		if (isCurrentFuelValid()) {
			return getCurrentFuelValue();
		}
		return 0;
	}

	/**
	 * Checks if this inventory has a valid fuel
	 * @return If this inventory has valid fuel
	 */
	protected boolean isCurrentFuelValid() {
		return getFuelManager().validFuel(this);
	}

	/**
	 * //TODO: implement this properly
	 * @return The amount of fuel consumed from the internal buffer per tick
	 */
	protected int fuelConsumptionRate() {
		return 1;
	}

	/**
	 * Generates steam, using the water from the internal water tank and placing stea, into the internal steam tank.
	 * @param maxSteam The maximum amount of steam that should be generated
	 * @return If any steam was generated
	 */
	protected boolean generateSteam(int maxSteam) {
		// maxSteam is how much steam we should try to make
		// get how much steam we could make from the water we have
		// use whatever is smaller
		// insert that steam into the steam tank
		// drain that water from the water tank
		int maxSteamFromWater = waterToSteam(waterTank.getFluidInTank(0).getAmount());

		int amountToFill = Math.min(maxSteam, maxSteamFromWater);
		int amountFilled = steamTank.fill(new FluidStack(SysteamsRegistry.Fluids.STEAM.getStill(), amountToFill), IFluidHandler.FluidAction.EXECUTE);
		waterTank.drain(steamToWater(amountFilled), IFluidHandler.FluidAction.EXECUTE);
		return amountFilled > 0;
	}

	protected int getCurrentFuelValue() {
		return getFuelManager().getFuel(this).getEnergy();
	}

	protected int steamPerTick() {
		return Math.round(energyToSteam(MathHelper.clamp(energyMod, AUG_SCALE_MIN, AUG_SCALE_MAX)));
	}

	protected int energyToSteam(double energy) {
		return (int) Math.round(energy * getEnergyToSteamRation());
	}

	protected abstract double getEnergyToSteamRation();

	protected abstract IFuelManager getFuelManager();

	@Override
	protected int getBaseEnergyStorage() {
		return 0; // we don't store no energy here in boiler town
	}

	// region AUGMENTS
    protected float energyMod = 1.0F;

    @Override
    protected Predicate<ItemStack> augValidator() {

        BiPredicate<ItemStack, List<ItemStack>> validator = tankInv.hasTanks() ? ThermalAugmentRules.DYNAMO_VALIDATOR : ThermalAugmentRules.DYNAMO_NO_FLUID_VALIDATOR;
        return item -> AugmentDataHelper.hasAugmentData(item) && validator.test(item, getAugmentsAsList());
    }

    @Override
    protected void resetAttributes() {

        super.resetAttributes();

        AugmentableHelper.setAttribute(augmentNBT, TAG_AUGMENT_DYNAMO_POWER, 1.0F);

        energyMod = 1.0F;

    }

    @Override
    protected void setAttributesFromAugment(CompoundTag augmentData) {

        super.setAttributesFromAugment(augmentData);

        AugmentableHelper.setAttributeFromAugmentAdd(augmentNBT, augmentData, TAG_AUGMENT_DYNAMO_POWER);

        energyMod *= AugmentableHelper.getAttributeModWithDefault(augmentData, TAG_AUGMENT_DYNAMO_ENERGY, 1.0F);
    }

    @Override
    protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {

        creativeEnergy = false;

        super.finalizeAttributes(enchantmentMap);
        float componentModifier = AugmentableHelper.getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_BASE_MOD, 1.0F);
        float powerModifier = AugmentableHelper.getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_DYNAMO_POWER, 1.0F);
        float totalMod = componentModifier * powerModifier;

//        baseProcessTick = Math.round(getBaseProcessTick() * totalMod);
        energyMod = MathHelper.clamp(energyMod, AUG_SCALE_MIN, AUG_SCALE_MAX);

//        processTick = baseProcessTick;
//        minProcessTick = throttleFeature ? 0 : baseProcessTick / 10;
    }

    protected final float getEnergyMod() {

        return energyMod;
    }
    // endregion


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
	private int waterToSteam(int water) {
		// Rounds down
		return (int) (water * SysteamsConfig.WATER_TO_STEAM_RATIO.get());
	}

	private int steamToWater(int steam) {
		// Rounds up
		return (int) Math.ceil(steam / SysteamsConfig.WATER_TO_STEAM_RATIO.get());
	}

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
