package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.DisenchantmentBoilerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DisenchantmentBoilerScreen extends ItemBoilerScreen<DisenchantmentBoilerMenu> {
	public DisenchantmentBoilerScreen(DisenchantmentBoilerMenu container, Inventory inv, Component titleIn) {
		super("disenchantment", container, inv, titleIn);
	}
}
