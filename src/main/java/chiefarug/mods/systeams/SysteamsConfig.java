package chiefarug.mods.systeams;

import net.minecraftforge.common.ForgeConfigSpec;

public class SysteamsConfig {

	// Steam ratios
	public static final ForgeConfigSpec.ConfigValue<Double>
		STEAM_RATIO_STERLING,
		STEAM_RATIO_MAGMATIC
		;

	public static final ForgeConfigSpec.ConfigValue<Double> WATER_TO_STEAM_RATIO;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Systeams Config", "---------------");

		builder.comment("The amount of steam 1mb of water makes");
		WATER_TO_STEAM_RATIO = builder.defineInRange("water_to_steam_ratio", 100, 0.001, 1000);

		builder.push("Steam Values");
			builder.comment("The number of mb of steam produced per RF of energy usually produced by the same fuel in a dynamo");
			final double defaultValue = 2;
			final double min = 0.0001;
			final double max = 100;
			STEAM_RATIO_STERLING = builder.defineInRange("sterling", defaultValue, min, max);
			STEAM_RATIO_MAGMATIC = builder.defineInRange("magmatic", defaultValue, min, max);
		builder.pop();


		spec = builder.build();
	}

}
