package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.containers.BoilerContainerBase;
import cofh.core.util.helpers.GuiHelper;
import cofh.thermal.core.client.gui.ThermalGuiHelper;
import cofh.thermal.lib.client.gui.AugmentableScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreenBase<T extends BoilerContainerBase<?>> extends AugmentableScreen<T> {

	protected BoilerBlockEntityBase blockEntity;

	public BoilerScreenBase(String id, T container, Inventory inv, BoilerBlockEntityBase blockEntity, Component titleIn) {
		super(container, inv, blockEntity, titleIn);
		this.blockEntity = blockEntity;
		info = GuiHelper.appendLine(GuiHelper.generatePanelInfo("info.systeams." + id + "_boiler"), "info.systeams.boiler.throttle");
	}

	@Override
	public void init() {
		super.init();
		addElement(ThermalGuiHelper.createDefaultDuration(this, 80, 35, GuiHelper.SCALE_FLAME, tile));
		addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 10, 22, blockEntity.waterTank), blockEntity, 0));
		addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 128, 22, blockEntity.steamTank), blockEntity, 1));
	}
}
