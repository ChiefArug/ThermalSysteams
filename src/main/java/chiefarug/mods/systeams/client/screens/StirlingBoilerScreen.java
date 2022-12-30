package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.StirlingBoilerContainer;
import cofh.core.util.helpers.GuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StirlingBoilerScreen extends BoilerScreenBase<StirlingBoilerContainer> {

	public StirlingBoilerScreen(StirlingBoilerContainer container, Inventory inv, Component titleIn) {
		super(container, inv, container.blockEntity, titleIn);
		texture = ITEM_TEXTURE;
		info = GuiHelper.generatePanelInfo("info.systeams.stirling_boiler");
	}

	@Override
    public void init() {
        super.init();
    }
}
