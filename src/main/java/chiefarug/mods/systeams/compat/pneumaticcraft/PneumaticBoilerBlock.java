package chiefarug.mods.systeams.compat.pneumaticcraft;

import chiefarug.mods.systeams.block.BoilerBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class PneumaticBoilerBlock extends BoilerBlock {
	public PneumaticBoilerBlock(Properties builder, Class<?> tileClass, Supplier<BlockEntityType<?>> blockEntityType) {
		super(builder, tileClass, blockEntityType);
	}
}
