package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.GourmandBoilerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GourmandBoilerScreen extends ItemBoilerScreen<GourmandBoilerMenu> {
	public GourmandBoilerScreen(GourmandBoilerMenu container, Inventory inv, Component titleIn) {
		super("gourmand", container, inv, titleIn);
	}
}
