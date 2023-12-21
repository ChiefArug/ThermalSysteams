package chiefarug.mods.systeams;

import chiefarug.mods.systeams.block.BoilerBlock;
import cofh.thermal.core.ThermalCore;
import cofh.thermal.lib.common.block.DynamoBlock;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.COMPRESSION;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.DISENCHANTMENT;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.GOURMAND;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.LAPIDARY;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.MAGMATIC;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.NUMISMATIC;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.STIRLING;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.thermal.lib.common.block.DynamoBlock.WATERLOGGED;

public class ConversionKitItem extends Item {
	public ConversionKitItem(Properties pProperties) {
		super(pProperties);
	}

	public static final BiMap<Block, BoilerBlock> dynamoBoilerMap = HashBiMap.create(7);

	public static void fillDynamoMap() {
		dynamoBoilerMap.put(getDynamo("stirling"), STIRLING.block());
		dynamoBoilerMap.put(getDynamo("compression"), COMPRESSION.block());
		dynamoBoilerMap.put(getDynamo("magmatic"), MAGMATIC.block());
		dynamoBoilerMap.put(getDynamo("numismatic"), NUMISMATIC.block());
		dynamoBoilerMap.put(getDynamo("lapidary"), LAPIDARY.block());
		dynamoBoilerMap.put(getDynamo("disenchantment"), DISENCHANTMENT.block());
		dynamoBoilerMap.put(getDynamo("gourmand"), GOURMAND.block());
	}

	public static BiMap<Block, BoilerBlock> getDynamoBoilerMap() {
		return dynamoBoilerMap;
	}

	private static DynamoBlock getDynamo(String id) {
		return (DynamoBlock) ThermalCore.BLOCKS.get("thermal:dynamo_" + id);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		BlockState oldState = level.getBlockState(pos);
		ItemStack stack = context.getItemInHand();
		InteractionHand hand = context.getHand();

		Block dynamo = oldState.getBlock();
		BoilerBlock boiler = dynamoBoilerMap.get(dynamo);
		if (boiler == null)
			return super.useOn(context);

		Player player = context.getPlayer();
		BlockState newState = boiler.defaultBlockState()
				.setValue(FACING_ALL, oldState.getValue(FACING_ALL))
				.setValue(WATERLOGGED, oldState.getValue(WATERLOGGED));
		transformDynamoBoiler(pos, level, oldState, newState, player);

		// if we have a player, replace with a coil. otherwise just shrink the itemstack
		if (player != null) {
			if (!player.getAbilities().instabuild) {
				ItemStack converstionReturn = boiler.getOtherConversionItem();
				stack.shrink(1);
				if (stack.isEmpty())
					player.setItemInHand(hand, converstionReturn);
				else
					player.addItem(converstionReturn);
			}
		} else {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
	}

	public static void transformDynamoBoiler(BlockPos pos, Level level, BlockState oldState, BlockState newState, @Nullable Player player) {
		List<TransferData> transferData = new ArrayList<>();

		if (!level.isClientSide()) {
			BlockEntity oldBE = level.getBlockEntity(pos);
			takeContents(oldBE, transferData);
		}

		level.setBlock(pos, newState, 3);
		if (level.isClientSide()) {
			level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oldState));
		}

		if (!level.isClientSide()) {
			BlockEntity newBE = level.getBlockEntity(pos);
			putContents(newBE, transferData);
		}
	}

	private static void takeContents(BlockEntity blockEntity, List<TransferData> transferData) {
		TransferData item = TransferData.item(blockEntity);
		if (item != null) transferData.add(item);
		TransferData fluid = TransferData.fluid(blockEntity);
		if (fluid != null) transferData.add(fluid);
		TransferData air = TransferData.air(blockEntity);
		if (air != null) transferData.add(air);
	}

	private static void putContents(BlockEntity blockEntity, List<TransferData> transferData) {
		for (TransferData transferDatum : transferData) {
			transferDatum.put(blockEntity);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, level, components, isAdvanced);
		components.add(Component.translatable(getDescriptionId(stack) + ".desc").withStyle(ChatFormatting.GOLD));
	}
}
