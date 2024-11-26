package chiefarug.mods.systeams.recipe;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.lib.common.fluid.FluidIngredient;
import cofh.thermal.lib.util.recipes.ThermalRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

import static chiefarug.mods.systeams.Systeams.LGGR;
import static chiefarug.mods.systeams.SysteamsRegistry.Recipes.MARKER_ENERGY;

public class BoilingRecipe extends ThermalRecipe {

    public BoilingRecipe(ResourceLocation recipeId, int energy, float _xp, List<Ingredient> _ii, List<FluidIngredient> inputFluids, List<ItemStack> _oi, List<Float> _oic, List<FluidStack> outputFluids) {
        super(recipeId, 0, 0, Collections.emptyList(), inputFluids, Collections.emptyList(), Collections.emptyList(), outputFluids);
        if (!_ii.isEmpty())
            LGGR.warn("Item inputs ignored for boiling recipe {}", recipeId);
        if (!_oi.isEmpty())
            LGGR.warn("Item outputs ignored for boiling recipe {}", recipeId);
        if (_xp != 0)
            LGGR.warn("XP amount ignored for boiling recipe {}", recipeId);
        if (energy != MARKER_ENERGY)
            LGGR.warn("Energy amount ignored for boiling recipe {}", recipeId);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SysteamsRegistry.Recipes.BOILING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SysteamsRegistry.Recipes.BOILING_TYPE.get();
    }
}
