package de.einfachesache.flareonevents.item.ingredient;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@SuppressWarnings("deprecation")
public class TridentStick {

    public static final String DISPLAY_NAME = "§6Trident Stick";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "trident_stick");
    public static final ItemStack ITEM = createTridentStick();

    public static ShapedRecipe getTridentStickRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape("ABA", "C C", "ABA");
        recipe.setIngredient('A', Material.PRISMARINE_CRYSTALS);
        recipe.setIngredient('B', Material.NETHERITE_SCRAP);
        recipe.setIngredient('C', Material.PRISMARINE_SHARD);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    private static ItemStack createTridentStick() {
        ItemStack itemStack = ItemUtils.createCustomItem(Material.LIGHTNING_ROD, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(69);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
