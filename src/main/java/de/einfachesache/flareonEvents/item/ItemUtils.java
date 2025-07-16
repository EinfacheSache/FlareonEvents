package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ItemUtils {

    static NamespacedKey invulnerable = new NamespacedKey(FlareonEvents.getPlugin(), "invulnerable");


    public static ItemStack createCustomItem(Material mat, String name, NamespacedKey itemKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setUnbreakable(true);
        meta.displayName(Component.text(name));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(invulnerable, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);

        item.addItemFlags(ItemFlag.HIDE_ENCHANTS);

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

    public static List<Component> getEnchantments(Map<Enchantment, Integer> enchantments) {
        List<Component> lore = new ArrayList<>();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String key = entry.getKey().getKey().getKey();
            String name = Arrays.stream(key.split("_"))
                    .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
            String roman = ItemUtils.toRoman(entry.getValue());
            lore.add(deserialize("§7➤ §4" + name + " " + roman));
        }

        lore.add(deserialize("§f"));

        return lore;
    }

    public static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    public static UUID stringToUUID(String attribut) {
        String combined = FlareonEvents.getPlugin().getName() + ":" + attribut;
        return UUID.nameUUIDFromBytes(combined.getBytes(StandardCharsets.UTF_8));
    }

    public static Component deserialize(String string) {
        return LegacyComponentSerializer.legacySection().deserialize(string);
    }

    public static String legacyString(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
