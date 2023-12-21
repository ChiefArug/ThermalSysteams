package chiefarug.mods.systeams;

import cofh.lib.common.fluid.SimpleTankInv;
import cofh.lib.common.inventory.SimpleItemInv;
import cofh.thermal.lib.common.block.entity.AugmentableBlockEntity;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static chiefarug.mods.systeams.Systeams.AIR_HANDLER_CAPABILITY;
import static net.minecraft.world.level.block.Block.popResource;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

interface TransferData {
	void take(BlockEntity be);

	void put(BlockEntity be);

	static TransferData from(TransferData data, BlockEntity be) {
		data.take(be);
		return data;
	}

	@Nullable
	static TransferData item(BlockEntity be) {
		if (be.getCapability(ITEM_HANDLER).isPresent() || be instanceof AugmentableBlockEntity) {
			return from(new ItemTransferData(), be);
		}
		return null;
	}

	@Nullable
	static TransferData fluid(BlockEntity be) {
		if (be.getCapability(FLUID_HANDLER).isPresent() || be instanceof AugmentableBlockEntity) {
			return from(new FluidTransferData(), be);
		}
		return null;
	}

	@Nullable
	static TransferData air(BlockEntity be) {
		if (be.getCapability(AIR_HANDLER_CAPABILITY).isPresent()) {
			return from(AirTransferData.from(), be);
		}
		return null;
	}


	class ItemTransferData implements TransferData {
		protected List<ItemStack> items = new ArrayList<>();

		@Override
		public void take(BlockEntity be) {
			if (be instanceof AugmentableBlockEntity abe) {
				takeFromThermal(abe);
			} else {
				takeFromOther(be);
			}
		}

		private void takeFromThermal(AugmentableBlockEntity abe) {
			SimpleItemInv oldInv = abe.getItemInv();
			for (int i = 0; i < oldInv.getSlots(); i++)
				items.add(oldInv.getSlot(i).extractItem(i, 64, false));
		}

		private void takeFromOther(BlockEntity be) {
			LazyOptional<IItemHandler> oldInvLO = be.getCapability(ITEM_HANDLER);
			oldInvLO.ifPresent(oldInvCap -> {
				for (int i = 0; i < oldInvCap.getSlots(); i++)
					items.add(oldInvCap.extractItem(i, 64, false));
			});
		}

		@Override
		public void put(BlockEntity be) {
			if (be instanceof AugmentableBlockEntity abe) {
				putToThermal(abe);
			} else {
				putToOther(be);
			}
			// chuck anything not transferred on the ground.
			items.forEach(item -> popResource(Objects.requireNonNull(be.getLevel()), be.getBlockPos(), item));
		}

		private void putToThermal(AugmentableBlockEntity abe) {
			SimpleItemInv newInv = abe.getItemInv();
			items.removeIf(item -> {
				for (int i = 0; i < newInv.getSlots(); i++) {
					if (item.isEmpty()) return true;
					item = abe.getItemInv().insertItem(i, item, false);
				}
				return item.isEmpty();
			});
		}

		private void putToOther(BlockEntity be) {
			LazyOptional<IItemHandler> newInvLO = be.getCapability(ITEM_HANDLER);
			newInvLO.ifPresent(newInvCap -> items.removeIf(item -> {
				for (int i = 0; i < newInvCap.getSlots(); i++) {
					if (item.isEmpty()) return true;
					item = newInvCap.insertItem(i, item, false);
				}
				return item.isEmpty();
			}));
		}
	}

	class FluidTransferData implements TransferData {
		protected List<FluidStack> fluids = new ArrayList<>();

		@Override
		public void take(BlockEntity be) {
			if (be instanceof AugmentableBlockEntity abe) {
				takeFromThermal(abe);
			} else {
				takeFromOther(be);
			}
		}

		private void takeFromThermal(AugmentableBlockEntity abe) {
			SimpleTankInv oldTanks = abe.getTankInv();
			for (int i = 0; i < oldTanks.getTanks(); i++)
				fluids.add(oldTanks.getTank(i).drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE));
		}

		private void takeFromOther(BlockEntity be) {
			LazyOptional<IFluidHandler> oldTankLO = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
			oldTankLO.ifPresent(oldTankCap -> {
				for (int i = 0; i < oldTankCap.getTanks(); i++)
					fluids.add(oldTankCap.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE));
			});
		}

		@Override
		public void put(BlockEntity be) {
			if (be instanceof AugmentableBlockEntity abe) {
				putToThermal(abe);
			} else {
				putToOther(be);
			}
		}

		private void putToThermal(AugmentableBlockEntity abe) {
			SimpleTankInv newTanks = abe.getTankInv();
			fluids.removeIf(fluid -> {
				for (int i = 0; i < newTanks.getTanks(); i++) {
					if (fluid.isEmpty()) return true;
					int remaining = newTanks.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
					fluid.shrink(remaining);
				}
				return fluid.isEmpty();
			});
		}

		private void putToOther(BlockEntity be) {
			LazyOptional<IFluidHandler> newTankLO = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
			newTankLO.ifPresent(newTankCap -> fluids.removeIf(fluid -> {
				for (int i = 0; i < newTankCap.getTanks(); i++) {
					if (fluid.isEmpty()) return true;
					int remaining = newTankCap.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
					fluid.shrink(remaining);
				}
				return fluid.isEmpty();
			}));
		}
	}

	class AirTransferData implements TransferData {

		private AirTransferData() {
		}

		// theoretically this shouldn't be loaded till it is called
		public static AirTransferData from() {
			return new AirTransferData();
		}

		protected int amount;
		protected int volume;
		protected float pressure;
		protected boolean exists;

		@Override
		public void take(BlockEntity be) {
			LazyOptional<IAirHandlerMachine> oldAirLO = be.getCapability(AIR_HANDLER_CAPABILITY);
			oldAirLO.ifPresent(oldAirCap -> {
				amount = oldAirCap.getAir();
				pressure = oldAirCap.getPressure();
				volume = oldAirCap.getVolume();
				exists = true;
				oldAirCap.addAir(-amount);
			});
		}

		@Override
		public void put(BlockEntity be) {
			LazyOptional<IAirHandlerMachine> newAirLO = be.getCapability(AIR_HANDLER_CAPABILITY);
			if (exists) {
				newAirLO.ifPresent(newAirCap -> {
					if (newAirCap.getVolume() >= volume) {
						newAirCap.addAir(amount);
					} else {
						newAirCap.setPressure(pressure);
					}
				});
			}
		}
	}
}