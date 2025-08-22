package de.einfachesache.flareonevents.item;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsBoots;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsChestplate;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsHelmet;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsLeggings;
import de.einfachesache.flareonevents.item.ingredient.*;
import de.einfachesache.flareonevents.item.tool.ReinforcedPickaxe;
import de.einfachesache.flareonevents.item.tool.SuperiorPickaxe;
import de.einfachesache.flareonevents.item.weapon.FireSword;
import de.einfachesache.flareonevents.item.weapon.NyxBow;
import de.einfachesache.flareonevents.item.weapon.PoseidonsTrident;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;

public class ItemRecipe {

    public static void loadRecipes() {
        Bukkit.addRecipe(FireSword.getFireSwordRecipe(), true);
        Bukkit.addRecipe(NyxBow.getNyxBowRecipe(), true);
        Bukkit.addRecipe(PoseidonsTrident.getPoseidonsTridentRecipe(), true);

        Bukkit.addRecipe(ReinforcedPickaxe.getReinforcedPickaxeRecipe(), true);
        Bukkit.addRecipe(SuperiorPickaxe.getSuperiorPickaxeRecipe(), true);

        Bukkit.addRecipe(AssassinsHelmet.getAssassinsAmorRecipe(), true);
        Bukkit.addRecipe(AssassinsChestplate.getAssassinsAmorRecipe(), true);
        Bukkit.addRecipe(AssassinsLeggings.getAssassinsAmorRecipe(), true);
        Bukkit.addRecipe(AssassinsBoots.getAssassinsAmorRecipe(), true);

        Bukkit.addRecipe(GoldShard.getGoldShardRecipe(), true);
        Bukkit.addRecipe(MagmaShard.getMagmaShardRecipe(), true);
        Bukkit.addRecipe(ReinforcedStick.getReinforcedStickRecipe(), true);
        Bukkit.addRecipe(TridentSpikes.getTridentSpikesRecipe(), true);
        Bukkit.addRecipe(TridentStick.getTridentStickRecipe(), true);
    }

    public static void discoverRecipe(Player player) {
        player.discoverRecipe(FireSword.getFireSwordRecipe().getKey());
        player.discoverRecipe(NyxBow.getNyxBowRecipe().getKey());
        player.discoverRecipe(PoseidonsTrident.getPoseidonsTridentRecipe().getKey());

        player.discoverRecipe(ReinforcedPickaxe.getReinforcedPickaxeRecipe().getKey());
        player.discoverRecipe(SuperiorPickaxe.getSuperiorPickaxeRecipe().getKey());

        player.discoverRecipe(AssassinsHelmet.getAssassinsAmorRecipe().getKey());
        player.discoverRecipe(AssassinsChestplate.getAssassinsAmorRecipe().getKey());
        player.discoverRecipe(AssassinsLeggings.getAssassinsAmorRecipe().getKey());
        player.discoverRecipe(AssassinsBoots.getAssassinsAmorRecipe().getKey());

        player.discoverRecipe(GoldShard.getGoldShardRecipe().getKey());
        player.discoverRecipe(MagmaShard.getMagmaShardRecipe().getKey());
        player.discoverRecipe(ReinforcedStick.getReinforcedStickRecipe().getKey());
        player.discoverRecipe(TridentSpikes.getTridentSpikesRecipe().getKey());
        player.discoverRecipe(TridentStick.getTridentStickRecipe().getKey());
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
        loadRecipes();
    }
}
