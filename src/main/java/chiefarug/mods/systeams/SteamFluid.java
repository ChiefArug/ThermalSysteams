package chiefarug.mods.systeams;

import cofh.lib.fluid.FluidCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SteamFluid /*extends FluidCoFH*/ { // We can't extend FluidCoFH because it forces you to use the base ForgeFlowingFluid, meaning I can't make it flow upwards.

	public final RegistryObject<FluidType> type;
	public final Vector3f particleColor =  new Vector3f(0.2F, 0.2F, 0.2F);
	public final RegistryObject<ForgeFlowingFluid> stillFluid;
	public final RegistryObject<ForgeFlowingFluid> flowingFluid;

	private static final BlockBehaviour.Properties fluidBlockProperties = BlockBehaviour.Properties.of(
					new Material.Builder(MaterialColor.COLOR_LIGHT_GRAY)
							.noCollider()
							.notSolidBlocking()
							.nonSolid()
							.destroyOnPush()
							.replaceable()
							.liquid()
							.build(),
					MaterialColor.COLOR_LIGHT_GRAY
				);
	private static final Item.Properties bucketItemProperties = new Item.Properties().tab(SysteamsRegistry.TAB).craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1);

    protected final RegistryObject<LiquidBlock> block;
    protected final RegistryObject<Item> bucket;

	public SteamFluid(DeferredRegisterCoFH<Fluid> fluidRegister, DeferredRegisterCoFH<FluidType> typeRegister, DeferredRegisterCoFH<Block> blockRegister, DeferredRegisterCoFH<Item> itemRegister, String id) {
		stillFluid = fluidRegister.register(id, () -> new Source(fluidProperties()));
        flowingFluid = fluidRegister.register(FluidCoFH.flowing(id), () -> new Flowing(fluidProperties()));

		block = blockRegister.register(id + "_fluid", () -> new SteamLiquidBlock(stillFluid, fluidBlockProperties));
		bucket = itemRegister.register(id + "_bucket", () -> new BucketItem(stillFluid, bucketItemProperties));


		type = typeRegister.register(id, () -> new FluidType(FluidType.Properties.create()
			.canDrown(true)
			.canExtinguish(true)
			.canHydrate(true)
			.density(1)
			.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
			.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)) {
			@Override
				public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
					consumer.accept(new IClientFluidTypeExtensions() {
						private static final ResourceLocation
								STILL = new ResourceLocation("systeams:block/steam_still"),
								FLOW = new ResourceLocation("systeams:block/steam_flow");

						@Override
						public ResourceLocation getStillTexture() {
							return STILL;
						}

						@Override
						public ResourceLocation getFlowingTexture() {
							return FLOW;
						}

						@Override
						public ResourceLocation getOverlayTexture() {
							return FluidCoFH.WATER_OVERLAY;
						}

						@Override
						public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
							return FluidCoFH.UNDERWATER_LOCATION;
						}

						@Override
						public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
							return particleColor;
						}

						@Override
						public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
							nearDistance = -8F;
							farDistance = 8F;

							if (farDistance > renderDistance) {
								farDistance = renderDistance;
								shape = FogShape.CYLINDER;
							}

							RenderSystem.setShaderFogStart(nearDistance);
							RenderSystem.setShaderFogEnd(farDistance);
							RenderSystem.setShaderFogShape(shape);
						}

					});
				}
				}
		);
	}

    protected ForgeFlowingFluid.Properties fluidProperties() {
        return new ForgeFlowingFluid.Properties(type(), stillFluid, flowingFluid).block(block).bucket(bucket).tickRate(2).slopeFindDistance(1);
    }

	protected Supplier<FluidType> type() {
		return type;
	}

	public abstract class SteamFlowingFluid extends ForgeFlowingFluid {
		public SteamFlowingFluid(Properties properties) {
			super(properties);
		}

		@Override
		protected boolean isWaterHole(BlockGetter pLevel, Fluid pFluid, BlockPos p_75959_, BlockState p_75960_, BlockPos p_75961_, BlockState p_75962_) {
			if (!this.canPassThroughWall(Direction.UP, pLevel, p_75959_, p_75960_, p_75961_, p_75962_)) {
				return false;
			} else {
				return p_75962_.getFluidState().getType().isSame(this) || this.canHoldFluid(pLevel, p_75961_, p_75962_, pFluid);
			}
		}

		@Override
		protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction) {
			return direction == Direction.UP && !this.isSame(fluidIn);
		}

		@Override
		public void tick(Level level, BlockPos pos, FluidState currentFluidState) {
			BlockState currentBlockState = level.getBlockState(pos);
			if (this.isSource(currentFluidState)) {
				// If its not hot enough then we can just turn to water
				if (isTooColdForSteam(level)) {
					level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
					level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.9F, level.getRandom().nextFloat());
					return; // stop trying anything else
				}

				// else we should flow.
				// steam is special and will flow its source upwards until it reaches a block, and only then will it spread out (this is mainly to prevent massive leaks of liquid)
				Direction flowDir = Direction.UP;
				BlockPos flowToPos = pos.relative(flowDir);
				boolean canFlowUp = this.canSpreadTo(level, pos, currentBlockState, flowDir, flowToPos, level.getBlockState(flowToPos), level.getFluidState(flowToPos), SteamFluid.this.stillFluid.get());
//				level.setBlock(pos.above(), Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
				if (canFlowUp) {
					int flags = 3;
					// if we aren't flowing into our fluid, prevent light updates. This prevents major lag
					if (level.getFluidState(flowToPos).getType() != SteamFluid.this.flowingFluid.get()) {
						flags |= 128;
					}
					level.setBlock(flowToPos, currentFluidState.createLegacyBlock(), flags);
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), flags);
					return;
				}
				this.spreadToSides(level, pos, currentFluidState, currentBlockState);
				return;
			}
			// we are a flowing block
