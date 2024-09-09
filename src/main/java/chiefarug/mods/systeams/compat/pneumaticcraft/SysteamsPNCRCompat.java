package chiefarug.mods.systeams.compat.pneumaticcraft;

import chiefarug.mods.systeams.ConversionKitItem;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.block.BoilerBlock;
import cofh.core.event.CoreClientEvents;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;

public class SysteamsPNCRCompat {

	public static final String PNEUMATIC_BOILER_ID = "pneumatic_boiler";
	//TODO: add pneumatic boiler to mineable tag. how on earth did i forget that
	public static void unfoldPressurizedManifold(IEventBus bus) {
		Registry.init();
		if (FMLEnvironment.dist == Dist.CLIENT) {
			bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(Registry.Client::initializeClientStuff));
			bus.addListener(Registry.Client::preStitch);
			bus.addListener(Registry.Client::postStitch);
		}
		bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(SysteamsPNCRCompat::fillDynamoBoilerMap));
		CoreClientEvents.addNamespace(Names.MOD_ID);
	}

	private static void fillDynamoBoilerMap() {
		ConversionKitItem.addToConversions(ModBlocks.PNEUMATIC_DYNAMO.get(), Registry.PNEUMATIC_BOILER_BLOCK.get(), ModBlocks.ADVANCED_PRESSURE_TUBE.get());
	}

	@SuppressWarnings("unused")
	static class Registry {
		static void init() {
		}

		public static final RegistryObject<BoilerBlock> PNEUMATIC_BOILER_BLOCK = SysteamsRegistry.BLOCK_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> new PneumaticBoilerBlock(SysteamsRegistry.B_PROPERTIES, PneumaticBoilerBlockEntity.class, Registry::pneumaticBoilerBE));
		public static final RegistryObject<BlockEntityType<?>> PNEUMATIC_BOILER_BLOCK_ENTITY = SysteamsRegistry.BLOCK_ENTITY_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> BlockEntityType.Builder.of(PneumaticBoilerBlockEntity::new, PNEUMATIC_BOILER_BLOCK.get()).build(null));
		public static final RegistryObject<Item> PNEUMATIC_BOILER_BLOCK_ITEM = SysteamsRegistry.ITEM_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> SysteamsRegistry.dynamoishBlockItemOf(PNEUMATIC_BOILER_BLOCK.get()));
		public static final RegistryObject<MenuType<PneumaticBoilerContainer>> PNEUMATIC_BOILER_MENU = SysteamsRegistry.MENU_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> IForgeMenuType.create(PneumaticBoilerContainer::new));

		private static BlockEntityType<?> pneumaticBoilerBE() {
			return PNEUMATIC_BOILER_BLOCK_ENTITY.get();
		}

		static class Client {
			public static TextureAtlasSprite AIR_ICON;
			private static final ResourceLocation AIR_ICON_LOCATION = new ResourceLocation("mob_effect/water_breathing");

			static void initializeClientStuff() {
				MenuScreens.register(PNEUMATIC_BOILER_MENU.get(), PneumaticBoilerScreen::new);
			}

			static void preStitch(TextureStitchEvent.Pre event) {
				if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
					return;
				}
				event.addSprite(AIR_ICON_LOCATION);
			}

			static void postStitch(TextureStitchEvent.Post event) {
				if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
					return;
				}
				AIR_ICON = event.getAtlas().getSprite(AIR_ICON_LOCATION);
			}
		}
	}
}
