package chiefarug.mods.systeams.compat;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import cofh.thermal.lib.util.managers.IFuelManager;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static chiefarug.mods.systeams.SysteamsConfig.SPEED_PNEUMATIC;
import static chiefarug.mods.systeams.SysteamsConfig.STEAM_RATIO_PNEUMATIC;

public class PneumaticBoilerBlockEntity extends BoilerBlockEntityBase {

	protected final IAirHandlerMachine airHandler;
	private final LazyOptional<IAirHandlerMachine> airHandlerCap;
	private final Map<IAirHandlerMachine, List<Direction>> airHandlerMap = new IdentityHashMap<>();

	public PneumaticBoilerBlockEntity(BlockPos pos, BlockState blockState) {
		super(SysteamsPNCRCompat.Registry.PNEUMATIC_BOILER_BLOCK_ENTITY.get(), pos, blockState);

		this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory().createAirHandler(PressureTier.TIER_TWO, PneumaticValues.VOLUME_PNEUMATIC_DYNAMO);
		this.airHandlerCap = LazyOptional.of(() -> airHandler);
	}

	@Override
	protected int consumeFuel() {
		return 0;
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return STEAM_RATIO_PNEUMATIC.get();
	}

	@Override
	protected int getFuelEnergy() {
		return 20;
	}

	@Override
	protected IFuelManager getFuelManager() {
		throw new NotImplementedException("PNC:R has no air fuel manager");
	}

	@Override
	protected double getSpeedMultiplier() {
		return SPEED_PNEUMATIC.get();
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new PneumaticBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}
}
