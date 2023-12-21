package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.block_entities.LapidaryBoilerBlockEntity;
import cofh.core.util.ProxyUtils;
import cofh.lib.common.inventory.SlotCoFH;
import cofh.lib.common.inventory.wrapper.InvWrapperCoFH;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LapidaryBoilerMenu extends BoilerMenuBase<LapidaryBoilerBlockEntity> {

	public LapidaryBoilerMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
		this(windowId, ProxyUtils.getClientWorld(), data.readBlockPos(), inv, ProxyUtils.getClientPlayer());
	}

	public LapidaryBoilerMenu(int windowId, Level level, BlockPos pos, Inventory inventory, Player player) {
		super(SysteamsRegistry.Boilers.LAPIDARY.menu(), windowId, level, pos, inventory, player);
		BoilerBlockEntityBase blockEntity = (BoilerBlockEntityBase) level.getBlockEntity(pos);
        InvWrapperCoFH blockEntityInv = new InvWrapperCoFH(blockEntity.getItemInv());

		addSlot(new SlotCoFH(blockEntityInv, 0, 44, 35));

        bindAugmentSlots(blockEntityInv, 1, blockEntity.augSize());
        bindPlayerInventory(inventory);
	}
}
