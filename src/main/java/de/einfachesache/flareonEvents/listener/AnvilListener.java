package de.einfachesache.flareonEvents.listener;

import de.einfachesache.flareonEvents.item.CustomItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilListener implements Listener {

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getItem(0);
        if (left == null || !left.hasItemMeta()) return;

        ItemStack result = event.getResult();
        if (result == null || !result.hasItemMeta()) return;

        ItemMeta resultMeta = result.getItemMeta();

        for (CustomItems item : CustomItems.values()) {
            if (item.matches(left)) {
                item.applyName(resultMeta);
                result.setItemMeta(resultMeta);
                event.setResult(result);
                break;
            }
        }
    }
}
