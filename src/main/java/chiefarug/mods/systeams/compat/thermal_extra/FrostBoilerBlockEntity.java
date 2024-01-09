package chiefarug.mods.systeams.compat.thermal_extra;

import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.block_entities.ItemBoilerBlockEntityBase;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import mrthomas20121.thermal_extra.recipe.ColdFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrostBoilerBlockEntity extends ItemBoilerBlockEntityBase {
    public FrostBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(SysteamsThermalExtraCompat.Registry.FROST.blockEntity(), pos, state);
    }

    @Override
    protected double getEnergyToSteamRatio() {
        return SysteamsConfig.STEAM_RATIO_FROST.get();
    }

    @Override
    protected double getSpeedMultiplier() {
        return SysteamsConfig.SPEED_FROST.get();
    }

    @Override
    protected SingleItemFuelManager getFuelManager() {
        return ColdFuelManager.instance();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return new FrostBoilerMenu(pContainerId, this.level, this.getBlockPos(), pPlayerInventory, pPlayer);
    }
}
