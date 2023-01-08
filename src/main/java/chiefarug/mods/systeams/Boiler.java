package chiefarug.mods.systeams;

import chiefarug.mods.systeams.block.BoilerBlock;
import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.containers.BoilerContainerBase;
import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;

// Holder and helper class for making boilers
public class Boiler<B extends BoilerBlockEntityBase, C extends BoilerContainerBase<B>> {

	private final RegistryObject<BoilerBlock> block;
	private final RegistryObject<Item> blockItem;
	private final RegistryObject<BlockEntityType<B>> blockEntity;
	private final RegistryObject<MenuType<C>> menu;

	private static final BlockBehaviour.Properties bProperties = SysteamsRegistry.B_PROPERTIES;

	public Boiler(String id, Class<B> blockEntityClass, BlockEntityType.BlockEntitySupplier<B> BEConstructor, IContainerFactory<C> containerConstructor) {
		this(id, blockEntityClass, BEConstructor, containerConstructor, SysteamsRegistry.BLOCK_REGISTRY, SysteamsRegistry.ITEM_REGISTRY, SysteamsRegistry.BLOCK_ENTITY_REGISTRY, SysteamsRegistry.MENU_REGISTRY);
	}

	public Boiler(String id, Class<B> blockEntityClass, BlockEntityType.BlockEntitySupplier<B> BEConstructor, IContainerFactory<C> containerConstructor, DeferredRegisterCoFH<Block> blockRegistry, DeferredRegisterCoFH<Item> itemRegistry, DeferredRegisterCoFH<BlockEntityType<?>> blockEntityRegistry, DeferredRegisterCoFH<MenuType<?>> menuRegistry) {
		block = blockRegistry.register(id, () -> new BoilerBlock(bProperties, blockEntityClass, this::blockEntity));
		blockItem = itemRegistry.register(id, () -> SysteamsRegistry.machineBlockItemOf(block()));
		//noinspection DataFlowIssue // i can pass null to datafixers alright
		blockEntity = blockEntityRegistry.register(id, () -> BlockEntityType.Builder.of(BEConstructor, block()).build(null));
		menu = menuRegistry.register(id, () -> IForgeMenuType.create(containerConstructor));
	}

	public BoilerBlock block() {
		return block.get();
	}

	public Item item() {
		return blockItem.get();
	}

	public BlockEntityType<B> blockEntity() {
		return blockEntity.get();
	}

	public MenuType<C> menu() {
		return menu.get();
	}
}
