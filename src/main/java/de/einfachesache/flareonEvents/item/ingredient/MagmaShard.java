package de.einfachesache.flareonEvents.item.ingredient;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class MagmaShard {

    private static final String itemName = "§6Magma Shard";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "magma_shard");
    private static final ItemStack item = ItemUtils.createCustomItem(Material.MAGMA_CREAM, itemName, namespacedKey);

    public static ShapedRecipe getMagmaShardRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, item);
        recipe.shape("AAA", "ABA", "AAA");
        recipe.setIngredient('A', Material.MAGMA_BLOCK);
        recipe.setIngredient('B', Material.NETHERITE_SCRAP);
        return recipe;
    }

    public static ItemStack getItem() {
        return item;
    }
}
