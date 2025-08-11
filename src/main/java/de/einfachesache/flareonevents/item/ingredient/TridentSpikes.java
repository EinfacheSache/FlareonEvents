package de.einfachesache.flareonevents.item.ingredient;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

@SuppressWarnings("deprecation")
public class TridentSpikes {

    public static final String DISPLAY_NAME = "§6Trident Spikes";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "trident_spikes");
    public static final ItemStack ITEM = createTridentSpikes();

    public static ShapedRecipe getTridentSpikesRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, ITEM);
        recipe.shape(" H ", " F ", " H ");
        recipe.setIngredient('H', SoulHeartCrystal.createSoulHeartCrystal());
        recipe.setIngredient('F', Material.WARPED_FUNGUS);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    private static ItemStack createTridentSpikes() {
        ItemStack itemStack = ItemUtils.createCustomItem(Material.PRISMARINE_SHARD, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setCustomModelData(69);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
