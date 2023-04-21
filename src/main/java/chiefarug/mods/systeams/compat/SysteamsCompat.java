package chiefarug.mods.systeams.compat;

import chiefarug.mods.systeams.compat.create.SysteamsCreateCompat;
import chiefarug.mods.systeams.compat.pneumaticcraft.SysteamsPNCRCompat;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

public class SysteamsCompat {

	public static final String PNEUMATICCRAFT = "pneumaticcraft";
	public static final String CREATE = "create";

	public static void loadCompats(IEventBus modBus) {
		if (modLoaded(PNEUMATICCRAFT)) {
			SysteamsPNCRCompat.unfoldPressurizedManifold(modBus);
		}
		if (modLoaded(CREATE)) {
			SysteamsCreateCompat.spinRotationalManifold(modBus);
		}
	}

	private static boolean modLoaded(String modid) {
		return ModList.get().isLoaded(modid);
	}
}
