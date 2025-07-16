package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.item.tool.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PassiveEffects {

    public static void applyPassiveEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            whenInMainHand(player);
        }
    }

    private static void whenInMainHand(Player player) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (FireSword.isFireSwordItem(item)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false));
        }

        if (PoseidonsTrident.isPoseidonsTridentItem(item)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 30, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 30, 0, true, false));
        }

        if (NyxBow.isNyxBowItem(item)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0, true, false));
        }

        if ((ReinforcedPickaxe.isReinforcedPickaxeItem(item) || BetterReinforcedPickaxe.isBetterReinforcedPickaxeItem(item)) && WorldUtils.isPlayerInCave(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 30, 0, true, false));
        }
    }
}
