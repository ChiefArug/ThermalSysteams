package chiefarug.mods.systeams.compat.jei;

import chiefarug.mods.systeams.Systeams;
import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.client.screens.CompressionBoilerScreen;
import chiefarug.mods.systeams.client.screens.DisenchantmentBoilerScreen;
import chiefarug.mods.systeams.client.screens.GourmandBoilerScreen;
import chiefarug.mods.systeams.client.screens.LapidaryBoilerScreen;
import chiefarug.mods.systeams.client.screens.MagmaticBoilerScreen;
import chiefarug.mods.systeams.client.screens.NumismaticBoilerScreen;
import chiefarug.mods.systeams.client.screens.SteamDynamoScreen;
import chiefarug.mods.systeams.client.screens.StirlingBoilerScreen;
import chiefarug.mods.systeams.compat.thermal_extra.SysteamsThermalExtraCompat;
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
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.systeams.Systeams.EXTRA;
import static chiefarug.mods.systeams.Systeams.MODID;

@JeiPlugin
public class SysteamsJEIPlugin implements IModPlugin {

	public static final RecipeType<SteamFuel> STEAM_DYNAMO_RECIPE_TYPE = RecipeType.create(MODID, SysteamsRegistry.STEAM_DYNAMO_ID, SteamFuel.class);

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new SteamFuelCategory(registration.getJeiHelpers().getGuiHelper(), new ItemStack(SysteamsRegistry.Items.STEAM_DYNAMO.get()), STEAM_DYNAMO_RECIPE_TYPE));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		RecipeManager recipeManager = getRecipeManager();

		registration.addRecipes(STEAM_DYNAMO_RECIPE_TYPE, recipeManager.getAllRecipesFor(SysteamsRegistry.Recipes.STEAM_TYPE.get()));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		final int flameX = 80;// + 12; // base pos + difference cause one uses center the other corner
		final int flameY = 35;// + 12;
		registration.addRecipeClickArea(SteamDynamoScreen.class, SteamFuelCategory.DURATION_X + 12, SteamFuelCategory.DURATION_Y + 12, 16, 16, STEAM_DYNAMO_RECIPE_TYPE);
		registration.addRecipeClickArea(StirlingBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.STIRLING_FUEL_TYPE);
		registration.addRecipeClickArea(MagmaticBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.MAGMATIC_FUEL_TYPE);
		registration.addRecipeClickArea(CompressionBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.COMPRESSION_FUEL_TYPE);
		registration.addRecipeClickArea(NumismaticBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.NUMISMATIC_FUEL_TYPE);
		registration.addRecipeClickArea(LapidaryBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.LAPIDARY_FUEL_TYPE);
		registration.addRecipeClickArea(DisenchantmentBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.DISENCHANTMENT_FUEL_TYPE);
		registration.addRecipeClickArea(GourmandBoilerScreen.class, flameX, flameY, 16, 16, TExpJeiPlugin.GOURMAND_FUEL_TYPE);

		if (ModList.get().isLoaded(EXTRA))
			SysteamsThermalExtraCompat.JEIHandler.registerGuiHandlers(registration, flameX, flameY);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Items.STEAM_DYNAMO.get()), STEAM_DYNAMO_RECIPE_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.STIRLING.item()), TExpJeiPlugin.STIRLING_FUEL_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.MAGMATIC.item()), TExpJeiPlugin.MAGMATIC_FUEL_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.COMPRESSION.item()), TExpJeiPlugin.COMPRESSION_FUEL_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.NUMISMATIC.item()), TExpJeiPlugin.NUMISMATIC_FUEL_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.LAPIDARY.item()), TExpJeiPlugin.LAPIDARY_FUEL_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.DISENCHANTMENT.item()), TExpJeiPlugin.DISENCHANTMENT_FUEL_TYPE);
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Boilers.GOURMAND.item()), TExpJeiPlugin.GOURMAND_FUEL_TYPE);

		if (ModList.get().isLoaded(EXTRA))
			SysteamsThermalExtraCompat.JEIHandler.registerRecipeCatalysts(registration);
	}

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return Systeams.MODRL;
	}

	private static RecipeManager getRecipeManager() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			throw new IllegalStateException("Tried to get recipe manager without a world to get it from!");
		}
		return level.getRecipeManager();
	}
}
