package chiefarug.mods.systeams.compat.pneumaticcraft;

import chiefarug.mods.systeams.block.BoilerBlock;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class PneumaticBoilerBlock extends BoilerBlock {

	public PneumaticBoilerBlock(Properties builder, Class<?> tileClass, Supplier<BlockEntityType<?>> blockEntityType) {
		super(builder, tileClass, blockEntityType);
	}

	@Override
	public ItemStack getOtherConversionItem() {
		return new ItemStack(ModBlocks.ADVANCED_PRESSURE_TUBE.get());
	}
}
