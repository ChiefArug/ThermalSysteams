package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.CompressionBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.CompressionFuelManager;
import cofh.thermal.lib.util.managers.SingleFluidFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CompressionBoilerBlockEntity extends FluidBoilerBlockEntityBase{
	public CompressionBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.Boilers.COMPRESSION.blockEntity(), pos, state);

		initHandlers();
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return SysteamsConfig.STEAM_RATIO_COMPRESSION.get();
	}

	@Override
	protected SingleFluidFuelManager getFuelManager() {
		return CompressionFuelManager.instance();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new CompressionBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}
}
