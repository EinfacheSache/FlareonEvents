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

    public static final String DISPLAY_NAME = "§6Reinforced Stick";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "reinforced_stick");
    public static final ItemStack ITEM = ItemUtils.createCustomItem(Material.BREEZE_ROD, DISPLAY_NAME, NAMESPACED_KEY);

    public static ShapedRecipe getReinforcedStickRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createReinforcedStick());
        recipe.shape(" C ", " I ", " C ");
        recipe.setIngredient('C', Material.CHAIN);
        recipe.setIngredient('I', Material.IRON_INGOT);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack createReinforcedStick() {
        ITEM.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        return ITEM;
    }
}
