package chiefarug.mods.systeams;

import chiefarug.mods.systeams.recipe.SteamFuelManager;
import net.minecraftforge.common.ForgeConfigSpec;

public class SysteamsConfig {

	// Steam ratios
	public static final ForgeConfigSpec.DoubleValue
		STEAM_RATIO_STIRLING,
		STEAM_RATIO_MAGMATIC,
		STEAM_RATIO_COMPRESSION,
		STEAM_RATIO_NUMISMATIC,
		STEAM_RATIO_LAPIDARY,
		STEAM_RATIO_DISENCHANTMENT,
		STEAM_RATIO_GOURMAND,
		STEAM_RATIO_PNEUMATIC,
		STEAM_RATIO_FROST
		;
	// Boiler sped multipliers
	public static final ForgeConfigSpec.DoubleValue
		SPEED_STIRLING,
		SPEED_MAGMATIC,
		SPEED_COMPRESSION,
		SPEED_NUMISMATIC,
		SPEED_LAPIDARY,
		SPEED_DISENCHANTMENT,
		SPEED_GOURMAND,
		SPEED_PNEUMATIC,
		SPEED_FROST
		;

	public static final ForgeConfigSpec.BooleanValue PNEUMATIC_BOILER_IN_WORLD_CONVERSION;

	public static final ForgeConfigSpec.IntValue STEAM_DYNAMO_POWER;

	public static final ForgeConfigSpec.BooleanValue REPLACE_TOOLTIPS;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Systeams Config", "---------------");

		builder.comment("The multiplier on the steam dynamo's RF/t");
		STEAM_DYNAMO_POWER = builder.defineInRange("steam_dynamo_base_power", SteamFuelManager.instance().getBasePower(), SteamFuelManager.instance().getMinPower(), SteamFuelManager.instance().getMaxPower());

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
			STEAM_RATIO_FROST = steamRatio(builder, "frost");

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
			SPEED_FROST = speed(builder, "frost");

		builder.pop();

		builder.push("Integration settings");
			PNEUMATIC_BOILER_IN_WORLD_CONVERSION = builder.comment(
					"If you can shift right click a Pneumatic Boiler with an Advanced Pneumatic Tube to convert it to a Pneumatic Dynamo",
					"This can get annoying when trying to place Advanced Pneumatic Tubes next to boilers")
				.define("pneumatic_boiler_conversion_to_dynamo", true);

		builder.pop();
			REPLACE_TOOLTIPS = builder.comment(
					"If the Dynamo in dynamo augment tooltips should be replaced with Dynamo & Boiler",
					"This doesn't have as much support for translations (it can still be translated with the key info.systeams.augment.type.DynamoBoiler)")
				.define("replace_dynamo_augment_tooltips", true);

		spec = builder.build();
	}


	private static final double steamRatioDefaultValue = 0.5;
	private static final double steamRatioMin = 0.05;
	private static final double steamRatioMax = 100;
	private static ForgeConfigSpec.DoubleValue steamRatio(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, steamRatioDefaultValue, steamRatioMin, steamRatioMax);
	}

	private static final double speedDefaultValue = 2;
	private static final double speedMin = 0.05;
	private static final double speedMax = 20;
	private static ForgeConfigSpec.DoubleValue speed(ForgeConfigSpec.Builder builder, String name) {
		return builder.defineInRange(name, speedDefaultValue, speedMin, speedMax);
	}

}
