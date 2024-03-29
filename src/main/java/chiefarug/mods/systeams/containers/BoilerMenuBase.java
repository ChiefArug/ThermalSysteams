package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import cofh.core.common.inventory.BlockEntityCoFHMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BoilerMenuBase<T extends BoilerBlockEntityBase> extends BlockEntityCoFHMenu {

	public final T blockEntity;

	@SuppressWarnings("unchecked") // if it's not a T, then something else has gone wrong
	public BoilerMenuBase(@Nullable MenuType<?> type, int windowId, Level world, BlockPos pos, Inventory inventory, Player player) {
		super(type, windowId, world, pos, inventory, player);
		blockEntity = (T) world.getBlockEntity(pos);
	}
}
