package chiefarug.mods.systeams;

import chiefarug.mods.systeams.block.BoilerBlock;
import cofh.lib.fluid.SimpleTankInv;
import cofh.lib.inventory.SimpleItemInv;
import cofh.thermal.core.ThermalCore;
import cofh.thermal.lib.block.DynamoBlock;
import cofh.thermal.lib.block.entity.AugmentableBlockEntity;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
import static chiefarug.mods.systeams.SysteamsRegistry.Items.RF_COIL;
import static cofh.lib.util.constants.BlockStatePropertiesCoFH.FACING_ALL;
import static cofh.thermal.lib.block.DynamoBlock.WATERLOGGED;

public class ConversionKitItem extends Item {
	public ConversionKitItem(Properties pProperties) {
		super(pProperties);
	}

	// this isn't static, so that the block registry is filled before we get all these block objects
	public BiMap<DynamoBlock, BoilerBlock> dynamoBoilerMap = ImmutableBiMap.of(
			getDynamo("stirling"), STIRLING.block(),
			getDynamo("compression"), COMPRESSION.block(),
			getDynamo("magmatic"), MAGMATIC.block(),
			getDynamo("numismatic"), NUMISMATIC.block(),
			getDynamo("lapidary"), LAPIDARY.block(),
			getDynamo("disenchantment"), DISENCHANTMENT.block(),
			getDynamo("gourmand"), GOURMAND.block()
	);

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
		@SuppressWarnings("SuspiciousMethodCalls") // i believe this is because its getting passed the wrong class type. it works tho
		Block boiler = dynamoBoilerMap.get(dynamo);
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
				stack.shrink(1);
				if (stack.isEmpty())
					player.setItemInHand(hand, new ItemStack(RF_COIL.get()));
				else
					player.addItem(new ItemStack(RF_COIL.get()));
			}
		} else {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
	}

	public static void transformDynamoBoiler(BlockPos pos, Level level, BlockState oldState, BlockState newState, @Nullable Player player) {
		List<ItemStack> oldItems = new ArrayList<>();
		List<FluidStack> oldFluids = new ArrayList<>();
		if (!level.isClientSide()) {
			AugmentableBlockEntity oldBE = (AugmentableBlockEntity) level.getBlockEntity(pos);
			assert oldBE != null;

			SimpleItemInv oldInv = oldBE.getItemInv();
			for (int i = 0; i < oldInv.getSlots(); i++)
				oldItems.add(oldInv.getSlot(i).extractItem(i, 64, false));
			SimpleTankInv oldTanks = oldBE.getTankInv();
			for (int i = 0;i < oldTanks.getTanks();i++)
				oldFluids.add(oldTanks.getTank(i).drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE));
		}

		level.setBlock(pos, newState, 3);
		if (level.isClientSide()) {
			level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oldState));
		}

		if (!level.isClientSide()) {
			AugmentableBlockEntity newBE = (AugmentableBlockEntity) level.getBlockEntity(pos);
			assert newBE != null;

			SimpleItemInv newInv = newBE.getItemInv();
			for (ItemStack item : oldItems) {
				for (int i = 0; i < newInv.getSlots(); i++) {
					if (item.isEmpty()) break;
					item = newBE.getItemInv().insertItem(i, item, false);
				}
			}
			SimpleTankInv newTanks = newBE.getTankInv();
			for (FluidStack fluid : oldFluids) {
				for (int i = 0; i < newTanks.getTanks(); i++) {
					if (fluid.isEmpty()) break;
					int remaining = newTanks.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
					fluid.shrink(remaining);
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, level, components, isAdvanced);
		components.add(Component.translatable(getDescriptionId(stack) + ".desc").withStyle(ChatFormatting.GOLD));
	}
}
