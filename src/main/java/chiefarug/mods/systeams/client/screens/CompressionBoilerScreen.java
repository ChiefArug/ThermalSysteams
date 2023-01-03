package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.CompressionBoilerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompressionBoilerScreen extends FluidBoilerScreen<CompressionBoilerContainer> {
	public CompressionBoilerScreen(CompressionBoilerContainer container, Inventory inv, Component titleIn) {
		super("compression", container, inv, titleIn);
	}
}
