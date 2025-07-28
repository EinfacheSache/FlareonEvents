package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.FlareonEvents;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomItemHandler implements Listener {


    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (isProtected(event.getItemDrop().getItemStack())) {
            event.getItemDrop().setGlowing(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (isProtected(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isProtected(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;

        if (isProtected(item.getItemStack())) {
            event.setCancelled(true);
        }
    }

    private boolean isProtected(ItemStack item) {
        if (!item.hasItemMeta()) return false;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(FlareonEvents.getPlugin(), "invulnerable");
        return container.has(key, PersistentDataType.BYTE);
    }

}
