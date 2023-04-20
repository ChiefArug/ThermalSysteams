package chiefarug.mods.systeams;

import chiefarug.mods.systeams.block.BoilerBlock;
import cofh.thermal.core.ThermalCore;
import cofh.thermal.lib.block.DynamoBlock;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
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
import static cofh.thermal.lib.block.DynamoBlock.WATERLOGGED;

public class ConversionKitItem extends Item {
	public ConversionKitItem(Properties pProperties) {
		super(pProperties);
	}

	// this isn't static, so that the block registry is filled before we get all these block objects
	// relies on item reg (therefore Item instance initialization) happening AFTER block init.
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
		return SysteamsRegistry.Items.BOILER_PIPE.get().dynamoBoilerMap;
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
				ItemStack converstionReturn = ((BoilerBlock) boiler).getOtherConversionItem();
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

	static class AirTransferData {
		protected int amount;
		protected int volume;
		protected float pressure;
		protected boolean exists;
	}

	public static void transformDynamoBoiler(BlockPos pos, Level level, BlockState oldState, BlockState newState, @Nullable Player player) {
		List<ItemStack> oldItems = new ArrayList<>();
		List<FluidStack> oldFluids = new ArrayList<>();
		final AirTransferData oldAir = new AirTransferData();

		if (!level.isClientSide()) {
			BlockEntity oldBE = level.getBlockEntity(pos);
			assert oldBE != null;

			LazyOptional<IItemHandler> oldInvLO = oldBE.getCapability(ForgeCapabilities.ITEM_HANDLER);
			oldInvLO.ifPresent(oldInvCap -> {
				for (int i = 0; i < oldInvCap.getSlots(); i++)
					oldItems.add(oldInvCap.extractItem(i, 64, false));
			});
			LazyOptional<IFluidHandler> oldTankLO = oldBE.getCapability(ForgeCapabilities.FLUID_HANDLER);
			oldTankLO.ifPresent(oldTankCap -> {
				for (int i = 0; i < oldTankCap.getTanks(); i++)
					oldFluids.add(oldTankCap.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE));
			});
			LazyOptional<IAirHandlerMachine> oldAirLO = oldBE.getCapability(Systeams.AIR_HANDLER_CAPABILITY);
			oldAirLO.ifPresent(oldAirCap -> {
				oldAir.amount = oldAirCap.getAir();
				oldAir.pressure = oldAirCap.getPressure();
				oldAir.volume = oldAirCap.getVolume();
				oldAir.exists = true;
				oldAirCap.addAir(-oldAir.amount);
			});
		}

		level.setBlock(pos, newState, 3);
		if (level.isClientSide()) {
			level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(oldState));
		}

		if (!level.isClientSide()) {
			BlockEntity newBE = level.getBlockEntity(pos);
			assert newBE != null;

			LazyOptional<IItemHandler> newInvLO = newBE.getCapability(ForgeCapabilities.ITEM_HANDLER);
			newInvLO.ifPresent(newInvCap -> {
				for (ItemStack item : oldItems) {
					for (int i = 0; i < newInvCap.getSlots(); i++) {
						if (item.isEmpty()) break;
						item = newInvCap.insertItem(i, item, false);
					}
				}
			});
			LazyOptional<IFluidHandler> newTankLO = newBE.getCapability(ForgeCapabilities.FLUID_HANDLER);
			newTankLO.ifPresent(newTankCap -> {
				for (FluidStack fluid : oldFluids) {
					for (int i = 0; i < newTankCap.getTanks(); i++) {
						if (fluid.isEmpty()) break;
						int remaining = newTankCap.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
						fluid.shrink(remaining);
					}
				}
			});
			LazyOptional<IAirHandlerMachine> newAirLO = newBE.getCapability(Systeams.AIR_HANDLER_CAPABILITY);
			if (oldAir.exists) {
				newAirLO.ifPresent(newAirCap -> {
					if (newAirCap.getVolume() >= oldAir.volume) {
						newAirCap.addAir(oldAir.amount);
					} else {
						newAirCap.setPressure(oldAir.pressure);
					}
				});
			}
		}
	}


	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isAdvanced) {
		super.appendHoverText(stack, level, components, isAdvanced);
		components.add(Component.translatable(getDescriptionId(stack) + ".desc").withStyle(ChatFormatting.GOLD));
	}
}
