package chiefarug.mods.systeams.jei;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.client.screens.CompressionBoilerScreen;
import chiefarug.mods.systeams.client.screens.DisenchantmentBoilerScreen;
import chiefarug.mods.systeams.client.screens.GourmandBoilerScreen;
import chiefarug.mods.systeams.client.screens.LapidaryBoilerScreen;
import chiefarug.mods.systeams.client.screens.MagmaticBoilerScreen;
import chiefarug.mods.systeams.client.screens.NumismaticBoilerScreen;
import chiefarug.mods.systeams.client.screens.SteamDynamoScreen;
import chiefarug.mods.systeams.client.screens.StirlingBoilerScreen;
import chiefarug.mods.systeams.recipe.SteamFuel;
import cofh.thermal.expansion.compat.jei.TExpJeiPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.systeams.Systeams.MODID;

@JeiPlugin
public class SysteamsJEIPlugin implements IModPlugin {

	public static final RecipeType<SteamFuel> STEAM_DYNAMO_RECIPE_TYPE = RecipeType.create(MODID, SysteamsRegistry.STEAM_DYNAMO_ID, SteamFuel.class);
	private static final ResourceLocation UID = new ResourceLocation(MODID, "systeams");

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new SteamFuelCategory(registration.getJeiHelpers().getGuiHelper(), new ItemStack(SysteamsRegistry.Items.STEAM_DYNAMO.get()), STEAM_DYNAMO_RECIPE_TYPE));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		RecipeManager recipeManager = getRecipeManager();

		registration.addRecipes(STEAM_DYNAMO_RECIPE_TYPE, recipeManager.getAllRecipesFor(SysteamsRegistry.Recipes.STEAM_TYPE));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		final int flameX = 80;// + 12; // base pos + difference cause one uses center the other corner
		final int flameY = 35;// + 12;
		registration.addRecipeClickArea(SteamDynamoScreen.class, SteamFuelCategory.DURATION_X + 12, SteamFuelCategory.DURATION_Y + 12, 16, 16, STEAM_DYNAMO_RECIPE_TYPE);
		registration.addRecipeClickArea(StirlingBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.STIRLING_FUEL);
		registration.addRecipeClickArea(MagmaticBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.MAGMATIC_FUEL);
		registration.addRecipeClickArea(CompressionBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.COMPRESSION_FUEL);
		registration.addRecipeClickArea(NumismaticBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.NUMISMATIC_FUEL);
		registration.addRecipeClickArea(LapidaryBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.LAPIDARY_FUEL);
		registration.addRecipeClickArea(DisenchantmentBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.DISENCHANTMENT_FUEL);
		registration.addRecipeClickArea(GourmandBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.GOURMAND_FUEL);

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Items.STEAM_DYNAMO.get()), STEAM_DYNAMO_RECIPE_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.STIRLING.item()), TExpJeiPlugin.STIRLING_FUEL);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.MAGMATIC.item()), TExpJeiPlugin.MAGMATIC_FUEL);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.COMPRESSION.item()), TExpJeiPlugin.COMPRESSION_FUEL);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.NUMISMATIC.item()), TExpJeiPlugin.NUMISMATIC_FUEL);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.LAPIDARY.item()), TExpJeiPlugin.LAPIDARY_FUEL);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.DISENCHANTMENT.item()), TExpJeiPlugin.DISENCHANTMENT_FUEL);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.GOURMAND.item()), TExpJeiPlugin.GOURMAND_FUEL);

	}

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return UID;
	}

	private static RecipeManager getRecipeManager() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			throw new IllegalStateException("Tried to get recipe manager without a world to get it from!");
		}
		return level.getRecipeManager();
	}
}
