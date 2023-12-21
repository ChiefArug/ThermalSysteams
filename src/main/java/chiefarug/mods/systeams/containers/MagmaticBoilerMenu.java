package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.block_entities.MagmaticBoilerBlockEntity;
import cofh.core.util.ProxyUtils;
import cofh.lib.common.inventory.wrapper.InvWrapperCoFH;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MagmaticBoilerMenu extends BoilerMenuBase<MagmaticBoilerBlockEntity> {
	public MagmaticBoilerMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
		this(windowId, ProxyUtils.getClientWorld(), data.readBlockPos(), inv, ProxyUtils.getClientPlayer());
	}

	public MagmaticBoilerMenu(int windowId, Level level, BlockPos pos, Inventory inventory, Player player) {
		super(SysteamsRegistry.Boilers.MAGMATIC.menu(), windowId, level, pos, inventory, player);
		BoilerBlockEntityBase blockEntity = (BoilerBlockEntityBase) level.getBlockEntity(pos);
        InvWrapperCoFH blockEntityInv = new InvWrapperCoFH(blockEntity.getItemInv());

        bindAugmentSlots(blockEntityInv, 0, blockEntity.augSize());
        bindPlayerInventory(inventory);
	}
}
