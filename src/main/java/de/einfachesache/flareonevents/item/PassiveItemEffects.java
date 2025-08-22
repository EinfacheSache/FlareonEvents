package de.einfachesache.flareonevents.item;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public final class PassiveItemEffects {

    // FAST: 1 s – “direkt”
    private static final int FAST_PERIOD_TICKS  = 20;                     // 1 s
    private static final int FAST_EFFECT_TICKS  = FAST_PERIOD_TICKS + 5;  // 25 Ticks (~1.25 s)
    private static final int FAST_REFRESH_TICKS = FAST_PERIOD_TICKS + 1;  // 21 Ticks -> vor Ablauf erneuern

    // SLOW: 2.5 s
    private static final int SLOW_PERIOD_TICKS  = 50;                         // 2.5 s
    private static final int SLOW_EFFECT_TICKS  = SLOW_PERIOD_TICKS * 2 + 10; // 110 Ticks ≈ 5.5 s
    private static final int SLOW_REFRESH_TICKS = SLOW_PERIOD_TICKS + 1;      // 2.5 s + 1 Tick

    public static void applyPassiveEffects() {
        var plugin = FlareonEvents.getPlugin();
        Bukkit.getScheduler().runTaskTimer(plugin, PassiveItemEffects::tickFast, FAST_PERIOD_TICKS, FAST_PERIOD_TICKS);
        Bukkit.getScheduler().runTaskTimer(plugin, PassiveItemEffects::tickSlow, SLOW_PERIOD_TICKS, SLOW_PERIOD_TICKS);
    }

    private static void tickFast() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isNoRelevantMode(p)) continue;

            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType().isAir()) continue;

            List<PotionEffect> add = new ArrayList<>(3);

            if (ItemUtils.isCustomItem(item, CustomItem.FIRE_SWORD)) {
                maybeAdd(add, p, PotionEffectType.FIRE_RESISTANCE, 0, FAST_EFFECT_TICKS, FAST_REFRESH_TICKS);
                maybeAdd(add, p, PotionEffectType.STRENGTH,       0, FAST_EFFECT_TICKS, FAST_REFRESH_TICKS);
            } else if (ItemUtils.isCustomItem(item, CustomItem.POSEIDONS_TRIDENT)) {
                maybeAdd(add, p, PotionEffectType.DOLPHINS_GRACE,  0, FAST_EFFECT_TICKS, FAST_REFRESH_TICKS);
                maybeAdd(add, p, PotionEffectType.WATER_BREATHING, 0, FAST_EFFECT_TICKS, FAST_REFRESH_TICKS);
            } else if (ItemUtils.isCustomItem(item, CustomItem.NYX_BOW)) {
                maybeAdd(add, p, PotionEffectType.SPEED, 0, FAST_EFFECT_TICKS, FAST_REFRESH_TICKS);
            }

            if (!add.isEmpty()) p.addPotionEffects(add);
        }
    }

    private static void tickSlow() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isNoRelevantMode(p)) continue;

            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType().isAir()) continue;

            boolean isPickaxe = ItemUtils.isCustomItem(item, CustomItem.REINFORCED_PICKAXE) || ItemUtils.isCustomItem(item, CustomItem.SUPERIOR_PICKAXE);
            if (!isPickaxe) continue;

            PotionEffect cur = p.getPotionEffect(PotionEffectType.NIGHT_VISION);
            if (cur != null && cur.getDuration() >= SLOW_REFRESH_TICKS) continue;

            if (WorldUtils.isPlayerInCave(p)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                        SLOW_EFFECT_TICKS, 0, true, false));
            }
        }
    }

    private static boolean isNoRelevantMode(Player p) {
        GameMode gm = p.getGameMode();
        return gm != GameMode.SURVIVAL && gm != GameMode.ADVENTURE && gm != GameMode.CREATIVE;
    }

    private static void maybeAdd(List<PotionEffect> list, Player p, PotionEffectType type, int amplifier, int duration, int refreshThreshold) {
        PotionEffect cur = p.getPotionEffect(type);
        if (cur == null || cur.getAmplifier() < amplifier || cur.getDuration() < refreshThreshold) {
            list.add(new PotionEffect(type, duration, amplifier, true, false));
        }
    }
}