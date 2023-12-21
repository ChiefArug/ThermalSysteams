package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.StirlingBoilerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StirlingBoilerScreen extends ItemBoilerScreen<StirlingBoilerMenu> {
	public StirlingBoilerScreen(StirlingBoilerMenu container, Inventory inv, Component titleIn) {
		super("stirling", container, inv, titleIn);
	}
}
