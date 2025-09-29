package de.einfachesache.flareonevents.listener;

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
        ItemStack left = inv.getFirstItem();
        if (left == null || !left.hasItemMeta()) return;

        ItemStack result = event.getResult();
        if (result == null || !result.hasItemMeta()) return;

        if(ItemUtils.isInvulnerable(left)) {

            AnvilView anvilView = event.getView();
            ItemMeta resultMeta = result.getItemMeta();

            if(inv.getSecondItem() == null) {
                event.setResult(null);
                return;
            }

            resultMeta.displayName(left.effectiveName());
            result.setItemMeta(resultMeta);

            int base = anvilView.getRepairCost();
            int newCost = (int) Math.max(0, Math.round((base - 1) * 1.5));

            anvilView.setRepairCost(newCost);
            anvilView.setMaximumRepairCost(Math.max(newCost, anvilView.getMaximumRepairCost()));

            ItemUtils.rewriteEnchantLoreWithNewEnchantments(result);
        }
    }
}
