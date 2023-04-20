package chiefarug.mods.systeams.compat;

import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.client.screens.BoilerScreenBase;
import cofh.core.client.gui.IGuiAccess;
import cofh.core.client.gui.element.panel.PanelBase;
import cofh.core.client.gui.element.panel.ResourcePanel;
import cofh.core.util.helpers.RenderHelper;
import cofh.lib.util.helpers.StringHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.IntSupplier;

public class PneumaticBoilerScreen extends BoilerScreenBase<PneumaticBoilerContainer> {

	protected static final ResourceLocation PRESSURE_TEXTURE = new ResourceLocation(Systeams.MODID, "textures/gui/pressure_boiler.png");
	protected PneumaticBoilerBlockEntity blockEntity;

	public PneumaticBoilerScreen(PneumaticBoilerContainer container, Inventory inv, Component titleIn) {
		super("pneumatic", container, inv, container.blockEntity, titleIn);
		texture = PRESSURE_TEXTURE;
		blockEntity = container.blockEntity;
	}

	@Override // this is so it renders after the background but before tooltips
	public void init() {
		super.init();
		this.renderables.add((poseStack, mouseX, mouseY, partialTick) -> PressureGaugeRenderer2D.drawPressureGauge(poseStack, font, 0, 25, 20, 15, blockEntity.getPressure(), leftPos + 60, topPos + 44));
		addPanel(airPanel());
	}

	private ResourcePanel airPanel() {
		return new AirResourcePanel(this)
				.setAirPerTick(blockEntity::getAirPerTick)
				.setResource(SysteamsPNCRCompat.Registry.Client.AIR_ICON, "info.systeams.air", false)
				.setCurrent(blockEntity::getAir, "info.systeams.air_volume", "info.systeams.air_volume_unit")
				.setMax(blockEntity::getVolume, "info.systeams.air_base_volume", "info.systeams.air_volume_unit");
	}

	protected class AirResourcePanel extends ResourcePanel {

		public IntSupplier doubleSupplier;

		protected AirResourcePanel(IGuiAccess gui) {
			super(gui, PanelBase.RIGHT);
		}

		public AirResourcePanel setAirPerTick(IntSupplier intSupplier) {
			this.doubleSupplier = intSupplier;
			return this;
		}

		@Override
		protected void drawForeground(PoseStack matrixStack) {
			super.drawForeground(matrixStack);
			if (!fullyOpen) {
				return;
			}
			if (doubleSupplier != null) {
				fontRenderer().drawShadow(matrixStack, StringHelper.localize("info.systeams.air_per_tick") + ":", sideOffset() + 6, 66, subheaderColor);
				fontRenderer().draw(matrixStack, doubleSupplier.getAsInt() + " " + StringHelper.localize("info.systeams.air_per_tick_unit"), sideOffset() + 14, 78, textColor);
			}
			RenderHelper.resetShaderColor();
		}
	}

	@Override
	protected int getFlameXOffset() {
		return 16;
	}
}
