package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.containers.SteamPulverizerContainer;
import cofh.core.util.helpers.GuiHelper;
import cofh.thermal.core.client.gui.ThermalGuiHelper;
import cofh.thermal.lib.client.gui.MachineScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static chiefarug.mods.systeams.Systeams.MODID;
import static cofh.core.util.helpers.GuiHelper.PROG_ARROW_RIGHT;
import static cofh.core.util.helpers.GuiHelper.SCALE_CRUSH;
import static cofh.core.util.helpers.GuiHelper.createInputSlot;
import static cofh.core.util.helpers.GuiHelper.createOutputSlot;
import static cofh.core.util.helpers.GuiHelper.generatePanelInfo;

public class SteamPulverizerScreen extends MachineScreen<SteamPulverizerContainer> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/gui/steam_pulverizer.png");

    public SteamPulverizerScreen(SteamPulverizerContainer container, Inventory inv, Component titleIn) {
        super(container, inv, container.blockEntity, titleIn);

        texture = TEXTURE;
        info = generatePanelInfo("info.thermal.machine_pulverizer");
        name = "pulverizer"; // this seems to not be displayed, only used for some config stuff.
    }

    @Override
    public void init() {
        super.init();

        addElement(createInputSlot(this, 44, 17, tile));
        addElement(createInputSlot(this, 44, 53, tile));

        addElement(createOutputSlot(this, 107, 26, tile));
        addElement(createOutputSlot(this, 125, 26, tile));
        addElement(createOutputSlot(this, 107, 44, tile));
        addElement(createOutputSlot(this, 125, 44, tile));

        addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 7, 28, tile.getTank(0)), tile, 0));
        //TODO: add widget for steam usage per tick
        addElement(ThermalGuiHelper.createDefaultProgress(this, 72, 35, PROG_ARROW_RIGHT, tile));
        addElement(ThermalGuiHelper.createDefaultSpeed(this, 44, 35, SCALE_CRUSH, tile));
    }
}
