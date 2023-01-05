package chiefarug.mods.systeams;

import cofh.core.config.CoreClientConfig;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.StringHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

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


    /*
    Stuff to do:

    Steam fluid. Should rise up
    Steam dynamos
    Steam conversion kits
    Steam conversion recipes
     */

    @SubscribeEvent
    static void tooltipSearch(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        // copy this because the default check only does cofh namespaces
        if (CoreClientConfig.enableKeywords.get() && Utils.getModId(stack.getItem()).equals(MODID)) {
            String translationKey = stack.getDescriptionId() + ".keyword";
            if (StringHelper.canLocalize(translationKey)) {
                if (tooltip.get(0) instanceof MutableComponent mutable) {
                    mutable.append(StringHelper.getKeywordTextComponent(translationKey));
                }
            }
        }
    }

}
