package de.einfachesache.flareonEvents.item.ingredient;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class TridentSpikes {

    private static final String itemName = "§6Trident Shard";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "trident_spikes");
    private static final ItemStack item = ItemUtils.createCustomItem(Material.PRISMARINE_SHARD, itemName, namespacedKey);

    public static ShapedRecipe getTridentSpikesRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, item);
        recipe.shape("AAA", "   ", "   ");
        recipe.setIngredient('A', Material.IRON_BLOCK);
        return recipe;
    }

    public static ItemStack getItem() {
        return item;
    }
}
