package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtils {

    static NamespacedKey invulnerable = new NamespacedKey(FlareonEvents.getPlugin(), "invulnerable");

    public static ItemStack createCustomItem(Material mat, String name, NamespacedKey itemKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setUnbreakable(true);
        meta.displayName(Component.text(name));
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(invulnerable, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createGuiItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), "gui_id"), org.bukkit.persistence.PersistentDataType.STRING, getExactDisplayName(item));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGuiItemFromMaterial(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), "gui_id"), org.bukkit.persistence.PersistentDataType.STRING, name);
        item.setItemMeta(meta);
        return item;
    }

    public static String getExactDisplayName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return "";
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "";
        Component display = meta.displayName();
        if (display == null) return "";
        return LegacyComponentSerializer.legacySection().serialize(display);
    }

    public static String legacyString(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
