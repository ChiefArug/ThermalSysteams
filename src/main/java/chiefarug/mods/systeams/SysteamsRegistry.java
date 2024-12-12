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
import chiefarug.mods.systeams.compat.pneumaticcraft.SysteamsPNCRCompat;
import chiefarug.mods.systeams.compat.thermal_extra.SysteamsThermalExtraCompat;
import chiefarug.mods.systeams.containers.CompressionBoilerMenu;
import chiefarug.mods.systeams.containers.DisenchantmentBoilerMenu;
import chiefarug.mods.systeams.containers.GourmandBoilerMenu;
import chiefarug.mods.systeams.containers.LapidaryBoilerMenu;
import chiefarug.mods.systeams.containers.MagmaticBoilerMenu;
import chiefarug.mods.systeams.containers.NumismaticBoilerMenu;
import chiefarug.mods.systeams.containers.SteamDynamoMenu;
import chiefarug.mods.systeams.containers.StirlingBoilerMenu;
import chiefarug.mods.systeams.fluid.PlasmaFluid;
import chiefarug.mods.systeams.fluid.SteamFluid;
import chiefarug.mods.systeams.recipe.BoilingRecipe;
import chiefarug.mods.systeams.recipe.BoilingRecipeManager;
import chiefarug.mods.systeams.recipe.SteamFuel;
import chiefarug.mods.systeams.recipe.SteamFuelManager;
import chiefarug.mods.systeams.recipe.UpgradeShapelessRecipe;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.lib.util.constants.BlockStatePropertiesCoFH;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.recipes.SerializableRecipeType;
import cofh.thermal.core.ThermalCore;
import cofh.thermal.core.common.config.ThermalCoreConfig;
import cofh.thermal.lib.common.block.DynamoBlock;
import cofh.thermal.lib.common.item.BlockItemAugmentable;
import cofh.thermal.lib.util.ThermalAugmentRules;
import cofh.thermal.lib.util.ThermalRecipeManagers;
import cofh.thermal.lib.util.recipes.DynamoFuelSerializer;
import cofh.thermal.lib.util.recipes.MachineRecipeSerializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.function.Supplier;

import static chiefarug.mods.systeams.Systeams.EXTRA;
import static chiefarug.mods.systeams.Systeams.MODID;
import static chiefarug.mods.systeams.Systeams.MODRL;
import static chiefarug.mods.systeams.Systeams.PNCR;

@SuppressWarnings("unused")
public class SysteamsRegistry {

	public static final DeferredRegisterCoFH<CreativeModeTab> TAB_REGISTRY = DeferredRegisterCoFH.create(Registries.CREATIVE_MODE_TAB, MODID);
	public static final RegistryObject<CreativeModeTab> TAB = TAB_REGISTRY.register(MODID, () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.systeams"))
			.icon(() -> new ItemStack(Items.BOILER_PIPE.get()))
			.displayItems((p, out) -> {
				out.accept(Items.BOILER_PIPE.get());
				out.accept(Items.STEAM_DYNAMO.get());
				out.accept(Boilers.COMPRESSION);
				out.accept(Boilers.GOURMAND);
				out.accept(Boilers.LAPIDARY);
				out.accept(Boilers.MAGMATIC);
				out.accept(Boilers.STIRLING);
				out.accept(Boilers.NUMISMATIC);
				out.accept(Boilers.DISENCHANTMENT);

				ModList mods = ModList.get();

				if (mods.isLoaded(PNCR)) SysteamsPNCRCompat.fillCreativeTab(out);

				if (mods.isLoaded(EXTRA)) SysteamsThermalExtraCompat.fillCreativeTab(out);

				out.accept(Fluids.STEAM.getStill().getBucket());
				out.accept(Fluids.STEAM_2.getStill().getBucket());
				out.accept(Fluids.STEAM_3.getStill().getBucket());
				out.accept(Fluids.STEAM_4.getStill().getBucket());
				out.accept(Fluids.STEAM_5.getStill().getBucket());
			})
			.build());

