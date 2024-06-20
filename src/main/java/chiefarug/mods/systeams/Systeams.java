package chiefarug.mods.systeams;

import chiefarug.mods.systeams.compat.mekanism.SysteamsMekanismCompat;
import chiefarug.mods.systeams.compat.pneumaticcraft.SysteamsPNCRCompat;
import chiefarug.mods.systeams.networking.RecipeCheckerChannel;
import cofh.core.event.CoreClientEvents;
import com.mojang.logging.LogUtils;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

import static chiefarug.mods.systeams.SysteamsCommonConfig.enableEarlyGameRecipeChanges;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@Mod("systeams")
@Mod.EventBusSubscriber(modid = Systeams.MODID)
public class Systeams {
    @SuppressWarnings("unused")
    public static final Logger LGGR = LogUtils.getLogger();
    public static final String MODID = "systeams";

    public static final Capability<IAirHandlerMachine> AIR_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IGasHandler> GAS_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public Systeams() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        SysteamsRegistry.init(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SysteamsConfig.spec, "systeams-server.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SysteamsCommonConfig.spec, "systeams-common.toml");
        RecipeCheckerChannel.init();
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(ConversionKitItem::fillDynamoMap));
        bus.addListener(Systeams::addEarlyGameRecipeChangesDatapack);

        if (ModList.get().isLoaded("pneumaticcraft")) {
            SysteamsPNCRCompat.unfoldPressurizedManifold(bus);
        }
        if (ModList.get().isLoaded("mekanism")) {
            SysteamsMekanismCompat.activateMechanisedManifold(bus);
        }

        CoreClientEvents.addNamespace(MODID);
    }

    /**
     * @param first The first tag
     * @param second The second tag. Will override any duplicate values in first
     * @return A new tag
     */
    @Nullable
    @Contract("null,null -> null;!null,_ -> !null;_,!null -> !null")
    public static CompoundTag mergeTags(@Nullable CompoundTag first, @Nullable CompoundTag second) {
        if (first == null && second == null) return null;
        if (first == null)
            return second.copy();
        else if (second == null)
            return first.copy();
        return first.copy().merge(second);
    }

    private static boolean first = true;
    @SubscribeEvent(priority = LOWEST)
    static void loginLogger(PlayerEvent.PlayerLoggedInEvent _e) {
        // if you recognise the quote, good job
        if (first) {
            LGGR.info("Welcome aboard Captain. All Systeams online");
            first = false;
        }
    }

    static void addEarlyGameRecipeChangesDatapack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA || !enableEarlyGameRecipeChanges.get()) return;
        String path = "early_game_data";
        String name = MODID + ":" + path;
        PackResources resources = new PathPackResources(name, ModList.get().getModFileById(MODID).getFile().findResource(path));
        event.addRepositorySource((consumer, constructor) -> {
            try {
                consumer.accept(constructor.create(
                        name, Component.literal("Systeams Early Game Recipes").withStyle(ChatFormatting.GRAY), true,
                        () -> resources, resources.getMetadataSection(PackMetadataSection.SERIALIZER),
                        Pack.Position.BOTTOM, PackSource.BUILT_IN, false)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
