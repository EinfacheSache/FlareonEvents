package de.einfachesache.flareonevents.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.EnumSet;
import java.util.Set;

public class PotionEffectsListener implements Listener {

    private static final Set<EntityPotionEffectEvent.Cause> BLOCKED = EnumSet.of(
            EntityPotionEffectEvent.Cause.POTION_DRINK,
            EntityPotionEffectEvent.Cause.POTION_SPLASH,
            EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD
    );

    @EventHandler(ignoreCancelled = true)
    public void onEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        var action = event.getAction();
        if (action != EntityPotionEffectEvent.Action.ADDED && action != EntityPotionEffectEvent.Action.CHANGED) return;

        if (BLOCKED.contains(event.getCause())) {
            event.setCancelled(true);
            player.sendMessage("§cTränke sind im Event deaktiviert.");
        }
    }
}
