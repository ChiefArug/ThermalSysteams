package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.containers.NumismaticBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.NumismaticFuelManager;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static chiefarug.mods.systeams.SysteamsConfig.SPEED_NUMISMATIC;
import static chiefarug.mods.systeams.SysteamsConfig.STEAM_RATIO_NUMISMATIC;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.NUMISMATIC;

public class NumismaticBoilerBlockEntity extends ItemBoilerBlockEntityBase{
	public NumismaticBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(NUMISMATIC.blockEntity(), pos, state);

		initHandlers();
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return STEAM_RATIO_NUMISMATIC.get();
	}

	@Override
	protected SingleItemFuelManager getFuelManager() {
		return NumismaticFuelManager.instance();
	}

	@Override
	protected double getSpeedMultiplier() {
		return SPEED_NUMISMATIC.get();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new NumismaticBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}
}
