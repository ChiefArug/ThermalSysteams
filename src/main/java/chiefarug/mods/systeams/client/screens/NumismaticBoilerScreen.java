package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.NumismaticBoilerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NumismaticBoilerScreen extends ItemBoilerScreen<NumismaticBoilerMenu> {
	public NumismaticBoilerScreen(NumismaticBoilerMenu container, Inventory inv, Component titleIn) {
		super("numismatic", container, inv, titleIn);
	}
}
