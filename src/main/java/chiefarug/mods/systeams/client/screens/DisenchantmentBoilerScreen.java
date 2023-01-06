package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.DisenchantmentBoilerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DisenchantmentBoilerScreen extends ItemBoilerScreen<DisenchantmentBoilerContainer> {
	public DisenchantmentBoilerScreen(DisenchantmentBoilerContainer container, Inventory inv, Component titleIn) {
		super("disenchantment", container, inv, titleIn);
	}
}
