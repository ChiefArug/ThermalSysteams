package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.BoilerContainerBase;
import cofh.core.util.helpers.GuiHelper;
import cofh.lib.util.constants.ModIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemBoilerScreen<T extends BoilerContainerBase<?>> extends BoilerScreenBase<T> {

	protected static final ResourceLocation ITEM_TEXTURE = new ResourceLocation(ModIds.ID_THERMAL, "textures/gui/container/item_dynamo.png");

	public ItemBoilerScreen(String id, T container, Inventory inv, Component titleIn) {
		super(container, inv, container.blockEntity, titleIn);
		texture = ITEM_TEXTURE;
		info = GuiHelper.generatePanelInfo("info.systeams." + id);
	}
}