	public static final Item.Properties I_PROPERTIES = new Item.Properties();
	public static final BlockBehaviour.Properties B_PROPERTIES = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.NETHERITE_BLOCK)
			.strength(2.0F)
			.lightLevel(BlockHelper.lightValue(BlockStatePropertiesCoFH.ACTIVE, 14));

	public static final DeferredRegisterCoFH<Fluid> FLUID_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.FLUIDS, MODID);
	public static final DeferredRegisterCoFH<FluidType> FLUID_TYPE_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
	public static final DeferredRegisterCoFH<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, MODID);
	public static final DeferredRegisterCoFH<Block> BLOCK_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.BLOCKS, MODID);
	public static final DeferredRegisterCoFH<Item> ITEM_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.ITEMS, MODID);
	public static final DeferredRegisterCoFH<SoundEvent> SOUND_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.SOUND_EVENTS, MODID);
	public static final DeferredRegisterCoFH<MenuType<?>> MENU_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.Keys.MENU_TYPES, MODID);
	public static final DeferredRegisterCoFH<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = DeferredRegisterCoFH.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
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

		TAB_REGISTRY.register(bus);
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

	public static final String STEAM_ID = "steam";
	public static final String STEAM_DYNAMO_ID = "steam_dynamo";
	public static final String STIRLING_BOILER_ID = "stirling_boiler";
	public static final String MAGMATIC_BOILER_ID = "magmatic_boiler";
	public static final String COMPRESSION_BOILER_ID = "compression_boiler";
	public static final String NUMISMATIC_BOILER_ID = "numismatic_boiler";
	public static final String LAPIDARY_BOILER_ID = "lapidary_boiler";
	public static final String DISENCHANTMENT_BOILER_ID = "disenchantment_boiler";
	public static final String GOURMAND_BOILER_ID = "gourmand_boiler";
	public static final String UPGRADE_RECIPE_ID = "upgrade_shapeless";
	public static final String BOILING_ID = "boiling";

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
		public static final ItemLike RF_COIL = () -> ThermalCore.ITEMS.get("thermal:rf_coil");
		public static final ITag<Item> UPGRADE_MAIN = modTag(ForgeRegistries.ITEMS, "recipe_control/upgrade_main");

		public static final RegistryObject<Item> STEAM_DYNAMO = ITEM_REGISTRY.register(STEAM_DYNAMO_ID, () -> machineBlockItemOf(Blocks.STEAM_DYNAMO.get()));
		public static final RegistryObject<ConversionKitItem> BOILER_PIPE = ITEM_REGISTRY.register("boiler_pipe", () -> new ConversionKitItem(I_PROPERTIES));
}
	public static class Fluids {
		static void init() {}
		public static final ITag<Fluid> WATER_TAG = modTag(ForgeRegistries.FLUIDS, "water");
		public static final ITag<Fluid> STEAMISH_TAG = modTag(ForgeRegistries.FLUIDS, "steamish");
		public static final SteamFluid STEAM = new SteamFluid(FLUID_REGISTRY, FLUID_TYPE_REGISTRY, null, ITEM_REGISTRY, STEAM_ID);
		public static final SteamFluid STEAM_2 = new SteamFluid(FLUID_REGISTRY, FLUID_TYPE_REGISTRY, null, ITEM_REGISTRY, "steamier");
		public static final SteamFluid STEAM_3 = new SteamFluid(FLUID_REGISTRY, FLUID_TYPE_REGISTRY, null, ITEM_REGISTRY, "steamiest");
		public static final SteamFluid STEAM_4 = new SteamFluid(FLUID_REGISTRY, FLUID_TYPE_REGISTRY, null, ITEM_REGISTRY, "steamiester");
		public static final SteamFluid STEAM_5 = new PlasmaFluid(FLUID_REGISTRY, FLUID_TYPE_REGISTRY, null, ITEM_REGISTRY, "steamiestest");
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
		public static final RegistryObject<MenuType<SteamDynamoMenu>> DYNAMO_STEAM = MENU_REGISTRY.register(STEAM_DYNAMO_ID, () -> IForgeMenuType.create(SteamDynamoMenu::new));
	}
	public static class Recipes {
		public static final RegistryObject<DynamoFuelSerializer<SteamFuel>> STEAM_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register(STEAM_ID, () -> new DynamoFuelSerializer<>(SteamFuel::new, SteamFuelManager.instance().getDefaultEnergy(), SteamFuelManager.MIN_ENERGY, SteamFuelManager.MAX_ENERGY));
		public static final RegistryObject<SerializableRecipeType<SteamFuel>> STEAM_TYPE = RECIPE_TYPE_REGISTRY.register(STEAM_ID, () -> new SerializableRecipeType<>(MODID, STEAM_ID));

		public static final RegistryObject<UpgradeShapelessRecipe.Serializer> UPGRADE_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register(UPGRADE_RECIPE_ID, UpgradeShapelessRecipe.Serializer::new);
		public static final RegistryObject<RecipeType<UpgradeShapelessRecipe>> UPGRADE_TYPE = RECIPE_TYPE_REGISTRY.register(UPGRADE_RECIPE_ID, () -> new RecipeType<>() {
			@Override
			public String toString() {
				return MODID + ':' + UPGRADE_RECIPE_ID;
			}
		});

		public static final int MARKER_ENERGY = 10138; // this marks 'no energy' for boiling recipes, as they do not take an energy but thermal will throw an error if energu == 0.
		public static final RegistryObject<MachineRecipeSerializer<BoilingRecipe>> BOILING_SERIALIZER = RECIPE_SERIALIZER_REGISTRY.register(BOILING_ID, () -> new MachineRecipeSerializer<>(BoilingRecipe::new, MARKER_ENERGY));
		public static final RegistryObject<SerializableRecipeType<BoilingRecipe>> BOILING_TYPE = RECIPE_TYPE_REGISTRY.register(BOILING_ID, () -> new SerializableRecipeType<>(MODID, BOILING_ID));

		static void init() {
			ThermalRecipeManagers.registerManager(SteamFuelManager.instance());
			ThermalRecipeManagers.registerManager(BoilingRecipeManager.instance());
		}
	}
	public static class Boilers {
		static void init() {}
		public static final Boiler<StirlingBoilerBlockEntity, StirlingBoilerMenu> STIRLING = new Boiler<>(STIRLING_BOILER_ID, StirlingBoilerBlockEntity.class, StirlingBoilerBlockEntity::new, StirlingBoilerMenu::new);
		public static final Boiler<MagmaticBoilerBlockEntity, MagmaticBoilerMenu> MAGMATIC = new Boiler<>(MAGMATIC_BOILER_ID, MagmaticBoilerBlockEntity.class, MagmaticBoilerBlockEntity::new, MagmaticBoilerMenu::new);
		public static final Boiler<CompressionBoilerBlockEntity, CompressionBoilerMenu> COMPRESSION = new Boiler<>(COMPRESSION_BOILER_ID, CompressionBoilerBlockEntity.class, CompressionBoilerBlockEntity::new, CompressionBoilerMenu::new);
		public static final Boiler<NumismaticBoilerBlockEntity, NumismaticBoilerMenu> NUMISMATIC = new Boiler<>(NUMISMATIC_BOILER_ID, NumismaticBoilerBlockEntity.class, NumismaticBoilerBlockEntity::new, NumismaticBoilerMenu::new);
		public static final Boiler<LapidaryBoilerBlockEntity, LapidaryBoilerMenu> LAPIDARY = new Boiler<>(LAPIDARY_BOILER_ID, LapidaryBoilerBlockEntity.class, LapidaryBoilerBlockEntity::new, LapidaryBoilerMenu::new);
		public static final Boiler<DisenchantmentBoilerBlockEntity, DisenchantmentBoilerMenu> DISENCHANTMENT = new Boiler<>(DISENCHANTMENT_BOILER_ID, DisenchantmentBoilerBlockEntity.class, DisenchantmentBoilerBlockEntity::new, DisenchantmentBoilerMenu::new);
		public static final Boiler<GourmandBoilerBlockEntity, GourmandBoilerMenu> GOURMAND = new Boiler<>(GOURMAND_BOILER_ID, GourmandBoilerBlockEntity.class, GourmandBoilerBlockEntity::new, GourmandBoilerMenu::new);
	}


	public static BlockItemAugmentable machineBlockItemOf(Block block) {
		return (BlockItemAugmentable) new BlockItemAugmentable(block, I_PROPERTIES)
						.setNumSlots(() -> ThermalCoreConfig.dynamoAugments)
						.setAugValidator(ThermalAugmentRules.DYNAMO_VALIDATOR)
						.setModId(MODID);
	}


	static <T> ITag<T> modTag(IForgeRegistry<T> registry, String key) {
		return tag(registry, MODRL.withPath(key));
	}

	static <T> ITag<T> forgeTag(IForgeRegistry<T> registry, String key) {
		return tag(registry, new ResourceLocation("forge", key));
	}

	static <T> ITag<T> tag(IForgeRegistry<T> registry, ResourceLocation key) {
		ITagManager<T> manager = registry.tags();
		if (manager == null) throw new IllegalArgumentException("Registry " + registry.getRegistryKey() + " does not support tags");

		return manager.getTag(manager.createTagKey(key));
	}
}