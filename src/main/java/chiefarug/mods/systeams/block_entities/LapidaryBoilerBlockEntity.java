package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.LapidaryBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.LapidaryFuelManager;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LapidaryBoilerBlockEntity extends ItemBoilerBlockEntityBase {

	public LapidaryBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.Boilers.LAPIDARY.blockEntity(), pos, state);

        initHandlers();
	}


	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new LapidaryBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return SysteamsConfig.STEAM_RATIO_LAPIDARY.get();
	}

	@Override
	protected SingleItemFuelManager getFuelManager() {
		return LapidaryFuelManager.instance();
	}
}
