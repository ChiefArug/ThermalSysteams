package chiefarug.mods.systeams.block;

import chiefarug.mods.systeams.ConversionKitItem;
import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.core.block.TileBlockActive6Way;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static chiefarug.mods.systeams.SysteamsRegistry.Items.BOILER_PIPE;
import static chiefarug.mods.systeams.SysteamsRegistry.Items.RF_COIL;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.ACTIVE;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

// Most of this is copied from dynamo code
// I don't extend it because these are not dynamos and should not be treated as such
@Mod.EventBusSubscriber(modid= Systeams.MODID)
public class BoilerBlock extends TileBlockActive6Way {

	public BoilerBlock(Properties builder, Class<?> tileClass, Supplier<BlockEntityType<?>> blockEntityType) {
		super(builder, tileClass, blockEntityType);
		this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false).setValue(FACING_ALL, Direction.UP).setValue(WATERLOGGED, false));
	}

	private static final VoxelShape[] BASE_SHAPE = new VoxelShape[]{
			Block.box(0, 6, 0, 16, 16, 16),
			Block.box(0, 0, 0, 16, 10, 16),
			Block.box(0, 0, 6, 16, 16, 16),
			Block.box(0, 0, 0, 16, 16, 10),
			Block.box(6, 0, 0, 16, 16, 16),
			Block.box(0, 0, 0, 10, 16, 16)
	};

	private static final VoxelShape[] TUBE_SHAPE = new VoxelShape[]{
			Block.box(4, 0, 4, 12, 6, 12),
			Block.box(4, 10, 4, 12, 16, 12),
			Block.box(4, 4, 0, 12, 12, 6),
			Block.box(4, 4, 10, 12, 12, 16),
			Block.box(0, 4, 4, 6, 12, 12),
			Block.box(10, 4, 4, 16, 12, 12)
	};

	private static final VoxelShape[] BOILER_SHAPE = new VoxelShape[]{
			Shapes.or(BASE_SHAPE[0], TUBE_SHAPE[0]),
			Shapes.or(BASE_SHAPE[1], TUBE_SHAPE[1]),
			Shapes.or(BASE_SHAPE[2], TUBE_SHAPE[2]),
			Shapes.or(BASE_SHAPE[3], TUBE_SHAPE[3]),
			Shapes.or(BASE_SHAPE[4], TUBE_SHAPE[4]),
			Shapes.or(BASE_SHAPE[5], TUBE_SHAPE[5])
	};

	@NotNull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return BOILER_SHAPE[state.getValue(FACING_ALL).ordinal()];
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED);
	}


	@NotNull
	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
		return super.getStateForPlacement(context).setValue(WATERLOGGED, flag);
	}

	@SubscribeEvent // Block#use does not trigger when crouching, so use the event
	public static void use(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		BlockPos pos = event.getHitVec().getBlockPos();
		BlockState oldState = level.getBlockState(pos);
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();

		ItemStack item = player.getItemInHand(hand);
		if (item.getItem() != RF_COIL.get() || !player.isCrouching()) return;

		Block boiler = oldState.getBlock();
		@SuppressWarnings("SuspiciousMethodCalls") // I believe this is because its getting passed the wrong class type. it works tho
		Block dynamo = SysteamsRegistry.Items.BOILER_PIPE.get().dynamoBoilerMap.inverse().get(boiler);
		if (dynamo == null)
			return;

		BlockState newState = dynamo.defaultBlockState()
				.setValue(FACING_ALL, oldState.getValue(FACING_ALL))
				.setValue(WATERLOGGED, oldState.getValue(WATERLOGGED));
		ConversionKitItem.transformDynamoBoiler(pos, level, oldState, newState, player);

		if (!player.getAbilities().instabuild) {
			item.shrink(1);
			if (item.isEmpty())
				player.setItemInHand(hand, new ItemStack(BOILER_PIPE.get()));
			else
				player.addItem(new ItemStack(BOILER_PIPE.get()));
		}
		if (level.isClientSide())
			player.swing(hand);
		event.setCanceled(true);
	}
}
