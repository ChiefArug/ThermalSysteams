package chiefarug.mods.systeams.block_entities;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.containers.DynamoSteamContainer;
import cofh.core.client.renderer.model.ModelUtils;
import cofh.core.network.packet.client.TileStatePacket;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.api.StorageGroup;
import cofh.lib.fluid.FluidStorageCoFH;
import cofh.lib.util.Constants;
import cofh.thermal.lib.tileentity.DynamoTileBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static cofh.lib.util.Constants.BUCKET_VOLUME;


public class DynamoSteamBlockEntity extends DynamoTileBase {

	private final Predicate<FluidStack> isSteam = stack -> ForgeRegistries.FLUIDS.tags().getTag(SysteamsRegistry.Fluids.Steam.TAG).contains(stack.getFluid());
	protected final FluidStorageCoFH steamTank = new FluidStorageCoFH(Constants.TANK_SMALL, isSteam);

	public DynamoSteamBlockEntity(BlockPos pos, BlockState state) {
		super(SysteamsRegistry.BlockEntities.STEAM_DYNAMO.get(), pos, state);

		tankInv.addTank(steamTank, StorageGroup.INPUT);

		renderFluid = new FluidStack(SysteamsRegistry.Fluids.Steam.FLUID.get(), BUCKET_VOLUME);
	}

	@Override
	protected boolean canProcessStart() {
		return false;
	}

	@Override
    protected void processStart() {
        if (cacheRenderFluid()) {
            TileStatePacket.sendToClient(this);
        }
    }

    @Override
    protected boolean cacheRenderFluid() {
        FluidStack prevFluid = renderFluid;
        renderFluid = new FluidStack(steamTank.getFluidStack(), BUCKET_VOLUME);
        return !FluidHelper.fluidsEqual(renderFluid, prevFluid);
    }

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
		return new DynamoSteamContainer(i, level, worldPosition, inventory, player);
	}

	@Nonnull
    @Override
    public ModelData getModelData() {

        return ModelData.builder()
                .with(ModelUtils.FLUID, renderFluid)
                .build();
    }
}
