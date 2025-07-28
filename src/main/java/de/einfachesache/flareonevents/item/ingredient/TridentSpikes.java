package de.einfachesache.flareonevents.item.ingredient;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class TridentSpikes {

    public static final String DISPLAY_NAME = "§6Trident Shard";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "trident_spikes");
    public static final ItemStack ITEM = ItemUtils.createCustomItem(Material.PRISMARINE_SHARD, DISPLAY_NAME, NAMESPACED_KEY);

    public static ShapedRecipe getTridentSpikesRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape("AAA", "   ", "   ");
        recipe.setIngredient('A', Material.IRON_BLOCK);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }
}
