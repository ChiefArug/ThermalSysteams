package chiefarug.mods.systeams;

import net.minecraftforge.common.ForgeConfigSpec;

public class SysteamsConfig {

	static final ForgeConfigSpec spec;
	public static final ForgeConfigSpec.ConfigValue<Integer> ENERGY;
	public static final ForgeConfigSpec.ConfigValue<Integer> AMOUNT;

	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Systeams Config");
		builder.push("Steam Dynamo");
			builder.comment("The amount of energy generated per amount of steam consumed");
			ENERGY = builder.defineInRange("energy", 100, 1, 100000);
			builder.comment("The amount of steam consumed to generate the above amount of energy");
			AMOUNT = builder.defineInRange("amount", 50, 1, 1000);
		builder.pop();

		spec = builder.build();
	}
}
