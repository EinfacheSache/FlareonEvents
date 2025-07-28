package de.einfachesache.flareonevents.item.ingredient;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class GoldShard {

    public static final String DISPLAY_NAME = "§6Gold Shard";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "gold_shard");
    public static final ItemStack ITEM = ItemUtils.createCustomItem(Material.GOLD_INGOT, DISPLAY_NAME, NAMESPACED_KEY);

    public static ShapedRecipe getGoldShardRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape("AAA", "ABA", "AAA");
        recipe.setIngredient('A', Material.GOLD_INGOT);
        recipe.setIngredient('B', Material.NETHERITE_SCRAP);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }
}

