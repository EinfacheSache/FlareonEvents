package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.armor.assassins.UgronsChestplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class UgronListener implements Listener {

    private final NamespacedKey namespacedKey = new NamespacedKey("mythicmobs", "type");

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow && Objects.equals(event.getEntity().getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING), "ugron")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if(!Objects.equals(entity.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING), "ugron")) {
            return;
        }

        if(!Config.isEventStarted()){
            return;
        }

        Bukkit.broadcast(Component.text("§cDer mächtige §4§lUgron §cwurde besiegt!"));
        Bukkit.getScheduler().runTaskLater(FlareonEvents.getPlugin(),
                () -> PortalCreateListener.setNether(true),
                2 * 20);

        Location loc = event.getEntity().getLocation();
        loc.getWorld().dropItem(loc, UgronsChestplate.create());
    }
}
