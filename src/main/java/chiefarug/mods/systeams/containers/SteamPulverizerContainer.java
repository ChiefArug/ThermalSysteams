package chiefarug.mods.systeams.containers;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.block_entities.SteamPulverizerBlockEntity;
import cofh.core.inventory.container.TileCoFHContainer;
import cofh.core.util.ProxyUtils;
import cofh.lib.inventory.container.slot.SlotCoFH;
import cofh.lib.inventory.container.slot.SlotRemoveOnly;
import cofh.lib.inventory.wrapper.InvWrapperCoFH;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SteamPulverizerContainer extends TileCoFHContainer {
    public final SteamPulverizerBlockEntity blockEntity;

    public SteamPulverizerContainer(int windowId, Level level, BlockPos pos, Inventory inventory, Player player) {
        super(SysteamsRegistry.SteamMachines.PULVERIZER.menu(), windowId, level, pos, inventory, player);
        this.blockEntity = (SteamPulverizerBlockEntity) level.getBlockEntity(pos);

        InvWrapperCoFH tileInv = new InvWrapperCoFH(this.blockEntity.getItemInv());
        // input and catalyst
        addSlot(new SlotCoFH(tileInv, 0, 44, 17));
        addSlot(new SlotCoFH(tileInv, 1, 44, 53));

        addSlot(new SlotRemoveOnly(tileInv, 2, 107, 26));
        addSlot(new SlotRemoveOnly(tileInv, 3, 125, 26));
        addSlot(new SlotRemoveOnly(tileInv, 4, 107, 44));
        addSlot(new SlotRemoveOnly(tileInv, 5, 125, 44));
        // fluid 'charge'
        addSlot(new SlotCoFH(tileInv, 6, 8, 8));

        bindAugmentSlots(tileInv, 7, this.blockEntity.augSize());
        bindPlayerInventory(inventory);
    }

    public SteamPulverizerContainer(int id, Inventory inv, FriendlyByteBuf byteBuf) {
        this(id, ProxyUtils.getClientWorld(), byteBuf.readBlockPos(), inv, ProxyUtils.getClientPlayer());
    }
}
