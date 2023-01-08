package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.GourmandBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.GourmandFuelManager;
import cofh.thermal.lib.util.recipes.internal.IDynamoFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GourmandBoilerBlockEntity extends ItemBoilerBlockEntityBase {

	public GourmandBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.Boilers.GOURMAND.blockEntity(), pos, state);

        initHandlers();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new GourmandBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return SysteamsConfig.STEAM_RATIO_GOURMAND.get();
	}

	@Override
	protected int getCurrentEnergy() {
		IDynamoFuel fuel = getFuelManager().getFuel(this);
		return fuel == null ? getFuelManager().getEnergyFromFood(fuelSlot.getItemStack()) : fuel.getEnergy();
	}

	@Override
	protected GourmandFuelManager getFuelManager() {
		return GourmandFuelManager.instance();
	}
}
