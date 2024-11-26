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

    private static final int DEFAULT_ENERGY = 1000;

    public BoilingRecipeManager() {
        super(DEFAULT_ENERGY);
    }


    private static final BoilingRecipeManager INSTANCE = new BoilingRecipeManager();

    public static BoilingRecipeManager instance() {
        return INSTANCE;
    }

    record HashFluid(FluidStack fluid) {
        @Override
        public int hashCode() {
            return FluidHelper.fluidHashcode(fluid);
        }
    }
    public record BoiledFluid(double inToOutRatio, FluidStack fluidOut) {
        public BoiledFluid(int inAmount, FluidStack fluidOut) {
            this((double) inAmount / fluidOut.getAmount(), fluidOut);
        }

        public int getInPerTick(int outPerTick) {
            return (int) Math.floor(inToOutRatio * outPerTick);
        }
    }

    private Map<HashFluid, BoiledFluid> recipes = new Object2ObjectOpenHashMap<>();

    public boolean canBoil(FluidStack in) {
        return boil(in) != null;
    }

    @Nullable
    public BoiledFluid boil(FluidStack in) {
        return recipes.get(new HashFluid(in));
    }

    public void addRecipe(BoilingRecipe recipe) {
        FluidStack out = recipe.getOutputFluids().get(0);
        for (FluidStack in : recipe.getInputFluids().get(0).getFluids()) {
            recipes.put(new HashFluid(in), new BoiledFluid(in.getAmount(), out));
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
