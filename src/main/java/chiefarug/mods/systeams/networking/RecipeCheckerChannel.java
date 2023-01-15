package chiefarug.mods.systeams.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;

import static chiefarug.mods.systeams.Systeams.MODID;

// This is purely to stop clients connecting that have a version with different recipe serialization, to avoid garbage network errors
public class RecipeCheckerChannel {
	private static final String VERSION = "1";

	public static void init() {
		// don't bother storing this in a variable. we don't need it to send packets anyway
		NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "recipe_version_check"), () -> VERSION, VERSION::equals, VERSION::equals);
	}
}
