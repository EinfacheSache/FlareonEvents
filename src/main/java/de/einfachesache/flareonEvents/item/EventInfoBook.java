package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.tool.FireSword;
import de.einfachesache.flareonEvents.item.tool.NyxBow;
import de.einfachesache.flareonEvents.item.tool.PoseidonsTrident;
import de.einfachesache.flareonEvents.item.tool.ReinforcedPickaxe;
import net.kyori.adventure.text.Component;
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

import java.util.Arrays;

public class EventInfoBook implements Listener {

    private static final String ITEM_NAME = "§6100-Spieler-Event Info";
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "event_info_book");

    public static ItemStack createEventInfoBook() {

        ItemStack book = ItemUtils.createCustomItem(Material.WRITTEN_BOOK, ITEM_NAME, namespacedKey);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("§eFlareonDev-Team");

        Component page1 = Component.text(
                """
                        §4§lInfos§7
                        
                        §cSpawn§7 bei §60 I 0§7
                        
                        §cPvP §7nach §62m§7
                        
                        §cNether §7nach §61h§7
                        
                        §cEnd §7ist §6geschlossen §7
                        
                        §cDie Zone §7startet bei §63000x3000§7
                        → §cBewegt§7 sich schrittweise zu §65 I 5§7
                        """);

        Component page2 = Component.text(
                """
                        §4§lRegeln§7
                        
                        §cCheaten §7verboten
                        
                        §cTeams §7bilden sich im §cEvent§7
                        
                        §cNur Voicechat§7 erlaubt
                        → externe Kommunikation ist §cverboten§7
                        
                        §cMax Teamgröße§7 ist §c5§7
                        
                        §cTrapping§7 ist §6erlaubt§7
                        """);

        Component page3 = Component.text(
                "§6Custom Items §7Part-1\n\n§7" +
                        "§7- " + FireSword.getItemName().replace("§l", "")       + "\n" +
                        "§7- " + PoseidonsTrident.getItemName().replace("§l", "")   + "\n" +
                        "§7- " + NyxBow.getItemName().replace("§l", "")            + "\n" +
                        "§7- " + ReinforcedPickaxe.getItemName().replace("§l", "")   + "\n");

        Component page4 = Component.text(
                """
                        §6Custom Items §7Part-2
                        
                        §71. Jedes §6Custom Item §7kann nur einmal gecraftet werden.
                        
                        §72. Jeder Spieler kann nur ein §6Custom Weapon   §7craften.
                        
                        §73. Alle §6Custom Items §7sind Unzerstörbar.
                        
                        """);

        Component page5 = Component.text(
                """
                        §cGebannte Items§7:
                        - Mace
                        - Potions
                        
                        §cGebannte Funktionen§7:
                        - TNT-Bed im Nether
                        - Anchor Explosion
                        
                        """);

        book.setItemMeta((ItemMeta) meta.pages(Arrays.asList(page1, page2, page3, page4, page5)));

        return book;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        ItemStack clicked = e.getCurrentItem();
        if (e.getSlot() != 8 || clicked == null || !clicked.hasItemMeta()) return;

        if(clicked.getItemMeta().getPersistentDataContainer().has(EventInfoBook.namespacedKey)) {
            e.setResult(Event.Result.DENY);
            e.getView().setCursor(null);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {

        ItemStack clicked = e.getItemDrop().getItemStack();
        if (!clicked.hasItemMeta()) return;

        if(clicked.getItemMeta().getPersistentDataContainer().has(EventInfoBook.namespacedKey)){
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
