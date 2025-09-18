package de.einfachesache.flareonevents.item.misc;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class EventInfoBook implements Listener {

    public static final String DISPLAY_NAME = "§6100-Spieler-Event Info";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "event_info_book");

    public static ItemStack create() {

        ItemStack book = ItemUtils.createCustomItem(Material.WRITTEN_BOOK, DISPLAY_NAME, NAMESPACED_KEY);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("§eFlareonDev-Team");

        book.setItemMeta((ItemMeta) meta.pages(Config.getInfoBookSorted().values().stream().toList()));

        return book;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        ItemStack clicked = e.getCurrentItem();
        if (e.getSlot() != 8 || clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getItemMeta().getPersistentDataContainer().has(EventInfoBook.NAMESPACED_KEY)) {
            e.setResult(Event.Result.DENY);
            e.getView().setCursor(null);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {

        ItemStack clicked = e.getItemDrop().getItemStack();
        if (!clicked.hasItemMeta()) return;

        if (clicked.getItemMeta().getPersistentDataContainer().has(EventInfoBook.NAMESPACED_KEY)) {
            e.setCancelled(true);
        }
    }
}
