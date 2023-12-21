package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.containers.DisenchantmentBoilerMenu;
import cofh.thermal.core.util.managers.dynamo.DisenchantmentFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static chiefarug.mods.systeams.SysteamsConfig.SPEED_DISENCHANTMENT;
import static chiefarug.mods.systeams.SysteamsRegistry.Boilers.DISENCHANTMENT;

public class DisenchantmentBoilerBlockEntity extends ItemBoilerBlockEntityBase {

	public DisenchantmentBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(DISENCHANTMENT.blockEntity(), pos, state);

        initHandlers();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new DisenchantmentBoilerMenu(containerId, this.level, this.getBlockPos(), playerInv, player);
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return SysteamsConfig.STEAM_RATIO_DISENCHANTMENT.get();
	}

	@Override
	protected DisenchantmentFuelManager getFuelManager() {
		return DisenchantmentFuelManager.instance();
	}

	@Override
	protected int getFuelEnergy() {
		return getFuelManager().getEnergy(fuelSlot.getItemStack());
	}

	@Override
	protected double getSpeedMultiplier() {
		return SPEED_DISENCHANTMENT.get();
	}
}
