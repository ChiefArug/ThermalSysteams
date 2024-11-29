package chiefarug.mods.systeams.compat.jei;

import chiefarug.mods.systeams.recipe.BoilingRecipe;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermal.core.compat.jei.TCoreJeiPlugin;
import cofh.thermal.expansion.client.gui.dynamo.DynamoCompressionScreen;
import cofh.thermal.lib.compat.jei.Drawables;
import cofh.thermal.lib.compat.jei.ThermalRecipeCategory;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BoilingCategory extends ThermalRecipeCategory<BoilingRecipe> {

    private final IDrawableStatic tankOverlay;
    private final IDrawableStatic tankBackground;
    private final IDrawableAnimated duration;
    private final IDrawableStatic durationBackground;

    public BoilingCategory(IGuiHelper guiHelper, ItemStack icon, RecipeType<BoilingRecipe> type) {
        super(guiHelper, icon, type);

        background = guiHelper.drawableBuilder(DynamoCompressionScreen.TEXTURE, 26, 11, 70, 62)
                .addPadding(0, 0, 16, 78)
                .build();
        name = StringHelper.getTextComponent("info.systeams.boiling_recipe");


        tankBackground = Drawables.getDrawables(guiHelper).getTank(Drawables.TANK_MEDIUM);
        tankOverlay = Drawables.getDrawables(guiHelper).getTankOverlay(Drawables.TANK_MEDIUM);
        durationBackground = Drawables.getDrawables(guiHelper).getScale(Drawables.SCALE_FLAME);
        duration = guiHelper.createAnimatedDrawable(Drawables.getDrawables(guiHelper).getScaleFill(Drawables.SCALE_FLAME), 400, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public void draw(BoilingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        tankBackground.draw(guiGraphics, 33, 10);
        tankBackground.draw(guiGraphics, 106, 10);
        durationBackground.draw(guiGraphics, 70, 24);
        duration.draw(guiGraphics, 70, 24);
    }

    @NotNull
    @Override
    public RecipeType<BoilingRecipe> getRecipeType() {
        return SysteamsJEIPlugin.BOILING_RECIPE_TYPE;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BoilingRecipe recipe, IFocusGroup focuses) {
        FluidStack output = recipe.getOutputFluids().get(0).copy();
        FluidStack[] input = recipe.getInputFluids().get(0).getFluids();
        double ratio = (double) input[0].getAmount() / output.getAmount();

        int inAmount;
        int outAmount;
        if (ratio < 1) {
            outAmount = 1000;
            inAmount = (int) Math.round(outAmount * ratio);
        } else {
            inAmount = 1000;
            outAmount = (int) Math.round(inAmount / ratio);
        }

        output.setAmount(outAmount);
        List<FluidStack> inputs = Arrays.stream(input)
                .map(FluidStack::copy)
                .peek(stack -> stack.setAmount(inAmount))
                .toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 34, 11)
                .addIngredients(ForgeTypes.FLUID_STACK, inputs)
                .setFluidRenderer(TCoreJeiPlugin.tankSize(Drawables.TANK_LARGE), false, 16, 40)
                .setOverlay(tankOverlay, 0,0)
                .addTooltipCallback(TCoreJeiPlugin.defaultFluidTooltip());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 11)
                .addIngredients(ForgeTypes.FLUID_STACK, List.of(output))
                .setFluidRenderer(TCoreJeiPlugin.tankSize(Drawables.TANK_LARGE), false, 16, 40)
                .setOverlay(tankOverlay, 0, 0)
                .addTooltipCallback(TCoreJeiPlugin.defaultFluidTooltip());
    }
}
