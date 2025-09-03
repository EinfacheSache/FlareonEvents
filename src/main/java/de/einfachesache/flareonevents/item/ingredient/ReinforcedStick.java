package de.einfachesache.flareonevents.item.ingredient;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@SuppressWarnings("deprecation")
public class ReinforcedStick {

    public static final String DISPLAY_NAME = "§6Reinforced Stick";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "reinforced_stick");
    public static final ItemStack ITEM = createReinforcedStick();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createReinforcedStick());
        recipe.shape(" C ", " I ", " C ");
        recipe.setIngredient('C', Material.CHAIN);
        recipe.setIngredient('I', Material.IRON_INGOT);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    private static ItemStack createReinforcedStick() {
        ItemStack itemStack = ItemUtils.createCustomItem(Material.BREEZE_ROD, DISPLAY_NAME, NAMESPACED_KEY);
        itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(69);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
