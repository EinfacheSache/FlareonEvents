package de.einfachesache.flareonevents.item.vanilla;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class Anvil {

    public static final NamespacedKey NAMESPACED_KEY = NamespacedKey.minecraft("anvil");
    public static final ItemStack ITEM = ItemStack.of(Material.ANVIL);

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape("III", " B ", "III");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.IRON_BLOCK);

        recipe.setCategory(CraftingBookCategory.MISC);

        return recipe;
    }
}

