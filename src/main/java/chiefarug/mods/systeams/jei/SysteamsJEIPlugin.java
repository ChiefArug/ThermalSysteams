package chiefarug.mods.systeams.jei;

import chiefarug.mods.systeams.SysteamsRegistry;
import chiefarug.mods.systeams.client.screens.SteamDynamoScreen;
import chiefarug.mods.systeams.recipe.SteamFuel;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
//		registration.addRecipes(STEAM_DYNAMO_RECIPE_TYPE, List.of(new Ste()));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(SteamDynamoScreen.class, SteamFuelCategory.DURATION_X + 12, SteamFuelCategory.DURATION_Y + 12, 16, 16, STEAM_DYNAMO_RECIPE_TYPE);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(SysteamsRegistry.Items.STEAM_DYNAMO.get()), STEAM_DYNAMO_RECIPE_TYPE);
	}

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return UID;
	}
}
