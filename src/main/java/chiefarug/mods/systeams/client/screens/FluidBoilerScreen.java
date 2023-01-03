package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.block_entities.FluidBoilerBlockEntityBase;
import chiefarug.mods.systeams.containers.BoilerContainerBase;
import cofh.core.util.helpers.GuiHelper;
import cofh.lib.util.constants.ModIds;
import cofh.thermal.core.client.gui.ThermalGuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static cofh.core.util.helpers.GuiHelper.SCALE_FLAME;

public class FluidBoilerScreen<T extends BoilerContainerBase<?>> extends BoilerScreenBase<T>{

	protected static final ResourceLocation FLUID_TEXTURE = new ResourceLocation(ModIds.ID_THERMAL, "textures/gui/container/fluid_dynamo.png");

	public FluidBoilerScreen(String id, T container, Inventory inv, Component titleIn) {
		super(container, inv, container.blockEntity, titleIn);
		texture = FLUID_TEXTURE;
		info = GuiHelper.generatePanelInfo("info.systeams." + id);
	}

	@Override
	public void init() {
		super.init();
		addElement(GuiHelper.setClearable(GuiHelper.createSmallFluidStorage(this, 34, 26, ((FluidBoilerBlockEntityBase) tile).getFuelTank()), tile, 0));
        addElement(ThermalGuiHelper.createDefaultDuration(this, 80, 35, SCALE_FLAME, tile));
	}
}
