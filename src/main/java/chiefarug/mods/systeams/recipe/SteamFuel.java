package chiefarug.mods.systeams.recipe;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.lib.fluid.FluidIngredient;
import cofh.thermal.lib.util.recipes.ThermalFuel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public class SteamFuel extends ThermalFuel {

	public SteamFuel(ResourceLocation recipeId, int energy, List<Ingredient> inputItems, List<FluidIngredient> inputFluids) {
		super(recipeId, energy, inputItems, inputFluids);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SysteamsRegistry.Recipes.Serializers.STEAM.get();
	}

	@Override
	public RecipeType<?> getType() {
		return null;
	}
}
