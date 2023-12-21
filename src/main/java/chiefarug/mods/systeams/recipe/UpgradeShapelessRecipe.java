package chiefarug.mods.systeams.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.systeams.Systeams.LGGR;
import static chiefarug.mods.systeams.Systeams.mergeTags;
import static chiefarug.mods.systeams.SysteamsRegistry.Items.UPGRADE_MAIN;

public class UpgradeShapelessRecipe extends ShapelessRecipe {

	private final ItemStack replacement;

	public UpgradeShapelessRecipe(ShapelessRecipe original, ItemStack replacement) {
		this(original.getId(), original.getGroup(), original.getResultItem(null/*techincally we should do this, but we know that its unused*/), original.getIngredients(), replacement);
	}

	public UpgradeShapelessRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients, ItemStack replacement) {
		super(id, group, CraftingBookCategory.MISC, result, ingredients);
		this.replacement = replacement;
	}

	@NotNull
	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess reg) {
		ItemStack result = super.assemble(inv, reg);

		int index = getMainItem(inv);
		if (index == -1) {
			LGGR.warn("Just crafted a Systeams upgrade recipe but couldn't find a main item! Did tags fail to load, or has someone changed the recipe but not the tags? Check the status of the #systeams:recipe_logic/upgrade_main item tag!");
			return result;
		}
		ItemStack input = inv.getItem(index);

		result.setTag(mergeTags(result.getTag(), input.getTag()));
		return result;

	}

	@NotNull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> original = super.getRemainingItems(container);

		int index = getMainItem(container);
		if (!replacement.isEmpty() && index > -1)
			original.set(index, replacement.copy());

		return original;
	}

	private int getMainItem(CraftingContainer container) {
		int i;
		boolean matchFound = false;

		for (i = 0; i < container.getContainerSize(); i++) {
			ItemStack item = container.getItem(i);
			if (item.is(UPGRADE_MAIN.getKey())) {
				matchFound = true;
				break;
			}
		}
		if (!matchFound) {
			return -1;
		}
		return i;
	}

	// this is mainly so we can use the super methods
	public static class Serializer extends ShapelessRecipe.Serializer {

		@NotNull
		@Override
		public UpgradeShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			ShapelessRecipe original = super.fromJson(recipeId, json);

			ItemStack replacement = ItemStack.EMPTY;

			if (json.has("replacement")) {
				replacement = itemFromJson(json.get("replacement"));
			}

			return new UpgradeShapelessRecipe(original, replacement);
		}

		public UpgradeShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			ShapelessRecipe original = super.fromNetwork(recipeId, buffer);
			if (original == null) return null;

			ItemStack replacement = buffer.readItem();

			return new UpgradeShapelessRecipe(original, replacement);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, ShapelessRecipe recipe) {
			// this cast should be safe... right?
			toNetwork(buffer, (UpgradeShapelessRecipe) recipe);
		}

		public void toNetwork(FriendlyByteBuf buffer, UpgradeShapelessRecipe recipe) {
			super.toNetwork(buffer, recipe);

			buffer.writeItemStack(recipe.replacement, true);
		}

		private ItemStack itemFromJson(JsonElement json) {
			if (json instanceof JsonObject o) {
				ItemStack item = itemFromString(o.get("item").getAsString());
				if (o.has("nbt")) {
					try {
						item.setTag(TagParser.parseTag(o.get("nbt").getAsString()));
					} catch (CommandSyntaxException e) {
						throw new JsonSyntaxException("Error while parsing NBT for item '" + item.getItem() + "'",e);
					}
				}
				return item;
			}
			return itemFromString(json.getAsString());
		}

		private ItemStack itemFromString(String name) {
			if (name.isEmpty()) return ItemStack.EMPTY;

			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
			if (item == null) throw new JsonSyntaxException("Unknown item '" + name + "'");

			return item.getDefaultInstance();
		}
	}
}
