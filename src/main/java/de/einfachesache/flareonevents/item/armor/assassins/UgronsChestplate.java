package de.einfachesache.flareonevents.item.armor.assassins;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.util.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@SuppressWarnings("deprecation")
public class UgronsChestplate {

    public static NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "ugrons_chestplate");
    public static Material MATERIAL = Material.NETHERITE_CHESTPLATE;
    public static String DISPLAY_NAME = "§4§lUgrons Brustpanzer";

    public static ItemFlag[] ITEM_FLAGS = new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS};
    public static Map<Enchantment, Integer> ENCHANTMENTS = Map.of(Enchantment.PROTECTION, 2);
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS = new HashMap<>();


    public static ItemStack create() {
        ItemStack item = ItemUtils.createCustomItem(MATERIAL, DISPLAY_NAME, NAMESPACED_KEY);
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        ItemMeta meta = item.getItemMeta();

        for (var entry : ATTRIBUTE_MODIFIERS.entrySet()) {
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Enchantment, Integer> entry : ENCHANTMENTS.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Die Brustplatte des Wächters §4§lUgrons §7glüht wie ein ewiger Schild aus Flammen."));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Effekte:"));
        lore.add(serializer.deserialize("§bFire-Resistance §7wenn getragen"));
        lore.add(serializer.deserialize("§f"));
        lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        lore.add(serializer.deserialize("§f"));

        meta.lore(lore);
        meta.setCustomModelData(69);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }
}
