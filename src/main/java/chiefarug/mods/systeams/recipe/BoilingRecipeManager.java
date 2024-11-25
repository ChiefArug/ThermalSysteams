package chiefarug.mods.systeams.recipe;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.core.util.helpers.FluidHelper;
import cofh.thermal.lib.util.managers.AbstractManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Map;

//TODO: JEI category for this
public class BoilingRecipeManager extends AbstractManager {

    private static int DEFAULT_ENERGY = 1000;

    public BoilingRecipeManager() {
        super(DEFAULT_ENERGY);
    }


    private static BoilingRecipeManager INSTANCE = new BoilingRecipeManager();

    public static BoilingRecipeManager getInstance() {
        return INSTANCE;
    }

    record HashableFluid(FluidStack fluid) {
        @Override
        public int hashCode() {
            return FluidHelper.fluidHashcode(fluid);
        }
    }
    public record BoiledFluid(int fluidInAmount, FluidStack fluidOut) {}

    private Map<HashableFluid, BoiledFluid> recipes = new Object2ObjectOpenHashMap<>();

    public void clear() {
        recipes.clear();
    }

    public boolean canBoil(FluidStack in) {
        return boil(in) != null;
    }

    @Nullable
    public BoiledFluid boil(FluidStack in) {
        return recipes.get(new HashableFluid(in));
    }

    public void addRecipe(BoilingRecipe recipe) {
        for (FluidStack stack : recipe.getInputFluids().get(0).getFluids()) {
            recipes.put(new HashableFluid(stack), new BoiledFluid(stack.getAmount(), recipe.getOutputFluids().get(0)));
        }
    }

    @Override
    public void refresh(RecipeManager recipeManager) {

        Map<ResourceLocation, BoilingRecipe> recipes = recipeManager.byType(SysteamsRegistry.Recipes.BOILING_TYPE.get());
        this.recipes = new Object2ObjectOpenHashMap<>(recipes.size());
        for (var entry : recipes.entrySet()) {
            addRecipe(entry.getValue());
        }
    }
}
