package chiefarug.mods.systeams.compat.pneumaticcraft;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.containers.BoilerMenuBase;
import cofh.core.util.ProxyUtils;
import cofh.lib.common.inventory.wrapper.InvWrapperCoFH;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PneumaticBoilerMenu extends BoilerMenuBase<PneumaticBoilerBlockEntity> {
	public PneumaticBoilerMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
		this(windowId, ProxyUtils.getClientWorld(), data.readBlockPos(), inv, ProxyUtils.getClientPlayer());
	}

	public PneumaticBoilerMenu(int windowId, Level level, BlockPos pos, Inventory inventory, Player player) {
		super(SysteamsPNCRCompat.Registry.PNEUMATIC_BOILER_MENU.get(), windowId, level, pos, inventory, player);
		BoilerBlockEntityBase blockEntity = (BoilerBlockEntityBase) level.getBlockEntity(pos);
        InvWrapperCoFH blockEntityInv = new InvWrapperCoFH(blockEntity.getItemInv());

        bindAugmentSlots(blockEntityInv, 0, blockEntity.augSize());
        bindPlayerInventory(inventory);
	}
}
