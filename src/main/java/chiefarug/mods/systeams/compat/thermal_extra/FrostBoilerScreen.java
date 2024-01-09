package chiefarug.mods.systeams.compat.thermal_extra;

import chiefarug.mods.systeams.client.screens.ItemBoilerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FrostBoilerScreen extends ItemBoilerScreen<FrostBoilerMenu> {
    public FrostBoilerScreen(FrostBoilerMenu container, Inventory inv, Component titleIn) {
        super("frost", container, inv, titleIn);
    }
}
