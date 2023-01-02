package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.StirlingBoilerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StirlingBoilerScreen extends ItemBoilerScreen<StirlingBoilerContainer> {
	public StirlingBoilerScreen(StirlingBoilerContainer container, Inventory inv, Component titleIn) {
		super("stirling_boiler", container, inv, titleIn);
	}
}
