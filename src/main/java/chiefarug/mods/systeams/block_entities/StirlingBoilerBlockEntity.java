package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.StirlingBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.StirlingFuelManager;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StirlingBoilerBlockEntity extends ItemBoilerBlockEntityBase {

	public StirlingBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.Boilers.STIRLING.blockEntity(), pos, state);

        initHandlers();
	}


	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new StirlingBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}

	@Override
	protected int steamPerTick() {
		return 0;
	}

	@Override
	protected double getEnergyToSteamRation() {
		return SysteamsConfig.STEAM_RATIO_STERLING.get();
	}

	@Override
	protected SingleItemFuelManager getFuelManager() {
		return StirlingFuelManager.instance();
	}
}
