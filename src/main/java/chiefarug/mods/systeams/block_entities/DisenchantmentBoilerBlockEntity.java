package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.containers.DisenchantmentBoilerContainer;
import cofh.thermal.core.util.managers.dynamo.DisenchantmentFuelManager;
import cofh.thermal.lib.util.recipes.internal.IDynamoFuel;
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
		return new DisenchantmentBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return SysteamsConfig.STEAM_RATIO_DISENCHANTMENT.get();
	}

	@Override
	protected int getCurrentEnergy() {
		IDynamoFuel fuel = getFuelManager().getFuel(this);
		return fuel == null ? getFuelManager().getEnergyFromEnchantments(fuelSlot.getItemStack()) : fuel.getEnergy();
	}

	@Override
	protected DisenchantmentFuelManager getFuelManager() {
		return DisenchantmentFuelManager.instance();
	}

	@Override
	protected double getSpeedMultiplier() {
		return SPEED_DISENCHANTMENT.get();
	}
}
