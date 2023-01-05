package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.NumismaticBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.NumismaticFuelManager;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NumismaticBoilerBlockEntity extends ItemBoilerBlockEntityBase{
	public NumismaticBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.Boilers.NUMISMATIC.blockEntity(), pos, state);

		initHandlers();
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return SysteamsConfig.STEAM_RATIO_NUMISMATIC.get();
	}

	@Override
	protected SingleItemFuelManager getFuelManager() {
		return NumismaticFuelManager.instance();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new NumismaticBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}
}
