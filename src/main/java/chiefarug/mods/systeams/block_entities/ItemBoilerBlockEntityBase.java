package chiefarug.mods.systeams.block_entities;

import cofh.lib.api.StorageGroup;
import cofh.lib.inventory.ItemStorageCoFH;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemBoilerBlockEntityBase extends BoilerBlockEntityBase {

	protected ItemStorageCoFH fuelSlot = new ItemStorageCoFH(item -> filter.valid(item) && getFuelManager().validFuel(item));

	public ItemBoilerBlockEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		inventory.addSlot(fuelSlot, StorageGroup.INPUT);
	}

	@Override
	protected abstract SingleItemFuelManager getFuelManager();

	@Override
	protected int consumeFuel() {
		int energy = getCurrentEnergy();;
		fuelSlot.consume(1);
		return energy;
	}
}
