package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
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

        if (damager instanceof LightningStrike && (event.getEntity() instanceof Item || damager.getPersistentDataContainer().has(namespacedKey) || damager.getPersistentDataContainer().has(new NamespacedKey(FlareonEvents.getPlugin(), "trident_lightning_" + event.getEntity().getName().toLowerCase().replace(" ", "")), PersistentDataType.BYTE))) {
            event.setCancelled(true);
        }

        if (event.getEntity() instanceof Player && damager.getType() == EntityType.END_CRYSTAL || damager.getType() == EntityType.TNT_MINECART) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if(event.getCause() == EntityDamageEvent.DamageCause.KILL) {
            return;
        }

        Entity entity = event.getEntity();

        if (entity instanceof Player player) {
            if (player.getGameMode() == GameMode.ADVENTURE && !player.getWorld().getPVP() && !Config.isEventIsRunning()) {
                event.setCancelled(true);
                return;
            }
        }

        if (entity instanceof Item & event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.setCancelled(true);
            entity.setFireTicks(1);
        }
    }
}
