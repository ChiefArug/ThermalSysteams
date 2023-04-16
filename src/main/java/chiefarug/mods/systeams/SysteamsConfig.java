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
		STEAM_RATIO_GOURMAND,
		STEAM_RATIO_PNEUMATIC
		;
	// Boiler sped multipliers
	public static final ForgeConfigSpec.ConfigValue<Double>
		SPEED_STIRLING,
		SPEED_MAGMATIC,
		SPEED_COMPRESSION,
		SPEED_NUMISMATIC,
		SPEED_LAPIDARY,
		SPEED_DISENCHANTMENT,
		SPEED_GOURMAND,
		SPEED_PNEUMATIC
		;


	public static final ForgeConfigSpec.ConfigValue<Double> WATER_TO_STEAM_RATIO;
	public static final ForgeConfigSpec.ConfigValue<Double> STEAM_DYNAMO_MULTIPLIER;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Systeams Config", "---------------");

		builder.comment("The amount of steam 1mb of water makes.");
		WATER_TO_STEAM_RATIO = builder.defineInRange("water_to_steam_ratio", 2.0, 0.1, 10.0);
		builder.comment("The multiplier on the steam dynamo's RF/t");
		STEAM_DYNAMO_MULTIPLIER = builder.defineInRange("steam_dynamo_output_multiplier", 2, 0.05, 10);

		builder.push("Steam Values");
			builder.comment("The number of mb of steam produced per RF of energy usually produced by the same fuel in a dynamo", "Note that this does not affect the steam dynamo's rates. That needs to be adjusted with a datapack");

			STEAM_RATIO_STIRLING = steamRatio(builder, "stirling");
			STEAM_RATIO_MAGMATIC = steamRatio(builder, "magmatic");
			STEAM_RATIO_COMPRESSION = steamRatio(builder, "compression");
			STEAM_RATIO_NUMISMATIC = steamRatio(builder, "numismatic");
			STEAM_RATIO_LAPIDARY = steamRatio(builder, "lapidary");
			STEAM_RATIO_DISENCHANTMENT = steamRatio(builder, "disenchantment");
			STEAM_RATIO_GOURMAND = steamRatio(builder, "gourmand");
			STEAM_RATIO_PNEUMATIC = steamRatio(builder, "pneumatic");

		builder.pop();

		builder.push("Boiler Speed Multipliers");
			builder.comment("The speed multiplier on each boiler's mB/t");

			SPEED_STIRLING = speed(builder, "stirling");
			SPEED_MAGMATIC = speed(builder, "magmatic");
			SPEED_COMPRESSION = speed(builder, "compression");
			SPEED_NUMISMATIC = speed(builder, "numismatic");
			SPEED_LAPIDARY = speed(builder, "lapidary");
			SPEED_DISENCHANTMENT = speed(builder, "disenchantment");
			SPEED_GOURMAND = speed(builder, "gourmand");
			SPEED_PNEUMATIC = speed(builder, "pneumatic");

		spec = builder.build();
	}


	private static final double steamRatioDefaultValue = 0.5;
	private static final double steamRatioMin = 0.05;
	private static final double steamRatioMax = 100;
	private static ForgeConfigSpec.ConfigValue<Double> steamRatio(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, steamRatioDefaultValue, steamRatioMin, steamRatioMax);
	}

	private static final double speedDefaultValue = 2;
	private static final double speedMin = 0.05;
	private static final double speedMax = 20;
	private static ForgeConfigSpec.ConfigValue<Double> speed(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, speedDefaultValue, speedMin, speedMax);
	}

}
