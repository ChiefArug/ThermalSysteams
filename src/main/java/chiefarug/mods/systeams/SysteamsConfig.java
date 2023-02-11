package chiefarug.mods.systeams;

import net.minecraftforge.common.ForgeConfigSpec;

public class SysteamsConfig {

	// Steam ratios
	public static final ForgeConfigSpec.ConfigValue<Double>
		STEAM_RATIO_STIRLING,
		STEAM_RATIO_MAGMATIC,
		STEAM_RATIO_COMPRESSION,
		STEAM_RATIO_NUMISMATIC,
		STEAM_RATIO_LAPIDARY,
		STEAM_RATIO_DISENCHANTMENT,
		STEAM_RATIO_GOURMAND
		;

	public static final ForgeConfigSpec.ConfigValue<Double> WATER_TO_STEAM_RATIO;
	public static final ForgeConfigSpec.ConfigValue<Double> STEAM_DYNAMO_MULTIPLIER;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Systeams Config", "---------------");

		builder.comment("The amount of steam 1mb of water makes.");
		WATER_TO_STEAM_RATIO = builder.defineInRange("water_to_steam_ratio", 2.0, 0.1, 10.0);
		builder.comment("The multiplier on the steam dynamo's output rate");
		STEAM_DYNAMO_MULTIPLIER = builder.defineInRange("steam_dynamo_output_multiplier", 2, 0.1, 10);

		builder.push("Steam Values");
			builder.comment("The number of mb of steam produced per RF of energy usually produced by the same fuel in a dynamo");

			STEAM_RATIO_STIRLING = steamRatio(builder, "stirling");
			STEAM_RATIO_MAGMATIC = steamRatio(builder, "magmatic");
			STEAM_RATIO_COMPRESSION = steamRatio(builder, "compression");
			STEAM_RATIO_NUMISMATIC = steamRatio(builder, "numismatic");
			STEAM_RATIO_LAPIDARY = steamRatio(builder, "lapidary");
			STEAM_RATIO_DISENCHANTMENT = steamRatio(builder, "disenchantment");
			STEAM_RATIO_GOURMAND = steamRatio(builder, "gourmand");

		builder.pop();

		spec = builder.build();
	}


	private static final double defaultValue = 0.5;
	private static final double min = 0.1;
	private static final double max = 100;
	private static ForgeConfigSpec.ConfigValue<Double> steamRatio(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, defaultValue, min, max);
	}

}
