package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.handler.TeamHandler;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityDamageListener implements Listener {

    NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "lightning_player_death");

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (damager instanceof LightningStrike && (target instanceof Item
                || damager.getPersistentDataContainer().has(namespacedKey)
                || damager.getPersistentDataContainer().has(new NamespacedKey(FlareonEvents.getPlugin(), "lightning_trident_" + target.getName().toLowerCase().replaceAll("[^a-z0-9_.\\-/]", "")), PersistentDataType.BYTE))) {
            event.setCancelled(true);
            return;
        }

        if (!(target instanceof Player player)) return;

        if (damager.getType() == EntityType.END_CRYSTAL || damager.getType() == EntityType.TNT_MINECART) {
            event.setCancelled(true);
        }

        if (damager instanceof Player playerDamager && TeamHandler.arePlayersOnSameTeam(player, playerDamager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if (event.getCause() == EntityDamageEvent.DamageCause.KILL) {
            return;
        }

        Entity entity = event.getEntity();

        if (!Config.isEventIsRunning() && entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE) {
            event.setCancelled(true);
            return;
        }

        if (entity instanceof Item & event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.setCancelled(true);
            entity.setFireTicks(1);
        }
    }
}
