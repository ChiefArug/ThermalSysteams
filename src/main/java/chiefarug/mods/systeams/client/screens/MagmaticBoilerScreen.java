package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.MagmaticBoilerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MagmaticBoilerScreen extends FluidBoilerScreen<MagmaticBoilerMenu> {
	public MagmaticBoilerScreen(MagmaticBoilerMenu container, Inventory inv, Component titleIn) {
		super("magmatic", container, inv, titleIn);
	}
}
