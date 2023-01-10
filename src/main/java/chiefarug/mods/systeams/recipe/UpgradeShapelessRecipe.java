package chiefarug.mods.systeams.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.systeams.Systeams.mergeTags;
import static chiefarug.mods.systeams.SysteamsRegistry.Items.UPGRADE_COPY_NBT;

public class UpgradeShapelessRecipe extends ShapelessRecipe {

	private final NonNullList<ItemStack> replacements;

	public UpgradeShapelessRecipe(ShapelessRecipe original, NonNullList<ItemStack> replacements) {
		this(original.getId(), original.getGroup(), original.getResultItem(), original.getIngredients(), replacements);
	}

	public UpgradeShapelessRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients, NonNullList<ItemStack> replacements) {
		super(id, group, result, ingredients);
		this.replacements = replacements;
	}

	@NotNull
	@Override
	public ItemStack assemble(CraftingContainer inv) {
		ItemStack result = super.assemble(inv);
		ItemStack input = ItemStack.EMPTY;

		for (int i = 0; i < inv.getContainerSize(); i++) {
			input = inv.getItem(i);
			if (input.is(UPGRADE_COPY_NBT.getKey())) {
				break;
			}
		}

		result.setTag(mergeTags(result.getTag(), input.getTag()));
		return result;

	}

	@NotNull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
		NonNullList<ItemStack> original = super.getRemainingItems(container);

		for (int i = 0; i < Math.min(original.size(), replacements.size()); i++) {
			ItemStack replacement = replacements.get(i).copy();
			if (!replacement.isEmpty())
				original.set(i, replacement);
		}
		return original;
	}
	// this is mainly so we can use the super methods
	public static class Serializer extends ShapelessRecipe.Serializer {

		@NotNull
		@Override
		public UpgradeShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			ShapelessRecipe original = super.fromJson(recipeId, json);

			NonNullList<ItemStack> replacements = NonNullList.create();

			if (json.has("replacements")) {
				JsonElement replacementsElement = json.get("replacements");
				if (!replacementsElement.isJsonArray())
					throw new JsonSyntaxException("Expected replacements to be an array");

				JsonArray replacementsArray = replacementsElement.getAsJsonArray();
				replacements = NonNullList.withSize(replacementsArray.size(), ItemStack.EMPTY);

				for (int i = 0; i < replacementsArray.size();i++) {
					JsonElement element = replacementsArray.get(i);
					replacements.set(i, itemFromJson(element));
				}
			}


			return new UpgradeShapelessRecipe(original, replacements);
		}
		@SuppressWarnings("Java8ListReplaceAll ") //stop it telling me to use replaceAll for the for loop, cause it makes the code harder to read (especially since this is half of a whole)
		public UpgradeShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			ShapelessRecipe original = super.fromNetwork(recipeId, buffer);

			int i = buffer.readInt();
			NonNullList<ItemStack> replacements = NonNullList.withSize(i, ItemStack.EMPTY);
			for (int j = 0; j < replacements.size();j++) {
				replacements.set(j, buffer.readItem());
			}

			return new UpgradeShapelessRecipe(original, replacements);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, ShapelessRecipe recipe) {
			// this cast should be safe... right?
			toNetwork(buffer, (UpgradeShapelessRecipe) recipe);
		}

		public void toNetwork(FriendlyByteBuf buffer, UpgradeShapelessRecipe recipe) {
			super.toNetwork(buffer, recipe);

			int i = recipe.replacements.size();
			buffer.writeInt(i);
			for (ItemStack item : recipe.replacements) {
				buffer.writeItemStack(item, true);
			}
		}

		private ItemStack itemFromJson(JsonElement json) {
			if (json instanceof JsonObject o) {
				ItemStack item = itemFromString(o.get("item").getAsString());
				if (o.has("nbt")) {
					try {
						item.setTag(TagParser.parseTag(o.get("nbt").getAsString()));
					} catch (CommandSyntaxException e) {
						throw new JsonSyntaxException("Error while parsing NBT for item '" + item.getItem() + "':",e);
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
