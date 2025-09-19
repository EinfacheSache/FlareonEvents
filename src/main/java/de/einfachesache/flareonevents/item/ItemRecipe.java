package de.einfachesache.flareonevents.item;

import de.einfachesache.flareonevents.FlareonEvents;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class ItemRecipe {

    private static final Set<String> bannedRecipes = Set.of("mace", "crafter", "tnt_minecart", "end_crystal", "respawn_anchor");

    public static void loadCustomRecipes() {
        Arrays.stream(CustomItem.getEnabledItems()).forEach(customItem ->
                Bukkit.addRecipe(customItem.getRecipe(), true));
    }

    public static void discoverCustomRecipe(Player player) {
        Arrays.stream(CustomItem.getEnabledItems()).forEach(customItem ->
                player.discoverRecipe(customItem.getNamespacedKey()));
    }

    public static void removeBannedRecipes() {
        bannedRecipes.forEach(bannedRecipe ->
                Bukkit.removeRecipe(NamespacedKey.minecraft(bannedRecipe), true));
    }

    public static void reloadAllPluginRecipes() {
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();

            if (recipe instanceof Keyed keyed) {
                NamespacedKey key = keyed.getKey();
                if (key.getNamespace().equals(new NamespacedKey(FlareonEvents.getPlugin(), "not_set").getNamespace())) {
                    it.remove();
                    FlareonEvents.getLogManager().info("Reload recipe: " + key);
                }
            }
        }
        loadCustomRecipes();
    }
}
