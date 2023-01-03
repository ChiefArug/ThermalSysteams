package chiefarug.mods.systeams;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.block_entities.CompressionBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.MagmaticBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.SteamDynamoBlockEntity;
import chiefarug.mods.systeams.block_entities.StirlingBoilerBlockEntity;
import chiefarug.mods.systeams.client.screens.CompressionBoilerScreen;
import chiefarug.mods.systeams.client.screens.MagmaticBoilerScreen;
import chiefarug.mods.systeams.client.screens.SteamDynamoScreen;
import chiefarug.mods.systeams.client.screens.StirlingBoilerScreen;
import chiefarug.mods.systeams.containers.CompressionBoilerContainer;
import chiefarug.mods.systeams.containers.MagmaticBoilerContainer;
import chiefarug.mods.systeams.containers.SteamDynamoContainer;
import chiefarug.mods.systeams.containers.StirlingBoilerContainer;
import chiefarug.mods.systeams.recipe.SteamFuel;
import chiefarug.mods.systeams.recipe.SteamFuelManager;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.lib.util.constants.BlockStatePropertiesCoFH;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.recipes.SerializableRecipeType;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.lib.block.TileBlockDynamo;
import cofh.thermal.lib.common.ThermalAugmentRules;
import cofh.thermal.lib.common.ThermalRecipeManagers;
import cofh.thermal.lib.item.BlockItemAugmentable;
import cofh.thermal.lib.util.recipes.DynamoFuelSerializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static chiefarug.mods.systeams.Systeams.MODID;

@SuppressWarnings("unused")
public class SysteamsRegistry {

	static final CreativeModeTab TAB = new CreativeModeTab(MODID) {
		@Override
		public @NotNull ItemStack makeIcon() {
			return new ItemStack(CONVERSION_KIT.get());
		}
	};
	static final Item.Properties I_PROPERTIES = new Item.Properties().tab(TAB);
	static final BlockBehaviour.Properties B_PROPERTIES = BlockBehaviour.Properties
			.of(Material.METAL)
			.sound(SoundType.NETHERITE_BLOCK)
			.strength(2.0F)
			.lightLevel(BlockHelper.lightValue(BlockStatePropertiesCoFH.ACTIVE, 14));

