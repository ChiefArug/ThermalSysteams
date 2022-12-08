package chiefarug.mods.systeams.jei;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.recipe.SteamFuel;
import cofh.lib.fluid.FluidIngredient;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermal.core.compat.jei.TCoreJeiPlugin;
import cofh.thermal.expansion.client.gui.dynamo.DynamoStirlingScreen;
import cofh.thermal.lib.compat.jei.Drawables;
import cofh.thermal.lib.compat.jei.ThermalFuelCategory;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static chiefarug.mods.systeams.jei.SysteamsJEIPlugin.STEAM_DYNAMO_RECIPE_TYPE;

public class SteamFuelCategory extends ThermalFuelCategory<SteamFuel> {

	public static final int DURATION_X = 70;
    public static final int DURATION_Y = 24;

    private static final int ENERGY_X = 106;
    private static final int ENERGY_Y = 10;

//	private final IDrawable icon;
//	private final IDrawable background;
//	private final Component name;
//
    private final IDrawableStatic tankOverlay;

	public SteamFuelCategory(IGuiHelper guiHelper, ItemStack icon, RecipeType<SteamFuel> type) {
		super(guiHelper, icon, type);
		background = guiHelper.drawableBuilder(DynamoStirlingScreen.TEXTURE, 26, 11, 70, 62)
                .addPadding(0, 0, 16, 78)
                .build();
		name = StringHelper.getTextComponent(SysteamsRegistry.Blocks.STEAM_DYNAMO.get().getDescriptionId());

//		tankBackground = Drawables.getDrawables(guiHelper).getTank(Drawables.TANK_MEDIUM);
        tankOverlay = Drawables.getDrawables(guiHelper).getTankOverlay(Drawables.TANK_MEDIUM);
		durationBackground = Drawables.getDrawables(guiHelper).getScale(Drawables.SCALE_FLAME);
		energyBackground = Drawables.getDrawables(guiHelper).getEnergyEmpty();
		energy = guiHelper.createAnimatedDrawable(Drawables.getDrawables(guiHelper).getEnergyFill(), 400, IDrawableAnimated.StartDirection.BOTTOM, false);
		duration = guiHelper.createAnimatedDrawable(Drawables.getDrawables(guiHelper).getScaleFill(Drawables.SCALE_FLAME), 400, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public @NotNull RecipeType<SteamFuel> getRecipeType() {
		return STEAM_DYNAMO_RECIPE_TYPE;
	}

	@Override
	public @NotNull Component getTitle() {
		return name;
	}

	@Override
	public @NotNull IDrawable getBackground() {
		return background;
	}

	@Override
	public @NotNull IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SteamFuel recipe, @NotNull IFocusGroup focuses) {
		List<FluidIngredient> inputs = recipe.getInputFluids();

        builder.addSlot(RecipeIngredientRole.INPUT, 34, 11)
                .addIngredients(ForgeTypes.FLUID_STACK, List.of(inputs.get(0).getFluids()))
                .setFluidRenderer(TCoreJeiPlugin.tankSize(Drawables.TANK_MEDIUM), false, 16, 40)
                .setOverlay(tankOverlay, 0, 0)
                .addTooltipCallback(TCoreJeiPlugin.defaultFluidTooltip());
	}
}