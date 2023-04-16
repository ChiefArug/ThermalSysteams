package chiefarug.mods.systeams.compat;

import chiefarug.mods.systeams.client.screens.BoilerScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PneumaticBoilerScreen extends BoilerScreenBase<PneumaticBoilerContainer> {
	public PneumaticBoilerScreen(PneumaticBoilerContainer container, Inventory inv, Component titleIn) {
		super("pneumatic", container, inv, container.blockEntity, titleIn);
	}
}
