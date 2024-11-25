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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BoilingRecipe extends ThermalRecipe {

    public BoilingRecipe(ResourceLocation recipeId, int energy, float _xp, List<Ingredient> _ii, List<FluidIngredient> inputFluids, List<ItemStack> _oi, List<Float> _oic, List<FluidStack> outputFluids) {
        super(recipeId, energy, 0, Collections.emptyList(), inputFluids, Collections.emptyList(), Collections.emptyList(), outputFluids);
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
