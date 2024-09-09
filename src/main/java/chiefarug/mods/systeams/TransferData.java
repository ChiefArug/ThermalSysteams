package chiefarug.mods.systeams;

import cofh.lib.fluid.SimpleTankInv;
import cofh.lib.inventory.ItemStorageCoFH;
import cofh.lib.inventory.ManagedItemInv;
import cofh.lib.inventory.SimpleItemInv;
import cofh.thermal.lib.block.entity.AugmentableBlockEntity;
import com.google.common.collect.Iterators;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static chiefarug.mods.systeams.Systeams.AIR_HANDLER_CAPABILITY;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

// This would be much easier if I had some mixined accessors. Maybe one day?
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
        protected List<ItemStack> inputs = new ArrayList<>();
        protected List<ItemStack> internal = new ArrayList<>();
        protected List<ItemStack> outputs = new ArrayList<>();
        protected List<ItemStack> other = new ArrayList<>();

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
            // if its a managed inventory (which it will be) extract each seperately
            if (oldInv instanceof ManagedItemInv manInv) {
                inputs.addAll(extractFromCoFHSlots(manInv.getInputSlots()));
                internal.addAll(extractFromCoFHSlots(manInv.getInternalSlots()));
                outputs.addAll(extractFromCoFHSlots(manInv.getOutputSlots()));
            } // in the rare case its not a ManagedItemInv or if there are other slots not in the above then manually got through everything.
            for (int i = 0; i < oldInv.getSlots(); i++) {
                ItemStack item = oldInv.getSlot(i).extractItem(0, 64, false);
                if (item.isEmpty()) return;
                other.add(item);
            }
        }

        private static List<ItemStack> extractFromCoFHSlots(List<ItemStorageCoFH> slotList) {
            return slotList.stream()
                    .map(slot -> slot.extractItem(0, 64, false))
                    .filter(Predicate.not(ItemStack::isEmpty))
                    .toList();
        }

        private void takeFromOther(BlockEntity be) {
            LazyOptional<IItemHandler> oldInvLO = be.getCapability(ITEM_HANDLER);
            oldInvLO.ifPresent(oldInvCap -> {
                for (int i = 0; i < oldInvCap.getSlots(); i++)
                    // we have no idea where these are supposed to go, so stick em in other.
                    other.add(oldInvCap.extractItem(i, 64, false));
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
            if (be.getLevel() == null) return;
            popResources(inputs, be.getLevel(), be.getBlockPos());
            popResources(internal, be.getLevel(), be.getBlockPos());
            popResources(outputs, be.getLevel(), be.getBlockPos());
            popResources(other, be.getLevel(), be.getBlockPos());
        }

        private static void popResources(List<ItemStack> items, Level level, BlockPos pos) {
            for (ItemStack item : items) Block.popResource(level, pos, item);
        }

        private void putToThermal(AugmentableBlockEntity abe) {
            SimpleItemInv newInv = abe.getItemInv();
            Iterator<ItemStack> remains;
            boolean isManaged = newInv instanceof ManagedItemInv;
            if (isManaged) {
                ManagedItemInv manInv = (ManagedItemInv) newInv;
                inputs = insertToCoFHSlots(inputs, manInv.getInputSlots());
                internal = insertToCoFHSlots(internal, manInv.getInternalSlots());
                outputs = insertToCoFHSlots(outputs, manInv.getOutputSlots());
                // if it's a managed inv then we only have other left to fit elsewhere, everything else doesn't belong randomly shoved around
                remains = other.iterator();
            } else {
                // if it's not a managed inv all bets are off so try stuff everything everywhere.
                remains = Iterators.concat(inputs.iterator(), internal.iterator(), outputs.iterator(), internal.iterator());
            }
            List<ItemStack> newOther = new ArrayList<>(other.size());
            while (remains.hasNext()) {
                ItemStack item = remains.next();
                for (int i = 0; i < newInv.getSlots(); i++) {
                    if (item.isEmpty()) continue;
                    item = newInv.getSlot(i).insertItem(0, item, false);
                }
                if (item.isEmpty()) continue;
                newOther.add(item);
            }
            other = newOther;
            if (!isManaged) { // these have been merged into other. we can't clear them earlier cause they are still backing the iterator
                inputs.clear();
                internal.clear();
                outputs.clear();
            }
        }

        private static List<ItemStack> insertToCoFHSlots(List<ItemStack> toInsert, List<ItemStorageCoFH> slotList) {
            List<ItemStack> newList = new ArrayList<>(toInsert.size());
            for (ItemStack item : toInsert) {
                for (ItemStorageCoFH slot : slotList) {
                    if (item.isEmpty()) continue;
                    item = slot.insertItem(0, item, false);
                }
                if (item.isEmpty()) continue;
                newList.add(item);
            }
            ;
            return newList;
        }

        private void putToOther(BlockEntity be) {
            be.getCapability(ITEM_HANDLER).ifPresent(cap -> {
                inputs = insertToCap(cap, inputs);
                internal = insertToCap(cap, internal);
                outputs = insertToCap(cap, outputs);
                other = insertToCap(cap, other);
            });
        }

        private static List<ItemStack> insertToCap(IItemHandler cap, List<ItemStack> toInsert) {
            List<ItemStack> newList = new ArrayList<>(toInsert.size());
            for (ItemStack item : toInsert) {
                for (int i = 0; i < cap.getSlots(); i++) {
                    if (item.isEmpty()) continue;
                    item = cap.insertItem(i, item, false);
                }
                if (item.isEmpty()) continue;
                newList.add(item);
            }
            return newList;
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

        private AirTransferData() {}

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