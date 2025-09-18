package de.einfachesache.flareonevents.item.ingredient;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@SuppressWarnings("deprecation")
public class DemonSoul {

    public static final String DISPLAY_NAME = "§6Demon Soul";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "demon_soul");
    public static final ItemStack ITEM = create();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape("AAA", "ABA", "AAA");
        recipe.setIngredient('A', Material.REDSTONE_BLOCK);
        recipe.setIngredient('B', Material.NETHERITE_SCRAP);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    private static ItemStack create() {
        ItemStack itemStack = ItemUtils.createCustomItem(Material.REDSTONE, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(69);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
