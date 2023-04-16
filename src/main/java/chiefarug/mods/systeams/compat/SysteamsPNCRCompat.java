package chiefarug.mods.systeams.compat;

import chiefarug.mods.systeams.Boiler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class SysteamsPNCRCompat {

	public static final String PNEUMATIC_BOILER_ID = "pneumatic_boiler";

	public static void unfoldPressurizedManifold() {
		Registry.init();
	}

	class Registry {
		static void init () {}
		public static final Boiler<PneumaticBoilerBlockEntity, PneumaticBoilerContainer> PNEUMATIC = new Boiler<>(PNEUMATIC_BOILER_ID, PneumaticBoilerBlockEntity.class, PneumaticBoilerBlockEntity::new, PneumaticBoilerContainer::new);

		class Client {
			static void registerMenuFactory() {
				MenuScreens.register(PNEUMATIC.menu(), PneumaticBoilerScreen::new);
			}
		}
	}


	public static final Capability<IAirHandlerMachine> AIR_HANDLER = CapabilityManager.get(new CapabilityToken<>(){}); //this is magic
}
