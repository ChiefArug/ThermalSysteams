package chiefarug.mods.systeams;

import chiefarug.mods.systeams.block_entities.DynamoSteamBlockEntity;
import chiefarug.mods.systeams.client.screens.DynamoSteamScreen;
import chiefarug.mods.systeams.containers.DynamoSteamContainer;
import chiefarug.mods.systeams.recipe.SteamFuel;
import chiefarug.mods.systeams.recipe.SteamFuelManager;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.constants.BlockStatePropertiesCoFH;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.recipes.SerializableRecipeType;
import cofh.thermal.core.config.ThermalCoreConfig;
import cofh.thermal.lib.block.TileBlockDynamo;
import cofh.thermal.lib.common.ThermalAugmentRules;
import cofh.thermal.lib.item.BlockItemAugmentable;
import cofh.thermal.lib.util.recipes.DynamoFuelSerializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.systeams.Systeams.MODID;

public class SysteamsRegistry {

	private static final CreativeModeTab TAB = new CreativeModeTab(MODID) {
		@Override
		public @NotNull ItemStack makeIcon() {
			return new ItemStack(CONVERSION_KIT.get());
		}
	};
	private static final Item.Properties I_PROPERTIES = new Item.Properties().tab(TAB);
	private static final BlockBehaviour.Properties B_PROPERTIES = BlockBehaviour.Properties
			.of(Material.METAL)
			.sound(SoundType.NETHERITE_BLOCK)
			.strength(2.0F)
			.lightLevel(BlockHelper.lightValue(BlockStatePropertiesCoFH.ACTIVE, 14));

	private static final DeferredRegister<Fluid> FLUID_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.FLUIDS, MODID);
	private static final DeferredRegister<FluidType> FLUID_TYPE_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, MODID);
	private static final DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.BLOCKS, MODID);
	private static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.ITEMS, MODID);
	private static final DeferredRegister<SoundEvent> SOUND_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.SOUND_EVENTS, MODID);
	private static final DeferredRegister<MenuType<?>> MENU_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.MENU_TYPES, MODID);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE_REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);

	public static void init(IEventBus bus) {
		// Make sure all of the classes have actually been static inited
		Fluids.init();
		Blocks.init();
		Items.init();
		BlockEntities.init();
		Menus.init();
		Recipes.init();

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

	// These classes are to prevent "forward reference" compile errors. How dare they force me to be more organized
	@SuppressWarnings("ConstantConditions") // Stop it complaining about passing null to datafixer stuff
	public static class BlockEntities {
		static void init() {}
		public static final RegistryObject<BlockEntityType<?>> STEAM_DYNAMO = BLOCK_ENTITY_REGISTRY.register(STEAM_DYNAMO_ID, () -> BlockEntityType.Builder.of(DynamoSteamBlockEntity::new, Blocks.STEAM_DYNAMO.get()).build(null));
	}
	public static class Blocks {
		static void init() {}
		public static final RegistryObject<Block> STEAM_DYNAMO = BLOCK_REGISTRY.register(STEAM_DYNAMO_ID, () -> new TileBlockDynamo(B_PROPERTIES, DynamoSteamBlockEntity.class, BlockEntities.STEAM_DYNAMO));
	}
	public static class Items {
		static void init() {}
		public static final RegistryObject<Item> STEAM_DYNAMO = ITEM_REGISTRY.register(STEAM_DYNAMO_ID, () -> machineBlockItemOf(Blocks.STEAM_DYNAMO.get()));
	}
	public static class Fluids { // Not fun. DO NOT TOUCH ON PAIN OF REWRITING IT
		static void init() {
			Steam.init();
		}
		// Subclasses are for organisation and getting around "forward reference" compile errors.
		public static class Steam {
			static void init() {}
			private static class Properties {
				private static final FluidType.Properties FLUID_TYPE = FluidType.Properties.create()
						.canDrown(true)
						.canExtinguish(true)
						.canHydrate(true)
						.density(1);
				private static final ForgeFlowingFluid.Properties FLUID = new ForgeFlowingFluid.Properties(Steam.FLUID_TYPE, Steam.FLUID, Steam.FLUID_FLOWING).bucket(Steam.BUCKET).block(Steam.BLOCK);
				private static final Item.Properties ITEM = new Item.Properties().tab(TAB).craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1);
				private static final BlockBehaviour.Properties BLOCK = BlockBehaviour.Properties.of(
					new Material.Builder(MaterialColor.COLOR_GRAY)
							.noCollider()
							.notSolidBlocking()
							.nonSolid()
							.destroyOnPush()
							.replaceable()
							.liquid()
							.build(),
					MaterialColor.COLOR_GRAY
				);
			}
			public static final TagKey<Fluid> TAG = FLUID_REGISTRY.createTagKey(new ResourceLocation(MODID, STEAM_ID));
			public static final RegistryObject<FlowingFluid> FLUID = FLUID_REGISTRY.register(STEAM_ID, () -> new ForgeFlowingFluid.Source(Properties.FLUID));
			public static final RegistryObject<FlowingFluid> FLUID_FLOWING = FLUID_REGISTRY.register(STEAM_ID + "_flowing", () -> new ForgeFlowingFluid.Flowing(Properties.FLUID));
			public static final RegistryObject<FluidType> FLUID_TYPE = FLUID_TYPE_REGISTRY.register(STEAM_ID, () -> new FluidType(Properties.FLUID_TYPE));
			public static final RegistryObject<Item> BUCKET = ITEM_REGISTRY.register(STEAM_ID + "_bucket", () -> new BucketItem(FLUID, Properties.ITEM));
			public static final RegistryObject<LiquidBlock> BLOCK = BLOCK_REGISTRY.register(STEAM_ID, () -> new LiquidBlock(FLUID, Properties.BLOCK));
		}
	}
	public static class Menus {
		static void init() {}
		static void registerFactories() {
			MenuScreens.register(DYNAMO_STEAM.get(), DynamoSteamScreen::new);
		}
		public static final RegistryObject<MenuType<DynamoSteamContainer>> DYNAMO_STEAM = MENU_REGISTRY.register(STEAM_DYNAMO_ID, () -> IForgeMenuType.create((windowId, inv, data) -> new DynamoSteamContainer(windowId, ProxyUtils.getClientWorld(), data.readBlockPos(), inv, ProxyUtils.getClientPlayer())));
	}
	public static class Recipes {
		static void init() {
			Types.init();
			Serializers.init();
		}
		public static class Types {
			static void init() {}
			public static final RegistryObject<SerializableRecipeType<SteamFuel>> STEAM = RECIPE_TYPE_REGISTRY.register(STEAM_ID, () -> new SerializableRecipeType<>(MODID, STEAM_ID));
		}
		public static class Serializers {
			static void init() {}
			public static final RegistryObject<DynamoFuelSerializer<SteamFuel>> STEAM = RECIPE_SERIALIZER_REGISTRY.register(STEAM_ID, () -> new DynamoFuelSerializer<>(SteamFuel::new, SteamFuelManager.instance().getDefaultEnergy(), SteamFuelManager.MIN_ENERGY, SteamFuelManager.MAX_ENERGY));
		}


	}



	private static BlockItemAugmentable machineBlockItemOf(Block block) {
		return (BlockItemAugmentable) new BlockItemAugmentable(block, I_PROPERTIES)
						.setNumSlots(() -> ThermalCoreConfig.dynamoAugments)
						.setAugValidator(ThermalAugmentRules.DYNAMO_VALIDATOR)
						.setModId(MODID);
	}
}