package de.einfachesache.flareonEvents.item.ingredient;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class GoldShard {

    private static final String itemName = "§6Gold Shard";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "gold_shard");
    private static final ItemStack item = ItemUtils.createCustomItem(Material.GOLD_INGOT, itemName, namespacedKey);

    public static ShapedRecipe getGoldShardRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, item);
        recipe.shape("AAA", "ABA", "AAA");
        recipe.setIngredient('A', Material.GOLD_INGOT);
        recipe.setIngredient('B', Material.NETHERITE_SCRAP);
        return recipe;
    }

    public static ItemStack getItem() {
        return item;
    }
}

