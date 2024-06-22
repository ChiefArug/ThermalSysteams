package chiefarug.mods.systeams;

import cofh.core.block.TileBlockActive4Way;
import cofh.core.inventory.container.TileCoFHContainer;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.thermal.lib.block.entity.MachineBlockEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;

public class SteamMachine<B extends MachineBlockEntity, C extends TileCoFHContainer> {

    private final RegistryObject<TileBlockActive4Way> block;
    private final RegistryObject<Item> blockItem;
    private final RegistryObject<BlockEntityType<B>> blockEntity;
    private final RegistryObject<MenuType<C>> menu;

    private static final BlockBehaviour.Properties bProperties = SysteamsRegistry.B_PROPERTIES;

    public SteamMachine(String id, Class<B> blockEntityClass, BlockEntityType.BlockEntitySupplier<B> BEConstructor, IContainerFactory<C> containerConstructor) {
        this(id, blockEntityClass, BEConstructor, containerConstructor, SysteamsRegistry.BLOCK_REGISTRY, SysteamsRegistry.ITEM_REGISTRY, SysteamsRegistry.BLOCK_ENTITY_REGISTRY, SysteamsRegistry.MENU_REGISTRY);
    }

    public SteamMachine(String id, Class<B> blockEntityClass, BlockEntityType.BlockEntitySupplier<B> BEConstructor, IContainerFactory<C> containerConstructor, DeferredRegisterCoFH<Block> blockRegistry, DeferredRegisterCoFH<Item> itemRegistry, DeferredRegisterCoFH<BlockEntityType<?>> blockEntityRegistry, DeferredRegisterCoFH<MenuType<?>> menuRegistry) {
        block = blockRegistry.register(id, () -> new TileBlockActive4Way(bProperties, blockEntityClass, this::blockEntity));
        blockItem = itemRegistry.register(id, () -> SysteamsRegistry.steamMachineBlockItemOf(block()));
        //noinspection DataFlowIssue // i can pass null to datafixers alright
        blockEntity = blockEntityRegistry.register(id, () -> BlockEntityType.Builder.of(BEConstructor, block()).build(null));
        menu = menuRegistry.register(id, () -> IForgeMenuType.create(containerConstructor));
    }

    public TileBlockActive4Way block() {
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
