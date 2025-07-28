package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.item.ingredient.*;
import de.einfachesache.flareonEvents.item.tool.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ItemRecipe {

    public static void loadRecipes() {

        Bukkit.addRecipe(FireSword.getFireSwordRecipe(), true);
        Bukkit.addRecipe(NyxBow.getNyxBowRecipe(), true);
        Bukkit.addRecipe(PoseidonsTrident.getPoseidonsTridentRecipe(), true);

        Bukkit.addRecipe(ReinforcedPickaxe.getReinforcedPickaxeRecipe(), true);
        Bukkit.addRecipe(BetterReinforcedPickaxe.getBetterReinforcedPickaxeRecipe(), true);

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
        player.discoverRecipe(BetterReinforcedPickaxe.getBetterReinforcedPickaxeRecipe().getKey());

        player.discoverRecipe(GoldShard.getGoldShardRecipe().getKey());
        player.discoverRecipe(MagmaShard.getMagmaShardRecipe().getKey());
        player.discoverRecipe(ReinforcedStick.getReinforcedStickRecipe().getKey());
        player.discoverRecipe(TridentSpikes.getTridentSpikesRecipe().getKey());
        player.discoverRecipe(TridentStick.getTridentStickRecipe().getKey());
    }
}
