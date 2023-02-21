package chiefarug.mods.systeams.fluid;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.lib.fluid.FluidCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static chiefarug.mods.systeams.Systeams.MODID;
import static net.minecraft.world.item.Items.BUCKET;

public class GrassolineFluid extends FluidCoFH {

	public final RegistryObject<FluidType> type;

	public GrassolineFluid(DeferredRegisterCoFH<FluidType> fluidTypeRegister, DeferredRegisterCoFH<Fluid> fluidRegister, DeferredRegisterCoFH<Block> blockRegister, DeferredRegisterCoFH<Item> itemRegister, String id) {
		super(fluidRegister, id);
		type = fluidTypeRegister.register(id, () -> new FluidType(FluidType.Properties.create()
						.canDrown(true)
						.canHydrate(true)
						.density(900)
						.canPushEntity(true)
						.motionScale(0.005)
						.viscosity(2500)) {
					@Override
					public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
						consumer.accept(new IClientFluidTypeExtensions() {
							private static final ResourceLocation STILL = new ResourceLocation(MODID, "block/grassoline_still");
							private static final ResourceLocation FLOW = new ResourceLocation(MODID, "block/grassoline_flow");

							private static final Vector3f particleColor = new Vector3f(0.1f, 1.0f, 0.1f);

							@Override
							public ResourceLocation getStillTexture() {
								return STILL;
							}

							@Override
							public ResourceLocation getFlowingTexture() {
								return FLOW;
							}

							@Override
							public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
								return particleColor;
							}
						});
					}
				}
		);
		bucket = itemRegister.register(bucket(id), () -> new BucketItem(this.still(), new Item.Properties().tab(SysteamsRegistry.TAB).craftRemainder(BUCKET).stacksTo(1)));
		block = blockRegister.register(id, () -> new LiquidBlock(still(), BlockBehaviour.Properties.of(Material.WATER).noCollission().noLootTable()));
	}

	@Override
	protected Supplier<FluidType> type() {
		return type;
	}

	@Override
	protected ForgeFlowingFluid.Properties fluidProperties() {
		return new ForgeFlowingFluid.Properties(type(), stillFluid, flowingFluid).block(block).bucket(bucket).levelDecreasePerBlock(2);
	}
}
