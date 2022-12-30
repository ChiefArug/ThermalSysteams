package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import cofh.core.inventory.container.TileContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BoilerContainerBase<T extends BoilerBlockEntityBase> extends TileContainer {

	public final T blockEntity;

	public BoilerContainerBase(@Nullable MenuType<?> type, int windowId, Level world, BlockPos pos, Inventory inventory, Player player) {
		super(type, windowId, world, pos, inventory, player);
		blockEntity = (T) world.getBlockEntity(pos);
	}
}
