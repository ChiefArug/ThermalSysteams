package chiefarug.mods.systeams.block_entities;

import cofh.lib.fluid.FluidStorageCoFH;
import cofh.lib.util.Constants;
import cofh.thermal.lib.util.managers.SingleFluidFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FluidBoilerBlockEntityBase extends BoilerBlockEntityBase{

	protected FluidStorageCoFH fuelTank =new FluidStorageCoFH(Constants.TANK_SMALL, fluid -> filter.valid(fluid) && getFuelManager().validFuel(fluid));

	public FluidBoilerBlockEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		tankInv.addSlot(fuelTank);
	}

	@Override
	protected abstract SingleFluidFuelManager getFuelManager();
}
