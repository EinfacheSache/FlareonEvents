package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.util.ItemUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;

public class EnchantItemListener implements Listener {

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        if (ItemUtils.isInvulnerable(e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        GrindstoneInventory inv = event.getInventory();
        if (ItemUtils.isInvulnerable(inv.getUpperItem()) || ItemUtils.isInvulnerable(inv.getLowerItem())) {
            event.setResult(null);
        }
    }
}
