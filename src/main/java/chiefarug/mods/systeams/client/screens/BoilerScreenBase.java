package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import cofh.core.inventory.container.ContainerCoFH;
import cofh.core.util.helpers.GuiHelper;
import cofh.lib.util.constants.ModIds;
import cofh.thermal.core.client.gui.ThermalGuiHelper;
import cofh.thermal.lib.client.gui.ThermalTileScreenBase;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreenBase<T extends ContainerCoFH> extends ThermalTileScreenBase<T> {

	protected static final ResourceLocation ITEM_TEXTURE = new ResourceLocation(ModIds.ID_THERMAL, "textures/gui/container/item_dynamo.png");
	protected static final ResourceLocation FLUID_TEXTURE = new ResourceLocation(ModIds.ID_THERMAL, "textures/gui/container/fluid_dynamo.png");

	protected BoilerBlockEntityBase blockEntity;

	public BoilerScreenBase(T container, Inventory inv, BoilerBlockEntityBase blockEntity, Component titleIn) {
		super(container, inv, blockEntity, titleIn);
		this.blockEntity = blockEntity;
	}

	@Override
	public void init() {
		super.init();
		addElement(ThermalGuiHelper.createDefaultDuration(this, 80, 35, GuiHelper.SCALE_FLAME, tile)); //TODO mess with these placements
		addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 10, 22, blockEntity.waterTank), blockEntity, 0));
		addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 128, 22, blockEntity.steamTank), blockEntity, 1));
	}
}
