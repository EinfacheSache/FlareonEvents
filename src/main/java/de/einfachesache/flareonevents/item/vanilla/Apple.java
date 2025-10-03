package de.einfachesache.flareonevents.item.vanilla;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class Apple {

    public static final NamespacedKey NAMESPACED_KEY = NamespacedKey.minecraft("apple");
    public static final ItemStack ITEM = ItemStack.of(Material.APPLE);

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape(" S ", "SIS", " S ");
        recipe.setIngredient('S', Material.WHEAT_SEEDS);
        recipe.setIngredient('I', Material.STICK);

        recipe.setCategory(CraftingBookCategory.MISC);

        return recipe;
    }
}
