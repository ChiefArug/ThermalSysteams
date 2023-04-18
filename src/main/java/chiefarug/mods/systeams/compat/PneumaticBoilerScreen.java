package chiefarug.mods.systeams.compat;

import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.client.screens.BoilerScreenBase;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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
		this.renderables.add((poseStack, mouseX, mouseY, partialTick) -> PressureGaugeRenderer2D.drawPressureGauge(poseStack, font, 0, blockEntity.airHandler.maxPressure(), 20, 15, blockEntity.getPressure(), leftPos + 60, topPos + 44));
	}

	@Override
	protected int getFlameXOffset() {
		return 16;
	}
}
