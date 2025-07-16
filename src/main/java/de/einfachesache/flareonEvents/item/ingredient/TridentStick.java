package de.einfachesache.flareonEvents.item.ingredient;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class TridentStick {

    private static final String ITEM_NAME = "§6Trident Stick";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "trident_stick");
    private static final ItemStack item = ItemUtils.createCustomItem(Material.LIGHTNING_ROD, ITEM_NAME, namespacedKey);

    public static ShapedRecipe getTridentStickRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, item);
        recipe.shape("ABA", "C C", "ABA");
        recipe.setIngredient('A', Material.PRISMARINE_CRYSTALS);
        recipe.setIngredient('B', Material.NETHERITE_SCRAP);
        recipe.setIngredient('C', Material.PRISMARINE_SHARD);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static String getItemName() {
        return ITEM_NAME;
    }

    public static ItemStack getItem() {
        return item;
    }
}
