package chiefarug.mods.systeams.block_entities;

import cofh.core.util.helpers.FluidHelper;
import cofh.lib.api.StorageGroup;
import cofh.lib.api.fluid.IFluidStackHolder;
import cofh.lib.fluid.FluidStorageCoFH;
import cofh.lib.util.Constants;
import cofh.thermal.lib.util.managers.SingleFluidFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

import static cofh.lib.util.Constants.BUCKET_VOLUME;

public abstract class FluidBoilerBlockEntityBase extends BoilerBlockEntityBase{

	protected FluidStorageCoFH fuelTank = new FluidStorageCoFH(Constants.TANK_SMALL, fluid -> filter.valid(fluid) && getFuelManager().validFuel(fluid));

	public FluidBoilerBlockEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		tankInv.addTank(fuelTank, StorageGroup.INPUT);

		renderFluid = new FluidStack(Fluids.WATER, BUCKET_VOLUME);
	}

	@Override
	protected abstract SingleFluidFuelManager getFuelManager();

	@Override
	public List<? extends IFluidStackHolder> inputTanks() {
		// Don't include the water tank, or else recipe checking fails
		return List.of(getFuelTank());
	}

	@Override
	protected int consumeFuel() {
		int energy = getCurrentFuel().getEnergy();
		fuelTank.drain(SingleFluidFuelManager.FLUID_FUEL_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
		return energy;
	}

	public FluidStorageCoFH getFuelTank() {
		return fuelTank;
	}

	@Override
    protected boolean cacheRenderFluid() {
        FluidStack prevFluid = renderFluid;
        renderFluid = new FluidStack(fuelTank.getFluidStack(), BUCKET_VOLUME);
        return !FluidHelper.fluidsEqual(renderFluid, prevFluid);
    }
}
