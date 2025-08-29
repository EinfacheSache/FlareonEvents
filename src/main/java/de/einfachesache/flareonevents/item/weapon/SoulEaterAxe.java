package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.MagmaShard;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.*;

@SuppressWarnings("deprecation")
public class SoulEaterAxe implements Listener {

    public static NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "soul_eater_axe");
    public static Material MATERIAL = Material.IRON_AXE;
    public static String DISPLAY_NAME = "§5§lSoul Eater Axe";

    public static ItemFlag[] ITEM_FLAGS = new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS};
    public static Map<Enchantment, Integer> ENCHANTMENTS = new HashMap<>();
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS = new HashMap<>();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createSoulEaterAxe());
        recipe.shape(" Z ", "HPH", "DBD");
        recipe.setIngredient('Z', MagmaShard.ITEM);
        recipe.setIngredient('H', SoulHeartCrystal.createSoulHeartCrystal());
        recipe.setIngredient('P', MATERIAL);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', Material.BLAZE_ROD);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack createSoulEaterAxe() {
        ItemStack item = ItemUtils.createCustomItem(MATERIAL, DISPLAY_NAME, NAMESPACED_KEY);
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        ItemMeta meta = item.getItemMeta();

        for (var entry : ATTRIBUTE_MODIFIERS.entrySet()) {
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Enchantment, Integer> entry : ENCHANTMENTS.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        var modifiers = meta.getAttributeModifiers();
        double attackDamage = ((modifiers == null)
                ? Collections.<AttributeModifier>emptyList()
                : modifiers.get(Attribute.ATTACK_DAMAGE))
                .stream().filter(Objects::nonNull)
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Besonderheit: ..."));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Schaden: §4" + attackDamage));
        lore.add(serializer.deserialize("§f"));

        // Dynamisch aus ENCHANTMENTS-Map
        if (!ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        }

        meta.lore(lore);
        meta.setCustomModelData(69);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }
}
