package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.util.ItemUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

@SuppressWarnings("UnstableApiUsage")
public class AnvilListener implements Listener {

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getItem(0);
        if (left == null || !left.hasItemMeta()) return;

        ItemStack result = event.getResult();
        if (result == null || !result.hasItemMeta()) return;

        ItemMeta resultMeta = result.getItemMeta();

        for (CustomItem item : CustomItem.values()) {
            if (item.matches(left)) {
                item.applyName(resultMeta);
                result.setItemMeta(resultMeta);
                break;
            }
        }

        if(ItemUtils.isInvulnerable(left)) {
            AnvilView anvilView = event.getView();

            int base = anvilView.getRepairCost();
            int newCost = (int) Math.max(0, Math.round(base * 1.5));

            anvilView.setRepairCost(newCost);
            anvilView.setMaximumRepairCost(Math.max(newCost, anvilView.getMaximumRepairCost()));

            ItemUtils.rewriteEnchantLoreWithNewEnchantments(result);
        }
    }
}
