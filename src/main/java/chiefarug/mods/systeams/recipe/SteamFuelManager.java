package chiefarug.mods.systeams.recipe;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.core.util.helpers.FluidHelper;
import cofh.thermal.lib.util.managers.SingleFluidFuelManager;
import cofh.thermal.lib.util.recipes.internal.BaseDynamoFuel;
import cofh.thermal.lib.util.recipes.internal.IDynamoFuel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static cofh.lib.util.Constants.BUCKET_VOLUME;

public class SteamFuelManager extends SingleFluidFuelManager {

    public static final int MIN_ENERGY = 1000;
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
        var recipes = recipeManager.byType(SysteamsRegistry.Recipes.STEAM_TYPE.get());
        for (var entry : recipes.entrySet()) {
            addFuel(entry.getValue());
        }
    }
    // endregion

    @Override // grumble, grumble arbitary limits. This is all so the MIN_ENERGY value can be less than default. UGH.
    public IDynamoFuel addFuel(int energy, List<ItemStack> inputItems, List<FluidStack> inputFluids) {
        if (inputFluids.isEmpty() || energy <= 0) {
            return null;
        }
        if (energy < MIN_ENERGY || energy > MAX_ENERGY) {
            return null;
        }
        FluidStack input = inputFluids.get(0);
        if (input.isEmpty()) {
            return null;
        }
        int amount = input.getAmount();
        if (amount != FLUID_FUEL_AMOUNT) {
            if (amount != BUCKET_VOLUME) {
                long normEnergy = (long) energy * BUCKET_VOLUME / amount;
                input.setAmount(FLUID_FUEL_AMOUNT);
                energy = (int) normEnergy;
            }
            energy /= ENERGY_FACTOR;
        }
        energy = (int) (energy * getDefaultScale());

        BaseDynamoFuel fuel = new BaseDynamoFuel(energy, inputItems, inputFluids);
        fuelMap.put(FluidHelper.fluidHashcode(input), fuel);
        return fuel;
    }


}
