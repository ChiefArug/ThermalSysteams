package chiefarug.mods.systeams;

import net.minecraftforge.common.ForgeConfigSpec;

public class SysteamsCommonConfig {
    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder()
            .comment("If you are looking for the config options to change power ratios and water",
            "consumption rates, those are located inside the serverconfig folder inside each world's folder!");
    public static final ForgeConfigSpec.BooleanValue enableEarlyGameRecipeChanges = builder
            .comment("If some early game recipe changes to force players to progress through systeams",
                    "before the rest of thermal should be applied. These changes include more expensive",
                    "recipes for X, Y and removing all Z recipes.")
            .define("enable_early_game_recipe_changes", true);
    static final ForgeConfigSpec spec = builder.build();
}
