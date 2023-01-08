package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.GourmandBoilerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GourmandBoilerScreen extends ItemBoilerScreen<GourmandBoilerContainer> {
	public GourmandBoilerScreen(GourmandBoilerContainer container, Inventory inv, Component titleIn) {
		super("gourmand", container, inv, titleIn);
	}
}
