package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.MagmaticBoilerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MagmaticBoilerScreen extends FluidBoilerScreen<MagmaticBoilerContainer> {
	public MagmaticBoilerScreen(MagmaticBoilerContainer container, Inventory inv, Component titleIn) {
		super("magmatic", container, inv, titleIn);
	}
}
