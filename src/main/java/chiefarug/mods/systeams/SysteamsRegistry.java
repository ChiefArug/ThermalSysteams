package chiefarug.mods.systeams;

import chiefarug.mods.systeams.block_entities.BoilerBlockEntityBase;
import chiefarug.mods.systeams.block_entities.CompressionBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.DisenchantmentBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.GourmandBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.LapidaryBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.MagmaticBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.NumismaticBoilerBlockEntity;
import chiefarug.mods.systeams.block_entities.SteamDynamoBlockEntity;
import chiefarug.mods.systeams.block_entities.StirlingBoilerBlockEntity;
import chiefarug.mods.systeams.client.screens.CompressionBoilerScreen;
import chiefarug.mods.systeams.client.screens.DisenchantmentBoilerScreen;
import chiefarug.mods.systeams.client.screens.GourmandBoilerScreen;
import chiefarug.mods.systeams.client.screens.LapidaryBoilerScreen;
import chiefarug.mods.systeams.client.screens.MagmaticBoilerScreen;
import chiefarug.mods.systeams.client.screens.NumismaticBoilerScreen;
import chiefarug.mods.systeams.client.screens.SteamDynamoScreen;
import chiefarug.mods.systeams.client.screens.StirlingBoilerScreen;
import chiefarug.mods.systeams.containers.CompressionBoilerContainer;
import chiefarug.mods.systeams.containers.DisenchantmentBoilerContainer;
import chiefarug.mods.systeams.containers.GourmandBoilerContainer;
import chiefarug.mods.systeams.containers.LapidaryBoilerContainer;
import chiefarug.mods.systeams.containers.MagmaticBoilerContainer;
import chiefarug.mods.systeams.containers.NumismaticBoilerContainer;
import chiefarug.mods.systeams.containers.SteamDynamoContainer;
import chiefarug.mods.systeams.containers.StirlingBoilerContainer;
import chiefarug.mods.systeams.recipe.SteamFuel;
import chiefarug.mods.systeams.recipe.SteamFuelManager;
import chiefarug.mods.systeams.recipe.UpgradeShapelessRecipe;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.lib.util.constants.BlockStatePropertiesCoFH;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.recipes.SerializableRecipeType;
import cofh.thermal.core.ThermalCore;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.lib.block.DynamoBlock;
import cofh.thermal.lib.common.ThermalAugmentRules;
import cofh.thermal.lib.common.ThermalRecipeManagers;
import cofh.thermal.lib.item.BlockItemAugmentable;
import cofh.thermal.lib.util.recipes.DynamoFuelSerializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
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
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static chiefarug.mods.systeams.Systeams.MODID;

@SuppressWarnings({"unused", "SameParameterValue"})
public class SysteamsRegistry {

	static final CreativeModeTab TAB = new CreativeModeTab(MODID) {
		@Override
		public @NotNull ItemStack makeIcon() {
			return new ItemStack(Items.BOILER_PIPE.get());
		}
	};
	static final Item.Properties I_PROPERTIES = new Item.Properties().tab(TAB);
	static final BlockBehaviour.Properties B_PROPERTIES = BlockBehaviour.Properties
			.of(Material.METAL)
			.sound(SoundType.NETHERITE_BLOCK)
			.strength(2.0F)
			.lightLevel(BlockHelper.lightValue(BlockStatePropertiesCoFH.ACTIVE, 14));

