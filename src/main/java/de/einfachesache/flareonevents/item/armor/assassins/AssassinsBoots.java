package de.einfachesache.flareonevents.item.armor.assassins;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.util.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.BloodShard;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class AssassinsBoots implements Listener {

    public static NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "assassins_boots");
    public static Material MATERIAL = Material.LEATHER_BOOTS;
    public static String DISPLAY_NAME = "§6§lAssassins Boots";

    public static ItemFlag[] ITEM_FLAGS = new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS};
    public static Map<Enchantment, Integer> ENCHANTMENTS = new HashMap<>();
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS = new HashMap<>();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, create());
        recipe.shape(" Z ", "HPH", "DBD");
        recipe.setIngredient('Z', BloodShard.ITEM);
        recipe.setIngredient('H', SoulHeartCrystal.create());
        recipe.setIngredient('P', MATERIAL);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', Material.BLAZE_ROD);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

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
        lore.add(serializer.deserialize("§7Besonderheit: ..."));
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
