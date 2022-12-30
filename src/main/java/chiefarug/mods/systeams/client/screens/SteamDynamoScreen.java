package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.SteamDynamoContainer;
import cofh.core.util.helpers.GuiHelper;
import cofh.lib.util.constants.ModIds;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermal.core.client.gui.ThermalGuiHelper;
import cofh.thermal.lib.client.gui.DynamoScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SteamDynamoScreen extends DynamoScreenBase<SteamDynamoContainer> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(ModIds.ID_THERMAL, "textures/gui/container/fluid_dynamo.png");

	public SteamDynamoScreen(SteamDynamoContainer container, Inventory inv, Component titleIn) {
		super(container, inv, container.blockEntity, StringHelper.getTextComponent("block.systeams.steam_dynamo"));
        texture = TEXTURE;
        info = GuiHelper.appendLine(GuiHelper.generatePanelInfo("info.systeams.steam_dynamo"), "info.thermal.dynamo.throttle");
	}

	@Override
    public void init() {
        super.init();
		addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 34, 22, tile.getTank(0)), tile, 0));
        addElement(ThermalGuiHelper.createDefaultDuration(this, 80, 35, GuiHelper.SCALE_FLAME, tile));
    }
}
