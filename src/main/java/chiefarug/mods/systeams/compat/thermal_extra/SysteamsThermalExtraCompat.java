package chiefarug.mods.systeams.compat.thermal_extra;

import chiefarug.mods.systeams.Boiler;
import chiefarug.mods.systeams.ConversionKitItem;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.compat.jei.SysteamsJEIPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mrthomas20121.thermal_extra.compat.jei.ThermalExtraPlugin;
import mrthomas20121.thermal_extra.init.ThermalExtraBlocks;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class SysteamsThermalExtraCompat {

    public static String FROST_BOILER_ID = "frost_boiler";

    public static void initializeExtraThermalization(IEventBus bus) {
        Registry.init();
        if (FMLEnvironment.dist == Dist.CLIENT)
            bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(Registry.Client::initializeClientStuff));
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(SysteamsThermalExtraCompat::fillDynamoBoilerMap));
    }

    private static void fillDynamoBoilerMap() {
        ConversionKitItem.getDynamoBoilerMap().put(ThermalExtraBlocks.DYNAMO_COLD.get(), Registry.FROST.block());
    }

    public static void fillCreativeTab(CreativeModeTab.Output out) {
        out.accept(Registry.FROST);
    }

    @SuppressWarnings("unused")
    static class Registry {
        static void init() {}

        public static final Boiler<FrostBoilerBlockEntity, FrostBoilerMenu> FROST = new Boiler<>(FROST_BOILER_ID, FrostBoilerBlockEntity.class, FrostBoilerBlockEntity::new, FrostBoilerMenu::new, SysteamsRegistry.BLOCK_REGISTRY, SysteamsRegistry.ITEM_REGISTRY, SysteamsRegistry.BLOCK_ENTITY_REGISTRY, SysteamsRegistry.MENU_REGISTRY);

        static class Client {
            static void initializeClientStuff() {
                MenuScreens.register(FROST.menu(), FrostBoilerScreen::new);
            }
        }
    }

    public static class JEIHandler {
        public static void registerGuiHandlers(IGuiHandlerRegistration registration, int flameX, int flameY) {
            registration.addRecipeClickArea(FrostBoilerScreen.class, flameX, flameY, 16, 16, ThermalExtraPlugin.COLD_FUEL_TYPE);
        }

        public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
            registration.addRecipeCatalyst(Registry.FROST.item().getDefaultInstance(), ThermalExtraPlugin.COLD_FUEL_TYPE);
            registration.addRecipeCatalyst(Registry.FROST.item().getDefaultInstance(), SysteamsJEIPlugin.BOILING_RECIPE_TYPE);
        }
    }
}
