package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.FlareonEvents;
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

    private static final String ITEM_NAME = "§6100-Spieler-Event Info";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "event_info_book");

    public static ItemStack createEventInfoBook() {

        ItemStack book = ItemUtils.createCustomItem(Material.WRITTEN_BOOK, ITEM_NAME, namespacedKey);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("§eFlareonDev-Team");

        book.setItemMeta((ItemMeta) meta.pages(Config.getInfoBookSorted().values().stream().toList()));

        return book;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        ItemStack clicked = e.getCurrentItem();
        if (e.getSlot() != 8 || clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getItemMeta().getPersistentDataContainer().has(EventInfoBook.namespacedKey)) {
            e.setResult(Event.Result.DENY);
            e.getView().setCursor(null);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {

        ItemStack clicked = e.getItemDrop().getItemStack();
        if (!clicked.hasItemMeta()) return;

        if (clicked.getItemMeta().getPersistentDataContainer().has(EventInfoBook.namespacedKey)) {
            e.setCancelled(true);
        }
    }

    public static String getItemName() {
        return ITEM_NAME;
    }

    public static NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }
}
