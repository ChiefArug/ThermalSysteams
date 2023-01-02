package chiefarug.mods.systeams.client.screens;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.containers.BoilerContainerBase;
import cofh.lib.util.constants.ModIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluidBoilerScreen<T extends BoilerContainerBase<?>> extends BoilerScreenBase<T>{

	protected static final ResourceLocation FLUID_TEXTURE = new ResourceLocation(ModIds.ID_THERMAL, "textures/gui/container/fluid_dynamo.png");

	public FluidBoilerScreen(String id, T container, Inventory inv, BoilerBlockEntityBase blockEntity, Component titleIn) {
		super(container, inv, blockEntity, titleIn);
		texture = FLUID_TEXTURE;
	}

	@Override
	public void init() {
		super.init();
	}
}
