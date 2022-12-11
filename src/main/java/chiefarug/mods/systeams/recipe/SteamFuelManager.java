package chiefarug.mods.systeams.recipe;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.thermal.lib.util.managers.SingleFluidFuelManager;
import cofh.thermal.lib.util.recipes.internal.IDynamoFuel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

public class SteamFuelManager extends SingleFluidFuelManager {
    private static final SteamFuelManager INSTANCE = new SteamFuelManager();
    protected static final int DEFAULT_ENERGY = 100000;

    public static SteamFuelManager instance() {
        return INSTANCE;
    }

    private SteamFuelManager() {

        super(DEFAULT_ENERGY);
    }

    public int getEnergy(FluidStack stack) {

        IDynamoFuel fuel = getFuel(stack);
        return fuel != null ? fuel.getEnergy() : 0;
    }

    // region IManager
    @Override
    public void refresh(RecipeManager recipeManager) {

        clear();
        var recipes = recipeManager.byType(SysteamsRegistry.Recipes.Types.STEAM.get());
        for (var entry : recipes.entrySet()) {
            addFuel(entry.getValue());
        }
    }
    // endregion
}
