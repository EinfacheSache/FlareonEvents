package de.einfachesache.flareonEvents.item.ingredient;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class TridentStick {

    private static final String itemName = "§6Trident Stick";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "trident_stick");
    private static final ItemStack item = ItemUtils.createCustomItem(Material.LIGHTNING_ROD, itemName, namespacedKey);

    public static ShapedRecipe getTridentStickRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, item);
        recipe.shape("ABA", "C C", "ABA");
        recipe.setIngredient('A', Material.PRISMARINE_CRYSTALS);
        recipe.setIngredient('B', Material.NETHERITE_SCRAP);
        recipe.setIngredient('C', Material.PRISMARINE_SHARD);
        return recipe;
    }

    public static ItemStack getItem() {
        return item;
    }
}
