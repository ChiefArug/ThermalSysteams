package chiefarug.mods.systeams;

import cofh.core.block.TileBlockActive4Way;
import cofh.core.block.entity.TileCoFH;
import cofh.thermal.core.ThermalCore;
import cofh.thermal.lib.block.DynamoBlock;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.*;
import static chiefarug.mods.systeams.SysteamsRegistry.Items.RF_COIL;
import static chiefarug.mods.systeams.SysteamsRegistry.SteamMachines.PULVERIZER;

public class ConversionKitItem extends Item {
	public ConversionKitItem(Properties pProperties) {
		super(pProperties);
	}

	private static final int steamyCount = 9;
	// a map of RF to Steam Blocks
	private static final BiMap<Block, Block> conversions = HashBiMap.create(steamyCount);
	private static final Map<Block, ItemLike> nonSteamItem = new HashMap<>(steamyCount);

	public static void fillConversionMap() {
		addToConversions(getDynamo("stirling"), STIRLING.block(), RF_COIL);
		addToConversions(getDynamo("compression"), COMPRESSION.block(), RF_COIL);
		addToConversions(getDynamo("magmatic"), MAGMATIC.block(), RF_COIL);
		addToConversions(getDynamo("numismatic"), NUMISMATIC.block(), RF_COIL);
		addToConversions(getDynamo("lapidary"), LAPIDARY.block(), RF_COIL);
		addToConversions(getDynamo("disenchantment"), DISENCHANTMENT.block(), RF_COIL);
		addToConversions(getDynamo("gourmand"), GOURMAND.block(), RF_COIL);
		addToConversions(getMachine("pulverizer"), PULVERIZER.block(), RF_COIL);
	}

	public static void addToConversions(Block rf, Block steam, ItemLike item) {
		conversions.put(rf, steam);
		nonSteamItem.put(steam, item);
	}

	public static Block getRFLike(Block steamy) {
		return conversions.inverse().get(steamy);
	}

	public static Block getSteamLike(Block rf) {
		return conversions.get(rf);
	}

	public static ItemStack getConversionItem(Block steamy) {
		return new ItemStack(nonSteamItem.get(steamy));
	}

	private static DynamoBlock getDynamo(String id) {
		return (DynamoBlock) ThermalCore.BLOCKS.get("thermal:dynamo_" + id);
	}

	private static TileBlockActive4Way getMachine(String id) {
		return (TileBlockActive4Way) ThermalCore.BLOCKS.get("thermal:machine_" + id);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		BlockState oldState = level.getBlockState(pos);
		ItemStack stack = context.getItemInHand();
		InteractionHand hand = context.getHand();

		Block dynamo = oldState.getBlock();
		Block steamyBlock = getSteamLike(dynamo);
		if (steamyBlock == null)
			return super.useOn(context);

		Player player = context.getPlayer();
		BlockState newState = steamyBlock.defaultBlockState();

		for (Property<?> property : oldState.getProperties()) {
			newState = applyProperty(property, oldState, newState);
		}
		coreTransfer(pos, level, oldState, newState, player);

		// if we have a player, replace with a coil. otherwise just shrink the itemstack
		if (player != null) {
			if (!player.getAbilities().instabuild) {
				ItemStack conversionReturn;
				conversionReturn = getConversionItem(steamyBlock);
				stack.shrink(1);
				if (stack.isEmpty())
					player.setItemInHand(hand, conversionReturn);
				else
					player.addItem(conversionReturn);
			}
		} else {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
	}

	public static <T extends Comparable<T>> BlockState applyProperty(Property<T> prop, BlockState oldState, BlockState toApply) {
		return toApply.hasProperty(prop) ? toApply.setValue(prop, oldState.getValue(prop)) : toApply;
	}

	public static void coreTransfer(BlockPos pos, Level level, BlockState oldState, BlockState newState, @Nullable Player player) {
		List<TransferData> transferData = new ArrayList<>(3);

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
			// refresh augment data and the like
			if (newBE instanceof TileCoFH abe) abe.onPlacedBy(level, pos, newState, player, /* hopefully this doesn't break anything. its not used anywhere*/ ItemStack.EMPTY);
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
}
