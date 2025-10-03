package de.einfachesache.flareonevents.item.vanilla;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class GoldenApple {

    public static final NamespacedKey NAMESPACED_KEY = NamespacedKey.minecraft("golden_apple");
    public static final ItemStack ITEM = ItemStack.of(Material.GOLDEN_APPLE);

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape(" G ", "GAG", " G ");
        recipe.setIngredient('A', Material.APPLE);
        recipe.setIngredient('G', Material.GOLD_INGOT);

        recipe.setCategory(CraftingBookCategory.MISC);

        return recipe;
    }
}
