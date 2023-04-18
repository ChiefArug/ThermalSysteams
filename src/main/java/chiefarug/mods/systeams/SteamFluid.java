package chiefarug.mods.systeams;

import cofh.lib.fluid.FluidCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import com.mojang.math.Vector3f;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.systeams.Systeams.LGGR;
@SuppressWarnings("unused")// ignore unused code, its probably for the block form.
public class SteamFluid /*extends FluidCoFH*/ { // We can't extend FluidCoFH because it forces you to use the base ForgeFlowingFluid, meaning I can't make it flow upwards.

	public final Vector3f particleColor = new Vector3f(0.1F, 0.1F, 0.1F);
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

//	protected final RegistryObject<LiquidBlock> block;
	protected final RegistryObject<Item> bucket;

	public SteamFluid(DeferredRegisterCoFH<Fluid> fluidRegister, DeferredRegisterCoFH<Block> blockRegister, DeferredRegisterCoFH<Item> itemRegister, String id) {
		stillFluid = fluidRegister.register(id, () -> new Source(fluidProperties()));
		flowingFluid = fluidRegister.register(FluidCoFH.flowing(id), () -> new Flowing(fluidProperties()));

//		block = blockRegister.register(id + "_fluid", () -> new SteamLiquidBlock(stillFluid, fluidBlockProperties));
		bucket = itemRegister.register(id + "_bucket", () -> new BucketItem(stillFluid, bucketItemProperties));

	}

	private static final ResourceLocation STILL = new ResourceLocation("systeams:block/steam_still");
	private static final ResourceLocation FLOW = new ResourceLocation("systeams:block/steam_flow");

	protected ForgeFlowingFluid.Properties fluidProperties() {
		return new ForgeFlowingFluid.Properties(stillFluid, flowingFluid, FluidAttributes.builder(STILL, FLOW));
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

		public static boolean warnOnFlow = true;
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
				if (canFlowUp) {
					int flags = 3;
					// if we aren't flowing into our fluid, prevent light updates. This tries to prevent major lag
					if (level.getFluidState(flowToPos).getType() != SteamFluid.this.flowingFluid.get()) {
						flags |= 128;
					}
					level.setBlock(flowToPos, currentFluidState.createLegacyBlock(), flags);
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), flags);
					if (pos.getY() > 100 && warnOnFlow) {
						LGGR.warn("A steam block flowed above y 100! If you are experiencing major lag spikes, this is likely the cause (silly lighting updates)");
						LGGR.warn("Also.. how on earth did you place a steam fluid block?? These aren't even registered!");
					}
					return;
				}
				this.spreadToSides(level, pos, currentFluidState, currentBlockState);
				return;
			}
			// we are a flowing block
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
		}

		@Override
		protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState, Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
			// if its flowing horizontally and the block above the toPos is an empty block, then do not flow
			// prevents large flooding of areas.
			if (Direction.Plane.HORIZONTAL == direction.getAxis().getPlane() && level.getBlockState(toPos.above()).isAir())
				return false;
				// if it's trying to flow up into its own flowing fluid, let it
			else if (direction == Direction.UP && toFluidState.getType() == SteamFluid.this.flowingFluid.get())
				return true;
			else
				return super.canSpreadTo(level, fromPos, fromBlockState, direction, toPos, toBlockState, toFluidState, fluid);

		}

		private boolean isTooColdForSteam(Level level) {
//			return false;
			return !level.dimensionType().ultraWarm();
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

	public Fluid getStill() {
		return stillFluid.get();
	}

	public Fluid getFlowing() {
		return flowingFluid.get();
	}
}
