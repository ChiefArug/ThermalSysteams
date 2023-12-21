package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.core.common.inventory.BlockEntityCoFHMenu;
import cofh.core.util.ProxyUtils;
import cofh.lib.common.inventory.wrapper.InvWrapperCoFH;
import cofh.thermal.lib.common.block.entity.DynamoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SteamDynamoMenu extends BlockEntityCoFHMenu {

	public final DynamoBlockEntity blockEntity;

	public SteamDynamoMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
		this(windowId, ProxyUtils.getClientWorld(), data.readBlockPos(), inv, ProxyUtils.getClientPlayer());
	}

	public SteamDynamoMenu(int windowId, Level level, BlockPos pos, Inventory inventory, Player player) {
		super(SysteamsRegistry.Menus.DYNAMO_STEAM.get(), windowId, level, pos, inventory, player);

		blockEntity = (DynamoBlockEntity) level.getBlockEntity(pos);
        InvWrapperCoFH blockEntityInv = new InvWrapperCoFH(blockEntity.getItemInv());

        bindAugmentSlots(blockEntityInv, 0, blockEntity.augSize());
        bindPlayerInventory(inventory);
	}
}
