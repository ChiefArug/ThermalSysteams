package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.containers.MagmaticBoilerMenu;
import cofh.thermal.core.util.managers.dynamo.MagmaticFuelManager;
import cofh.thermal.lib.util.managers.SingleFluidFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import static chiefarug.mods.systeams.SysteamsConfig.SPEED_MAGMATIC;
import static chiefarug.mods.systeams.SysteamsConfig.STEAM_RATIO_MAGMATIC;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.MAGMATIC;
import static cofh.lib.util.Constants.BUCKET_VOLUME;

public class MagmaticBoilerBlockEntity extends FluidBoilerBlockEntityBase{
	public MagmaticBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(MAGMATIC.blockEntity(), pos, state);

		renderFluid = new FluidStack(Fluids.LAVA, BUCKET_VOLUME);

		initHandlers();
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return STEAM_RATIO_MAGMATIC.get();
	}

	@Override
	protected SingleFluidFuelManager getFuelManager() {
		return MagmaticFuelManager.instance();
	}

	@Override
	protected double getSpeedMultiplier() {
		return SPEED_MAGMATIC.get();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new MagmaticBoilerMenu(containerId, this.level, this.getBlockPos(), playerInv, player);
	}
}
