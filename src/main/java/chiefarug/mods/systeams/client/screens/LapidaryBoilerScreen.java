package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.LapidaryBoilerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LapidaryBoilerScreen extends ItemBoilerScreen<LapidaryBoilerMenu> {
	public LapidaryBoilerScreen(LapidaryBoilerMenu container, Inventory inv, Component titleIn) {
		super("lapidary", container, inv, titleIn);
	}
}
