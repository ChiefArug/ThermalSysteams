package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.core.inventory.container.TileContainer;
import cofh.core.util.ProxyUtils;
import cofh.lib.inventory.wrapper.InvWrapperCoFH;
import cofh.thermal.lib.tileentity.DynamoTileBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SteamDynamoContainer extends TileContainer {

	public final DynamoTileBase blockEntity;

	public SteamDynamoContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
		this(windowId, ProxyUtils.getClientWorld(), data.readBlockPos(), inv, ProxyUtils.getClientPlayer());
	}

	public SteamDynamoContainer(int windowId, Level level, BlockPos pos, Inventory inventory, Player player) {
		super(SysteamsRegistry.Menus.DYNAMO_STEAM.get(), windowId, level, pos, inventory, player);

		blockEntity = (DynamoTileBase) level.getBlockEntity(pos);
        InvWrapperCoFH blockEntityInv = new InvWrapperCoFH(blockEntity.getItemInv());

        bindAugmentSlots(blockEntityInv, 0, blockEntity.augSize());
        bindPlayerInventory(inventory);
	}
}
