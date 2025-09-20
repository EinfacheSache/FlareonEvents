package de.einfachesache.flareonevents.item.misc;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

@SuppressWarnings("deprecation")
public class EventInfoBook implements Listener {

    public static final String DISPLAY_NAME = "§6Event Information";
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "event_info_book");

    public static ItemStack create() {

        ItemStack book = ItemUtils.createCustomItem(Material.WRITTEN_BOOK, DISPLAY_NAME, NAMESPACED_KEY);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("§eFlareonDev-Team");
        meta.setCustomModelData(69);

        book.setItemMeta((ItemMeta) meta.pages(Config.getInfoBookSorted().values().stream().toList()));

        return book;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (isEventBook(e.getCurrentItem()) || isEventBook(e.getCursor())) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player p) {
                p.setItemOnCursor(null);
                p.updateInventory();
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isEventBook(e.getItemDrop().getItemStack()))
            e.setCancelled(true);
    }

    private boolean isEventBook(ItemStack itemStack) {
        return ItemUtils.isCustomItem(itemStack, CustomItem.EVENT_INFO_BOOK);
    }
}