	static final DeferredRegisterCoFH<Fluid> FLUID_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.FLUIDS, MODID);
	static final DeferredRegisterCoFH<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, MODID);
	static final DeferredRegisterCoFH<Block> BLOCK_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.BLOCKS, MODID);
	static final DeferredRegisterCoFH<Item> ITEM_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.ITEMS, MODID);
	static final DeferredRegisterCoFH<SoundEvent> SOUND_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.SOUND_EVENTS, MODID);
	static final DeferredRegisterCoFH<MenuType<?>> MENU_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.CONTAINER_TYPES, MODID);
	static final DeferredRegisterCoFH<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

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
		BLOCK_ENTITY_REGISTRY.register(bus);
		BLOCK_REGISTRY.register(bus);
		ITEM_REGISTRY.register(bus);
		SOUND_REGISTRY.register(bus);
		MENU_REGISTRY.register(bus);
		RECIPE_SERIALIZER_REGISTRY.register(bus);

		bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(Menus::registerFactories));
		bus.addListener(Recipes::registerRecipeTypes);
	}

	public static final ResourceLocation STEAM_ID = new ResourceLocation(MODID, "steam");
	public static final String STEAM_DYNAMO_ID = "steam_dynamo";
	public static final String STIRLING_BOILER_ID = "stirling_boiler";
	public static final String MAGMATIC_BOILER_ID = "magmatic_boiler";
	public static final String COMPRESSION_BOILER_ID = "compression_boiler";
	public static final String NUMISMATIC_BOILER_ID = "numismatic_boiler";
	public static final String LAPIDARY_BOILER_ID = "lapidary_boiler";
	public static final String DISENCHANTMENT_BOILER_ID = "disenchantment_boiler";
	public static final String GOURMAND_BOILER_ID = "gourmand_boiler";
	public static final String UPGRADE_RECIPE_ID = "upgrade_shapeless";

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
		public static final RegistryObject<Block> STEAM_DYNAMO = BLOCK_REGISTRY.register(STEAM_DYNAMO_ID, () -> new DynamoBlock(B_PROPERTIES, SteamDynamoBlockEntity.class, BlockEntities.STEAM_DYNAMO));
	}
	public static class Items {
		static void init() {}
		public static final Supplier<Item> RF_COIL = () -> ThermalCore.ITEMS.get("thermal:rf_coil");
		public static final ITag<Item> UPGRADE_MAIN = modTag(ForgeRegistries.ITEMS, "recipe_control/upgrade_main");

		public static final RegistryObject<Item> STEAM_DYNAMO = ITEM_REGISTRY.register(STEAM_DYNAMO_ID, () -> machineBlockItemOf(Blocks.STEAM_DYNAMO.get()));
		public static final RegistryObject<ConversionKitItem> BOILER_PIPE = ITEM_REGISTRY.register("boiler_pipe", () -> new ConversionKitItem(new Item.Properties().tab(TAB)));
}
	public static class Fluids {
		static void init() {}
		public static final ITag<Fluid> WATER_TAG = modTag(ForgeRegistries.FLUIDS, "water");
		public static final ITag<Fluid> STEAM_TAG = forgeTag(ForgeRegistries.FLUIDS, "steam");
		public static final SteamFluid STEAM = new SteamFluid(FLUID_REGISTRY, BLOCK_REGISTRY, ITEM_REGISTRY, STEAM_ID.getPath());
	}
	public static class Menus {
		static void init() {}
		static void registerFactories() {
			MenuScreens.register(DYNAMO_STEAM.get(), SteamDynamoScreen::new);
			MenuScreens.register(Boilers.STIRLING.menu(), StirlingBoilerScreen::new);
			MenuScreens.register(Boilers.MAGMATIC.menu(), MagmaticBoilerScreen::new);
			MenuScreens.register(Boilers.COMPRESSION.menu(), CompressionBoilerScreen::new);
			MenuScreens.register(Boilers.NUMISMATIC.menu(), NumismaticBoilerScreen::new);
			MenuScreens.register(Boilers.LAPIDARY.menu(), LapidaryBoilerScreen::new);
			MenuScreens.register(Boilers.DISENCHANTMENT.menu(), DisenchantmentBoilerScreen::new);
			MenuScreens.register(Boilers.GOURMAND.menu(), GourmandBoilerScreen::new);
		}
		public static final RegistryObject<MenuType<SteamDynamoContainer>> DYNAMO_STEAM = MENU_REGISTRY.register(STEAM_DYNAMO_ID, () -> IForgeMenuType.create(SteamDynamoContainer::new));
	}
	public static class Recipes {

		public static void registerRecipeTypes(FMLCommonSetupEvent event) {
			STEAM_TYPE = Registry.register(Registry.RECIPE_TYPE, STEAM_ID, new SerializableRecipeType<>(STEAM_ID));
			UPGRADE_TYPE = Registry.register(Registry.RECIPE_TYPE, UPGRADE_RECIPE_ID, new RecipeType<UpgradeShapelessRecipe>() {
				@Override
				public String toString() {
					return UPGRADE_RECIPE_ID.toString();
				}
			});
		}

		public static final RegistryObject<DynamoFuelSerializer<SteamFuel>> STEAM_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register(STEAM_ID, () -> new DynamoFuelSerializer<>(SteamFuel::new, SteamFuelManager.instance().getDefaultEnergy(), SteamFuelManager.MIN_ENERGY, SteamFuelManager.MAX_ENERGY));
		public static SerializableRecipeType<SteamFuel> STEAM_TYPE;

		public static final RegistryObject<UpgradeShapelessRecipe.Serializer> UPGRADE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register(UPGRADE_RECIPE_ID, UpgradeShapelessRecipe.Serializer::new);
		public static RecipeType<UpgradeShapelessRecipe> UPGRADE_TYPE;

		static void init() {
			ThermalRecipeManagers.registerManager(SteamFuelManager.instance());
		}
	}
	public static class Boilers {
		static void init() {}
		public static final Boiler<StirlingBoilerBlockEntity, StirlingBoilerContainer> STIRLING = new Boiler<>(STIRLING_BOILER_ID, StirlingBoilerBlockEntity.class, StirlingBoilerBlockEntity::new, StirlingBoilerContainer::new);
		public static final Boiler<MagmaticBoilerBlockEntity, MagmaticBoilerContainer> MAGMATIC = new Boiler<>(MAGMATIC_BOILER_ID, MagmaticBoilerBlockEntity.class, MagmaticBoilerBlockEntity::new, MagmaticBoilerContainer::new);
		public static final Boiler<CompressionBoilerBlockEntity, CompressionBoilerContainer> COMPRESSION = new Boiler<>(COMPRESSION_BOILER_ID, CompressionBoilerBlockEntity.class, CompressionBoilerBlockEntity::new, CompressionBoilerContainer::new);
		public static final Boiler<NumismaticBoilerBlockEntity, NumismaticBoilerContainer> NUMISMATIC = new Boiler<>(NUMISMATIC_BOILER_ID, NumismaticBoilerBlockEntity.class, NumismaticBoilerBlockEntity::new, NumismaticBoilerContainer::new);
		public static final Boiler<LapidaryBoilerBlockEntity, LapidaryBoilerContainer> LAPIDARY = new Boiler<>(LAPIDARY_BOILER_ID, LapidaryBoilerBlockEntity.class, LapidaryBoilerBlockEntity::new, LapidaryBoilerContainer::new);
		public static final Boiler<DisenchantmentBoilerBlockEntity, DisenchantmentBoilerContainer> DISENCHANTMENT = new Boiler<>(DISENCHANTMENT_BOILER_ID, DisenchantmentBoilerBlockEntity.class, DisenchantmentBoilerBlockEntity::new, DisenchantmentBoilerContainer::new);
		public static final Boiler<GourmandBoilerBlockEntity, GourmandBoilerContainer> GOURMAND = new Boiler<>(GOURMAND_BOILER_ID, GourmandBoilerBlockEntity.class, GourmandBoilerBlockEntity::new, GourmandBoilerContainer::new);
	}


	static BlockItemAugmentable machineBlockItemOf(Block block) {
		return (BlockItemAugmentable) new BlockItemAugmentable(block, I_PROPERTIES)
						.setNumSlots(() -> ThermalCoreConfig.dynamoAugments)
						.setAugValidator(ThermalAugmentRules.DYNAMO_VALIDATOR)
						.setModId(MODID);
	}


	static <T extends IForgeRegistryEntry<T>> ITag<T> modTag(IForgeRegistry<T> registry, String key) {
		return tag(registry, new ResourceLocation(MODID, key));
	}

	static <T extends IForgeRegistryEntry<T>> ITag<T> forgeTag(IForgeRegistry<T> registry, String key) {
		return tag(registry, new ResourceLocation("forge", key));
	}

	static <T extends IForgeRegistryEntry<T>> ITag<T> tag(IForgeRegistry<T> registry, ResourceLocation key) {
		ITagManager<T> manager = registry.tags();
		if (manager == null) throw new IllegalArgumentException("Registry " + registry.getRegistryKey() + " does not support tags");

		return manager.getTag(manager.createTagKey(key));
	}
}