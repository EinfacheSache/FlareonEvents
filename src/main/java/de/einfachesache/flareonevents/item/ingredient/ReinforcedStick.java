package de.einfachesache.flareonevents.item.ingredient;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class ReinforcedStick {

    private static final String ITEM_NAME = "§6Reinforced Stick";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "reinforced_stick");
    private static final ItemStack item = ItemUtils.createCustomItem(Material.BREEZE_ROD, ITEM_NAME, namespacedKey);

    public static ShapedRecipe getReinforcedStickRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, getItem());
        recipe.shape(" C ", " I ", " C ");
        recipe.setIngredient('C', Material.CHAIN);
        recipe.setIngredient('I', Material.IRON_INGOT);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static String getItemName() {
        return ITEM_NAME;
    }

    public static ItemStack getItem() {
        item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        return item;
    }
}
