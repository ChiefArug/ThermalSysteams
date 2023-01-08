package chiefarug.mods.systeams;

import cofh.core.config.CoreClientConfig;
import cofh.lib.util.Utils;
import cofh.lib.util.constants.ModIds;
import cofh.lib.util.helpers.StringHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("systeams")
@Mod.EventBusSubscriber(modid = Systeams.MODID)
public class Systeams {
    @SuppressWarnings("unused")
    public static final Logger LGGR = LogUtils.getLogger();
    public static final String MODID = "systeams";

    public Systeams() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        SysteamsRegistry.init(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SysteamsConfig.spec, "systeams-server.toml");
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

    @SubscribeEvent
    static void tooltipEvent(ItemTooltipEvent event) {
        // copy this because the default check only does cofh namespaces
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        String modid = Utils.getModId(stack.getItem());
        if (!(modid.equals(MODID) || /*CoreClientEvents.NAMESPACES*/ modid.equals(ModIds.ID_THERMAL))) return;


        if (CoreClientConfig.enableKeywords.get()) {
            String translationKey = stack.getDescriptionId() + ".keyword";
            if (StringHelper.canLocalize(translationKey)) {
                if (tooltip.get(0) instanceof MutableComponent mutable) {
                    mutable.append(StringHelper.getKeywordTextComponent(translationKey));
                }
            }
        }

        if (CoreClientConfig.enableItemDescriptions.get()) {
            if (stack.getItem().equals(SysteamsRegistry.Items.RF_COIL.get())) {
                tooltip.add(Component.translatable(stack.getDescriptionId() + ".desc").withStyle(ChatFormatting.GOLD));
            }
        }
    }

}
