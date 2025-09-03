package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.item.ItemUtils;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class CustomItemHandler implements Listener {


    @EventHandler
    public void onDrop(ItemSpawnEvent event) {
        if (ItemUtils.isInvulnerable(event.getEntity().getItemStack())) {
            event.getEntity().setGlowing(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (ItemUtils.isInvulnerable(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (ItemUtils.isInvulnerable(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;

        if (ItemUtils.isInvulnerable(item.getItemStack())) {
            event.setCancelled(true);
        }
    }
}
