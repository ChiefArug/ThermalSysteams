package chiefarug.mods.systeams.jei;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.lib.fluid.FluidIngredient;
import cofh.lib.util.Constants;

import java.util.List;

public class JEISteamRecipe {
	List<FluidIngredient> steamIngredient = List.of(FluidIngredient.of(SysteamsRegistry.Fluids.Steam.TAG, Constants.BUCKET_VOLUME));

	public List<FluidIngredient> getFluid() {
		return steamIngredient;
	}
}
