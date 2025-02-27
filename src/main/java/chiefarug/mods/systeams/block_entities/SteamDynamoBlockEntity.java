package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.SteamDynamoMenu;
import chiefarug.mods.systeams.recipe.SteamFuelManager;
import cofh.core.client.renderer.model.ModelUtils;
import cofh.core.common.network.packet.client.TileStatePacket;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.api.StorageGroup;
import cofh.lib.common.fluid.FluidStorageCoFH;
import cofh.lib.util.Constants;
import cofh.thermal.core.common.config.ThermalCoreConfig;
import cofh.thermal.lib.common.block.entity.DynamoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Predicate;

import static cofh.lib.util.Constants.BUCKET_VOLUME;
import static cofh.thermal.lib.util.managers.SingleFluidFuelManager.FLUID_FUEL_AMOUNT;


public class SteamDynamoBlockEntity extends DynamoBlockEntity {

	private final Predicate<FluidStack> isSteam = fluid -> filter.valid(fluid) && SteamFuelManager.instance().validFuel(fluid);
	protected final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_LARGE, isSteam);

	public SteamDynamoBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.BlockEntities.STEAM_DYNAMO.get(), pos, state);

		tankInv.addTank(steamTank, StorageGroup.INPUT);

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
        fuelUp();
    }

	/**
	 * This keeps track of how much fuel per cycle we are burning, and is cached so the flame inside the gui is consistent *ish*
	 */
	private int fuelPerCycle = 1;

	private void fuelUp() {
		int energy = Math.round(SteamFuelManager.instance().getEnergy(steamTank.getFluidStack()) * energyMod);
		for (int i = fuelPerCycle;i > 0 && !steamTank.isEmpty();i--) {
			fuel += energy;
			steamTank.modify(-FLUID_FUEL_AMOUNT);
		}
		while (fuel <= 0 && !steamTank.isEmpty()) {
			fuelPerCycle++;
			fuel += energy;
			steamTank.modify(-FLUID_FUEL_AMOUNT);
		}
		fuelMax = energy * fuelPerCycle;
	}

	@Override
	protected void resetAttributes() {
		super.resetAttributes();
		// reset the fuel per cycle incase the speed changed due to augs being added/removed
		fuelPerCycle = 1;
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
		return new SteamDynamoMenu(i, Objects.requireNonNull(this.level, "Tried to construct a Steam Dynamo menu too ealy!"), worldPosition, inventory, player);
	}

	@Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(ModelUtils.FLUID, renderFluid)
                .build();
    }
}
