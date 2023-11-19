package chiefarug.mods.systeams.compat.pneumaticcraft;

import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import cofh.core.util.helpers.AugmentableHelper;
import cofh.lib.api.block.entity.ITickableTile;
import cofh.lib.util.constants.NBTTags;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.lib.util.managers.IFuelManager;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static chiefarug.mods.systeams.SysteamsConfig.SPEED_PNEUMATIC;
import static chiefarug.mods.systeams.SysteamsConfig.STEAM_RATIO_PNEUMATIC;

public class PneumaticBoilerBlockEntity extends BoilerBlockEntityBase implements ITickableTile.IClientTickable {

	// Gives us 40% efficiency
	private static final int airPerCycle = 100;
	private static final int energyPerCycle = 40;

	protected final IAirHandlerMachine airHandler;
	private final LazyOptional<IAirHandlerMachine> airHandlerCap;

	private int airPerTick = calcAirPerTick();


	public PneumaticBoilerBlockEntity(BlockPos pos, BlockState blockState) {
		super(SysteamsPNCRCompat.Registry.PNEUMATIC_BOILER_BLOCK_ENTITY.get(), pos, blockState);

		this.airHandler = PneumaticRegistry.getInstance().getAirHandlerMachineFactory().createAirHandler(PressureTier.TIER_TWO, PneumaticValues.VOLUME_PNEUMATIC_DYNAMO);
		this.airHandlerCap = LazyOptional.of(() -> airHandler);

		addAugmentSlots(ThermalCoreConfig.dynamoAugments);
		initHandlers();
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.put(NBTKeys.NBT_AIR_HANDLER, airHandler.serializeNBT());
	}

	@Override
	public FriendlyByteBuf getGuiPacket(FriendlyByteBuf buffer) {
		super.getGuiPacket(buffer);
		buffer.writeFloat(airHandler.getPressure());
		return buffer;
	}

	@Override
	public void handleGuiPacket(FriendlyByteBuf buffer) {
		super.handleGuiPacket(buffer);
		airHandler.setPressure(buffer.readFloat());
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		airHandler.deserializeNBT(nbt.getCompound(NBTKeys.NBT_AIR_HANDLER));
		if (nbt.contains(NBTKeys.NBT_AIR_AMOUNT)) {
			// when restoring from item NBT
			airHandler.addAir(nbt.getInt(NBTKeys.NBT_AIR_AMOUNT));
		}
	}

	@Override
	public void tickServer() {
		super.tickServer();
		airHandler.tick(this);
	}

	@Override
	protected int consumeFuel() {
		this.addAir(-airPerCycle);
		return energyPerCycle;
	}

	@Override
	protected int getFuelEnergy() {
		if (getPressure() > getMinWorkingPressure()) {
			return energyPerCycle;
		}
		return 0;
	}

	@Override
	protected double getEnergyToSteamRatio() {
		return STEAM_RATIO_PNEUMATIC.get();
	}


	@Override
	protected IFuelManager getFuelManager() {
		throw new NotImplementedException("There is no air fuel manager!");
	}

	@Override
	protected double getSpeedMultiplier() {
		return SPEED_PNEUMATIC.get();
	}

	@Override
	protected void resetAttributes() {
		super.resetAttributes();
//		airHandler.setVolumeUpgrades(0);
	}

	@Override
	protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {
		super.finalizeAttributes(enchantmentMap);
		airPerTick = calcAirPerTick();

		float holdingMod = getHoldingMod(enchantmentMap);
        float baseMod = AugmentableHelper.getAttributeModWithDefault(augmentNBT, NBTTags.TAG_AUGMENT_BASE_MOD, 1.0F);

		airHandler.setVolumeUpgrades((int) ((holdingMod + baseMod) - 2)); // subtract two because that is the default
	}

	private int calcAirPerTick() {
		return (int) ((((double) baseEnergyPerTick) / energyPerCycle) * airPerCycle);
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		assert this.level != null;
		return new PneumaticBoilerContainer(containerId, this.level, this.getBlockPos(), playerInv, player);
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == Systeams.AIR_HANDLER_CAPABILITY) {
			if (side == null || side != getFacing()) {
				return airHandlerCap.cast();
			} else {
				return LazyOptional.empty();
			}
		} else {
			return super.getCapability(cap, side);
		}
	}
	// Pressure

	public float getPressure() {
		return airHandler.getPressure();
	}

	public float getMinWorkingPressure() {
		return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DYNAMO;
	}

	public void addAir(int air) {
		airHandler.addAir(air);
	}

	public int getAirPerTick() {
		return airPerTick;
	}

	public int getAir() {
		return airHandler.getAir();
	}

	public int getVolume() {
		return airHandler.getVolume();
	}

	@Override
	public void tickClient() {
		airHandler.tick(this);
	}
}
