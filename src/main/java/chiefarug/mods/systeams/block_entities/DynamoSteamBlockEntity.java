package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.DynamoSteamContainer;
import chiefarug.mods.systeams.recipe.SteamFuelManager;
import cofh.core.client.renderer.model.ModelUtils;
import cofh.core.network.packet.client.TileStatePacket;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.api.StorageGroup;
import cofh.lib.fluid.FluidStorageCoFH;
import cofh.lib.util.Constants;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.lib.tileentity.DynamoTileBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static cofh.lib.util.Constants.BUCKET_VOLUME;
import static cofh.thermal.lib.util.managers.SingleFluidFuelManager.FLUID_FUEL_AMOUNT;


public class DynamoSteamBlockEntity extends DynamoTileBase {

	private final Predicate<FluidStack> isSteam = fluid -> filter.valid(fluid) && SteamFuelManager.instance().validFuel(fluid);
	protected final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_LARGE, isSteam);

	public DynamoSteamBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.BlockEntities.STEAM_DYNAMO.get(), pos, state);

		tankInv.addTank(steamTank, StorageGroup.INPUT);

		renderFluid = new FluidStack(SysteamsRegistry.Fluids.STEAM.stillFluid.get(), BUCKET_VOLUME);

		addAugmentSlots(ThermalCoreConfig.dynamoAugments);
        initHandlers();
	}

	@Override
	protected int getBaseProcessTick() {
		return SteamFuelManager.instance().getBasePower();
	}

	@Override
	protected boolean canProcessStart() {
		return SteamFuelManager.instance().getEnergy(steamTank.getFluidStack()) > 0 && steamTank.getAmount() >= FLUID_FUEL_AMOUNT;
	}

	@Override
    protected void processStart() {
        if (cacheRenderFluid()) {
            TileStatePacket.sendToClient(this);
        }
        fuel += fuelMax = Math.round(SteamFuelManager.instance().getEnergy(steamTank.getFluidStack()) * energyMod);
        steamTank.modify(-FLUID_FUEL_AMOUNT);
    }

    @Override
    protected boolean cacheRenderFluid() {
        FluidStack prevFluid = renderFluid;
        renderFluid = new FluidStack(steamTank.getFluidStack(), BUCKET_VOLUME);
        return !FluidHelper.fluidsEqual(renderFluid, prevFluid);
    }

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return new DynamoSteamContainer(i, this.level, worldPosition, inventory, player);
	}

	@Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(ModelUtils.FLUID, renderFluid)
                .build();
    }
}