//			level.setBlock(pos.above(), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
			int maxSurroundingLevel = 0;
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				FluidState stateInDir = level.getFluidState(pos.relative(dir));
				if (stateInDir.getType() == SteamFluid.this.stillFluid.get()) {
					// we are at max. no need to continue checking (unless we want to fill into a source)
					maxSurroundingLevel = 8;
					break;
				}
				if (stateInDir.getType() != SteamFluid.this.flowingFluid.get())
					continue; // skip the block if it's not our fluid
				maxSurroundingLevel = Math.max(maxSurroundingLevel, stateInDir.getValue(LEVEL));
			}
			maxSurroundingLevel -= this.getDropOff(level);
			level.setBlock(pos, maxSurroundingLevel > 0 ? this.getFlowing(maxSurroundingLevel, false).createLegacyBlock() : Blocks.AIR.defaultBlockState(), 3);
			this.spreadToSides(level, pos, currentFluidState, currentBlockState);
//			if (!currentFluidState.isSource()) {
//				int currentLevel = currentFluidState.getValue(LEVEL);
//				// haha special casing go brrrrrr
//				if (currentLevel < 8) {
//					level.setBlock(pos, currentLevel > 1 ? currentFluidState.setValue(LEVEL, currentLevel - 1).createLegacyBlock() : Blocks.AIR.defaultBlockState(), 3);
//					return;
//				}
//				FluidState newFluidstate = this.getNewLiquid(level, pos, level.getBlockState(pos));
//				int i = this.getSpreadDelay(level, pos, currentFluidState, newFluidstate);
//				if (newFluidstate.isEmpty()) {
//					currentFluidState = newFluidstate;
//					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
//				} else if (!newFluidstate.equals(currentFluidState)) {
//					currentFluidState = newFluidstate;
//					BlockState blockstate = newFluidstate.createLegacyBlock();
//					level.setBlock(pos, blockstate, 2);
//					level.scheduleTick(pos, newFluidstate.getType(), i);
//					level.updateNeighborsAt(pos, blockstate.getBlock());
//				}
//			}
//
//			this.spread(level, pos, currentFluidState);
		}

		@Override
		protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState, Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
			var fromFluidState = level.getFluidState(fromPos);
			// if its flowing horizontally and the block above the toPos is an empty block, then do not flow

			if (Direction.Plane.HORIZONTAL == direction.getAxis().getPlane() &&
					level.getBlockState(toPos.above()).isAir()) {
				// prevents large flooding of areas.
				return false;
			} else if (direction == Direction.UP && toFluidState.getType() == SteamFluid.this.flowingFluid.get()) { // if its trying to flow up into flowing fluid, let it
				return true;
			} else {
				return super.canSpreadTo(level, fromPos, fromBlockState, direction, toPos, toBlockState, toFluidState, fluid);
			}
		}


		private boolean isTooColdForSteam(Level level) {
			return false;
//			return !level.dimensionType().ultraWarm();
		}


	}

	public class Flowing extends SteamFlowingFluid {
		public Flowing(Properties properties) {
			super(properties);
			registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
		}

		protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public class Source extends SteamFlowingFluid {
		public Source(Properties properties) {
			super(properties);
		}

		@Override
		public boolean isSource(FluidState pState) {
			return true;
		}

		@Override
		public int getAmount(FluidState pState) {
			return 8;
		}
	}

	public static class SteamLiquidBlock extends LiquidBlock {
		public SteamLiquidBlock(Supplier<? extends FlowingFluid> p_54694_, Properties p_54695_) {
			super(p_54694_, p_54695_);
		}

	}
}
