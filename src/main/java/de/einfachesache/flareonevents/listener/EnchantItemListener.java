package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.util.ItemUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class EnchantItemListener implements Listener {

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        if (ItemUtils.isInvulnerable(e.getItem())) {
            e.setCancelled(true);
        }
    }
}
