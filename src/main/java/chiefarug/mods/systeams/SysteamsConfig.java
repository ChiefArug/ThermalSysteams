package chiefarug.mods.systeams;

import net.minecraftforge.common.ForgeConfigSpec;

public class SysteamsConfig {

	// Steam ratios
	public static final ForgeConfigSpec.ConfigValue<Double>
		STEAM_RATIO_STERLING,
		STEAM_RATIO_MAGMATIC,
		STEAM_RATIO_COMPRESSION
		;

	public static final ForgeConfigSpec.ConfigValue<Double> WATER_TO_STEAM_RATIO;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Systeams Config", "---------------");

		builder.comment("The amount of steam 1mb of water makes");
		WATER_TO_STEAM_RATIO = builder.defineInRange("water_to_steam_ratio", 10, 0.001, 1000);

		builder.push("Steam Values");
			builder.comment("The number of mb of steam produced per RF of energy usually produced by the same fuel in a dynamo");

			STEAM_RATIO_STERLING = steamRatio(builder, "sterling");
			STEAM_RATIO_MAGMATIC = steamRatio(builder, "magmatic");
			STEAM_RATIO_COMPRESSION = steamRatio(builder, "compression");
		builder.pop();


		spec = builder.build();
	}


	private static final double defaultValue = 0.1;
	private static final double min = 0.001;
	private static final double max = 100;
	private static ForgeConfigSpec.ConfigValue<Double> steamRatio(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, defaultValue, min, max);
	}

}
