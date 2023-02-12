package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.containers.StirlingBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.StirlingFuelManager;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static chiefarug.mods.systeams.SysteamsConfig.SPEED_STIRLING;
import static chiefarug.mods.systeams.SysteamsConfig.STEAM_RATIO_STIRLING;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.STIRLING;

public class StirlingBoilerBlockEntity extends ItemBoilerBlockEntityBase {

	public StirlingBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(STIRLING.blockEntity(), pos, state);

        initHandlers();
	}


	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new StirlingBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return STEAM_RATIO_STIRLING.get();
	}

	@Override
	protected SingleItemFuelManager getFuelManager() {
		return StirlingFuelManager.instance();
	}

	@Override
	protected double getSpeedMultiplier() {
		return SPEED_STIRLING.get();
	}
}
