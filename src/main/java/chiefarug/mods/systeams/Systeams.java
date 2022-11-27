package chiefarug.mods.systeams;

import cofh.lib.util.constants.ModIds;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("systeams")
public class Systeams {
    @SuppressWarnings("unused")
    private static final Logger LGGR = LogUtils.getLogger();

    public Systeams() {
        FMLJavaModLoadingContext.get().getModEventBus();
        LGGR.info(ModIds.ID_THERMAL_EXPANSION);
    }


    /*
    Stuff to do:

    Steam fluid. Should rise up
    Steam dynamos
    Steam conversion kits
    Steam conversion recipes
     */



}