	static final DeferredRegisterCoFH<Fluid> FLUID_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.FLUIDS, MODID);
	static final DeferredRegisterCoFH<FluidType> FLUID_TYPE_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
	static final DeferredRegisterCoFH<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, MODID);
	static final DeferredRegisterCoFH<Block> BLOCK_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.BLOCKS, MODID);
	static final DeferredRegisterCoFH<Item> ITEM_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.ITEMS, MODID);
	static final DeferredRegisterCoFH<SoundEvent> SOUND_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.SOUND_EVENTS, MODID);
	static final DeferredRegisterCoFH<MenuType<?>> MENU_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.MENU_TYPES, MODID);
	static final DeferredRegisterCoFH<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
	public static final DeferredRegisterCoFH<RecipeType<?>> RECIPE_TYPE_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.RECIPE_TYPES, MODID);

	public static void init(IEventBus bus) {
		// Make sure all the inner classes have actually been static inited
		Fluids.init();
		Blocks.init();
		Items.init();
		BlockEntities.init();
		Menus.init();
		Recipes.init();
		Boilers.init();

		FLUID_REGISTRY.register(bus);
		FLUID_TYPE_REGISTRY.register(bus);
		BLOCK_ENTITY_REGISTRY.register(bus);
		BLOCK_REGISTRY.register(bus);
		ITEM_REGISTRY.register(bus);
		SOUND_REGISTRY.register(bus);
		MENU_REGISTRY.register(bus);
		RECIPE_SERIALIZER_REGISTRY.register(bus);
		RECIPE_TYPE_REGISTRY.register(bus);

		bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(Menus::registerFactories));
	}

	public static final RegistryObject<Item> CONVERSION_KIT = ITEM_REGISTRY.register("steam_conversion_kit", () -> new Item(I_PROPERTIES));

	public static final String STEAM_ID = "steam";
	public static final String STEAM_DYNAMO_ID = "steam_dynamo";
	public static final String STIRLING_BOILER_ID = "stirling_boiler";
	public static final String MAGMATIC_BOILER_ID = "magmatic_boiler";
	public static final String COMPRESSION_BOILER_ID = "compression_boiler";

	// These classes are to prevent "forward reference" compile errors. How dare they force me to be more organized
	@SuppressWarnings("ConstantConditions") // Stop it complaining about passing null to datafixer stuff
	public static class BlockEntities {
		static void init() {}

		public static final RegistryObject<BlockEntityType<?>> STEAM_DYNAMO = BLOCK_ENTITY_REGISTRY.register(STEAM_DYNAMO_ID, () -> BlockEntityType.Builder.of(SteamDynamoBlockEntity::new, Blocks.STEAM_DYNAMO.get()).build(null));
		// supply the supplier to avoid a NullPointerException
		private static <T extends BoilerBlockEntityBase> RegistryObject<BlockEntityType<?>> registerBoiler(String id, BlockEntityType.BlockEntitySupplier<T> BEConstructor, Supplier<Supplier<Block>> block) {
			return BLOCK_ENTITY_REGISTRY.register(id, () -> BlockEntityType.Builder.of(BEConstructor, block.get().get()).build(null));
		}
	}
	public static class Blocks {
		static void init() {}
		public static final RegistryObject<Block> STEAM_DYNAMO = BLOCK_REGISTRY.register(STEAM_DYNAMO_ID, () -> new TileBlockDynamo(B_PROPERTIES, SteamDynamoBlockEntity.class, BlockEntities.STEAM_DYNAMO));
	}
	public static class Items {
		static void init() {}
		public static final RegistryObject<Item> STEAM_DYNAMO = ITEM_REGISTRY.register(STEAM_DYNAMO_ID, () -> machineBlockItemOf(Blocks.STEAM_DYNAMO.get()));
}
	public static class Fluids {
		static void init() {}
		public static final ITag<Fluid> WATER = ForgeRegistries.FLUIDS.tags().getTag(ForgeRegistries.FLUIDS.tags().createTagKey(new ResourceLocation(MODID, "water")));
		public static final SteamFluid STEAM = new SteamFluid(FLUID_REGISTRY, FLUID_TYPE_REGISTRY, BLOCK_REGISTRY, ITEM_REGISTRY, STEAM_ID);
	}
	public static class Menus {
		static void init() {}
		static void registerFactories() {
			MenuScreens.register(DYNAMO_STEAM.get(), SteamDynamoScreen::new);
			MenuScreens.register(Boilers.STIRLING.menu(), StirlingBoilerScreen::new);
			MenuScreens.register(Boilers.MAGMATIC.menu(), MagmaticBoilerScreen::new);
			MenuScreens.register(Boilers.COMPRESSION.menu(), CompressionBoilerScreen::new);
		}
		public static final RegistryObject<MenuType<SteamDynamoContainer>> DYNAMO_STEAM = MENU_REGISTRY.register(STEAM_DYNAMO_ID, () -> IForgeMenuType.create(SteamDynamoContainer::new));
	}
	public static class Recipes {
		public static final RegistryObject<DynamoFuelSerializer<SteamFuel>> STEAM_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register(STEAM_ID, () -> new DynamoFuelSerializer<>(SteamFuel::new, SteamFuelManager.instance().getDefaultEnergy(), SteamFuelManager.MIN_ENERGY, SteamFuelManager.MAX_ENERGY));
		public static final RegistryObject<SerializableRecipeType<SteamFuel>> STEAM_TYPE = RECIPE_TYPE_REGISTRY.register(STEAM_ID, () -> new SerializableRecipeType<>(MODID, STEAM_ID));

		static void init() {
			ThermalRecipeManagers.registerManager(SteamFuelManager.instance());
		}
	}
	public static class Boilers {
		static void init() {}
		public static final Boiler<StirlingBoilerBlockEntity, StirlingBoilerContainer> STIRLING = new Boiler<>(STIRLING_BOILER_ID, StirlingBoilerBlockEntity.class, StirlingBoilerBlockEntity::new, StirlingBoilerContainer::new);
		public static final Boiler<MagmaticBoilerBlockEntity, MagmaticBoilerContainer> MAGMATIC = new Boiler<>(MAGMATIC_BOILER_ID, MagmaticBoilerBlockEntity.class, MagmaticBoilerBlockEntity::new, MagmaticBoilerContainer::new);
		public static final Boiler<CompressionBoilerBlockEntity, CompressionBoilerContainer> COMPRESSION = new Boiler<>(COMPRESSION_BOILER_ID, CompressionBoilerBlockEntity.class, CompressionBoilerBlockEntity::new, CompressionBoilerContainer::new);
	}



	static BlockItemAugmentable machineBlockItemOf(Block block) {
		return (BlockItemAugmentable) new BlockItemAugmentable(block, I_PROPERTIES)
						.setNumSlots(() -> ThermalCoreConfig.dynamoAugments)
						.setAugValidator(ThermalAugmentRules.DYNAMO_VALIDATOR)
						.setModId(MODID);
	}
}