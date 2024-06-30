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
		STEAM_RATIO_PNEUMATIC,
		STEAM_RATIO_PULVERIZER
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
		SPEED_PNEUMATIC,
		SPEED_PULVERIZER
		;

	public static final ForgeConfigSpec.ConfigValue<Boolean> PNEUMATIC_BOILER_IN_WORLD_CONVERSION;


	public static final ForgeConfigSpec.ConfigValue<Double> WATER_TO_STEAM_RATIO;
	public static final ForgeConfigSpec.ConfigValue<Double> STEAM_DYNAMO_MULTIPLIER;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Systeams Config", "---------------");

		builder.comment("The amount of steam 1mB of water makes.");
		WATER_TO_STEAM_RATIO = builder.defineInRange("water_to_steam_ratio", 2.0, 0.1, 10.0);
		builder.comment("The multiplier on the steam dynamo's RF/t");
		STEAM_DYNAMO_MULTIPLIER = builder.defineInRange("steam_dynamo_output_multiplier", 2, 0.05, 10);

		builder.push("Steam Boiler Ratios")
			.comment("The number of mB of steam produced per RF of energy usually produced by the same fuel in a dynamo",
					"Note that this does not affect the steam dynamo's rates. That needs to be adjusted with a datapack");

			STEAM_RATIO_STIRLING = boilerSteamRatio(builder, "stirling");
			STEAM_RATIO_MAGMATIC = boilerSteamRatio(builder, "magmatic");
			STEAM_RATIO_COMPRESSION = boilerSteamRatio(builder, "compression");
			STEAM_RATIO_NUMISMATIC = boilerSteamRatio(builder, "numismatic");
			STEAM_RATIO_LAPIDARY = boilerSteamRatio(builder, "lapidary");
			STEAM_RATIO_DISENCHANTMENT = boilerSteamRatio(builder, "disenchantment");
			STEAM_RATIO_GOURMAND = boilerSteamRatio(builder, "gourmand");
			STEAM_RATIO_PNEUMATIC = boilerSteamRatio(builder, "pneumatic");
		builder.pop().push("Steam Machine Ratios")
				.comment("The number of mB of steam required per RF of energy that usually consumed by the machine");
			STEAM_RATIO_PULVERIZER = machineSteamRatio(builder, "pulverizer");

		builder.pop().push("Boiler Speed Multipliers");
			builder.comment("The speed multiplier on each boiler's mB/t compared to the original RF/t");

			SPEED_STIRLING = boilerSpeed(builder, "stirling");
			SPEED_MAGMATIC = boilerSpeed(builder, "magmatic");
			SPEED_COMPRESSION = boilerSpeed(builder, "compression");
			SPEED_NUMISMATIC = boilerSpeed(builder, "numismatic");
			SPEED_LAPIDARY = boilerSpeed(builder, "lapidary");
			SPEED_DISENCHANTMENT = boilerSpeed(builder, "disenchantment");
			SPEED_GOURMAND = boilerSpeed(builder, "gourmand");
			SPEED_PNEUMATIC = boilerSpeed(builder, "pneumatic");
		builder.pop().push("Steam Machine Speed Multipliers")
			.comment("The speed multiplier on each steam machine's mB/t compared to the original RF/t");
			SPEED_PULVERIZER = machineSpeed(builder, "pulverizer");

		builder.push("Integration settings");
			PNEUMATIC_BOILER_IN_WORLD_CONVERSION = builder.comment(
					"If you can shift right click a Pneumatic Boiler with an Advanced Pneumatic Tube to convert it to a Pneumatic Dynamo",
					"This can get annoying when trying to place Advanced Pneumatic Tubes next to boilers")
				.define("pneumatic_boiler_conversion_to_dynamo", true);

		spec = builder.build();
	}


	private static final double steamRatioDefaultValue = 0.5;
	private static final double steamRatioMin = 0.05;
	private static final double steamRatioMax = 100;
	private static ForgeConfigSpec.ConfigValue<Double> boilerSteamRatio(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, steamRatioDefaultValue, steamRatioMin, steamRatioMax);
	}
	private static final double machineSteamRatioDefaultValue = 1.5;
	private static final double machineSteamRatioMin = 0.05;
	private static final double machineSteamRatioMax = 100;
	private static ForgeConfigSpec.ConfigValue<Double> machineSteamRatio(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, machineSteamRatioDefaultValue, machineSteamRatioMin, machineSteamRatioMax);
	}

	private static final double speedDefaultValue = 2;
	private static final double speedMin = 0.05;
	private static final double speedMax = 20;
	private static ForgeConfigSpec.ConfigValue<Double> boilerSpeed(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, speedDefaultValue, speedMin, speedMax);
	}
	private static final double machineSpeedDefaultValue = 1;
	private static final double machineSpeedMin = 0.05;
	private static final double machineSpeedMax = 20;
	private static ForgeConfigSpec.ConfigValue<Double> machineSpeed(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, machineSpeedDefaultValue, machineSpeedMin, machineSpeedMax);
	}

}
