package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BoilerStirlingBlockEntity extends BoilerBlockEntityBase {
	public BoilerStirlingBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.BlockEntities.STIRLING_BOILER.get(), pos, state);
	}
}
