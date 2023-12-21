package chiefarug.mods.systeams.compat.pneumaticcraft;

import chiefarug.mods.systeams.ConversionKitItem;
import chiefarug.mods.systeams.SysteamsConfig;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.block.BoilerBlock;
import cofh.core.common.config.CoreClientConfig;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.StringHelper;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class SysteamsPNCRCompat {

	public static final String PNEUMATIC_BOILER_ID = "pneumatic_boiler";

	public static void unfoldPressurizedManifold(IEventBus bus) {
		Registry.init();
		if (FMLEnvironment.dist == Dist.CLIENT) {
			bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(Registry.Client::initializeClientStuff));
		}
		bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(SysteamsPNCRCompat::fillDynamoBoilerMap));
		MinecraftForge.EVENT_BUS.addListener(SysteamsPNCRCompat::tooltipEvent);
	}

	private static void fillDynamoBoilerMap() {
		ConversionKitItem.getDynamoBoilerMap().put(ModBlocks.PNEUMATIC_DYNAMO.get(), Registry.PNEUMATIC_BOILER_BLOCK.get());
	}

	@SuppressWarnings("unused")
	static class Registry {
		static void init() {
		}

		public static final RegistryObject<BoilerBlock> PNEUMATIC_BOILER_BLOCK = SysteamsRegistry.BLOCK_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> new PneumaticBoilerBlock(SysteamsRegistry.B_PROPERTIES, PneumaticBoilerBlockEntity.class, Registry::pneumaticBoilerBE));
		public static final RegistryObject<BlockEntityType<?>> PNEUMATIC_BOILER_BLOCK_ENTITY = SysteamsRegistry.BLOCK_ENTITY_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> BlockEntityType.Builder.of(PneumaticBoilerBlockEntity::new, PNEUMATIC_BOILER_BLOCK.get()).build(null));
		public static final RegistryObject<Item> PNEUMATIC_BOILER_BLOCK_ITEM = SysteamsRegistry.ITEM_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> SysteamsRegistry.machineBlockItemOf(PNEUMATIC_BOILER_BLOCK.get()));
		public static final RegistryObject<MenuType<PneumaticBoilerMenu>> PNEUMATIC_BOILER_MENU = SysteamsRegistry.MENU_REGISTRY.register(PNEUMATIC_BOILER_ID, () -> IForgeMenuType.create(PneumaticBoilerMenu::new));

		private static BlockEntityType<?> pneumaticBoilerBE() {
			return PNEUMATIC_BOILER_BLOCK_ENTITY.get();
		}

		static class Client {
			public static TextureAtlasSprite AIR_ICON;
			public static final ResourceLocation AIR_ICON_LOCATION = new ResourceLocation("mob_effect/water_breathing");

			static void initializeClientStuff() {
				MenuScreens.register(PNEUMATIC_BOILER_MENU.get(), PneumaticBoilerScreen::new);
			}

		}
	}

	static void tooltipEvent(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        String modid = Utils.getModId(stack.getItem());
        if (!(modid.equals(Names.MOD_ID))) return;


        if (CoreClientConfig.enableKeywords.get()) {
            String translationKey = stack.getDescriptionId() + ".keyword";
            if (StringHelper.canLocalize(translationKey)) {
                if (tooltip.get(0) instanceof MutableComponent mutable) {
                    mutable.append(StringHelper.getKeywordTextComponent(translationKey));
                }
            }
        }
		if (CoreClientConfig.enableItemDescriptions.get() && SysteamsConfig.PNEUMATIC_BOILER_IN_WORLD_CONVERSION.get()) {
			if (stack.getItem().equals(ModBlocks.ADVANCED_PRESSURE_TUBE.get().asItem())) {
				tooltip.add(Component.translatable(stack.getDescriptionId() + ".desc").withStyle(ChatFormatting.GOLD));
			}
		}
	}
}
