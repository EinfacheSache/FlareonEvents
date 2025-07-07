package de.einfachesache.flareonEvents.item.ingredient;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class TridentSpikes {

    private static final String ITEM_NAME = "§6Trident Shard";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "trident_spikes");
    private static final ItemStack item = ItemUtils.createCustomItem(Material.PRISMARINE_SHARD, ITEM_NAME, namespacedKey);

    public static ShapedRecipe getTridentSpikesRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, item);
        recipe.shape("AAA", "   ", "   ");
        recipe.setIngredient('A', Material.IRON_BLOCK);

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
