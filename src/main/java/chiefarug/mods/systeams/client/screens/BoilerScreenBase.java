package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.containers.BoilerMenuBase;
import cofh.core.client.gui.element.ElementItem;
import cofh.core.client.gui.element.panel.ResourcePanel;
import cofh.core.util.helpers.GuiHelper;
import cofh.thermal.core.client.gui.ThermalGuiHelper;
import cofh.thermal.lib.client.gui.AugmentableTileScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class BoilerScreenBase<T extends BoilerMenuBase<?>> extends AugmentableTileScreen<T> {

    protected BoilerBlockEntityBase blockEntity;

    public BoilerScreenBase(String id, T container, Inventory inv, BoilerBlockEntityBase blockEntity, Component titleIn) {
        super(container, inv, blockEntity, titleIn);
        this.blockEntity = blockEntity;
        info = GuiHelper.appendLine(GuiHelper.generatePanelInfo("info.systeams." + id + "_boiler"), "info.systeams.boiler.throttle");
    }

    protected int getFlameXOffset() {
        return 0;
    }

    @Override
    public void init() {
        super.init();

        addPanel(new ResourcePanel(this)
                .setResource(GuiHelper.ICON_STEAM, "info.systeams.steam", true)
                .setEfficiency(tile::getEfficiency)
                .setCurrent(tile::getCurSpeed, "info.systeams.steam_prod", "info.cofh.unit_mb_t")
                .setMax(blockEntity::getWaterConsumption, "info.systeams.water_cons", "info.cofh.unit_mb_t")
        );


        addElement(ThermalGuiHelper.createDefaultDuration(this, 80 + getFlameXOffset(), 35, GuiHelper.SCALE_FLAME, tile));
        addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 10, 22, blockEntity.waterTank), blockEntity, 0));
        addElement(GuiHelper.setClearable(GuiHelper.createMediumFluidStorage(this, 128, 22, blockEntity.steamTank), blockEntity, 1));

        addElement(new ElementItem(this, 154, 4)
                .setItem(() -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(Systeams.MEKANISM, "basic_pressurized_tube")).getDefaultInstance())
                .setTooltipFactory((element, mouseX, mouseY) -> List.of(
                        Component.translatable("info.systeams.mekanism_gas_output.1"),
                        Component.translatable("info.systeams.mekanism_gas_output.2"),
                        Component.translatable("info.systeams.mekanism_gas_output.3")
                ))
                .setVisible(() -> blockEntity.gasMode)
                .setSize(16, 16)
        );
    }
}
