package de.einfachesache.flareonEvents.item.tool;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import de.einfachesache.flareonEvents.WorldUtils;
import de.einfachesache.flareonEvents.item.ingredient.ReinforcedStick;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"deprecation"})
public class ReinforcedPickaxe implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    public static ShapedRecipe getReinforcedPickaxeRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createReinforcedPickaxe());
        recipe.shape("GBG", "DSD", " S ");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('B', Material.GOLD_BLOCK);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', ReinforcedStick.getItem());

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static boolean isReinforcedPickaxeItem(ItemStack item) {
        if (item == null || item.getType() != MATERIAL) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return DISPLAY_NAME.equalsIgnoreCase(item.getItemMeta().getDisplayName());
    }

    public static ItemStack createReinforcedPickaxe() {
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
        lore.add(serializer.deserialize("§7Beim §bAbbauen §7von §6Erzen §7können"));
        lore.add(serializer.deserialize("§7zufällig andere §6Erze §7droppen:"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7➤ §e5%§7\u2009\u2009§bDiamant"));
        lore.add(serializer.deserialize("§7➤ §e15%§7 §6Raw Gold"));
        lore.add(serializer.deserialize("§7➤ §e20%§7 §fRaw Eisen"));
        lore.add(serializer.deserialize("§7➤ §e10%§7 §8Kohle"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Besonderheit: §bNight Vision §7in Höhlen"));
        lore.add(serializer.deserialize("§f"));

        // Dynamisch aus der geladenen ENCHANTMENTS-Map
        if (!ReinforcedPickaxe.ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ReinforcedPickaxe.ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ReinforcedPickaxe.ENCHANTMENTS));
        }

        meta.lore(lore);
        meta.setCustomModelData(1);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isReinforcedPickaxeItem(item)) return;

        Material blockType = event.getBlock().getType();

        if (!WorldUtils.isOre(blockType)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Material drop = null;
        int roll = FlareonEvents.getRandom().nextInt(100) + 1;
        if (roll <= 5) {
            drop = Material.DIAMOND;
        } else if (roll <= 20) {
            drop = Material.RAW_GOLD;
        } else if (roll <= 40) {
            drop = Material.RAW_IRON;
        } else if (roll <= 50) {
            drop = Material.COAL;
        }

        if (drop != null) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(drop));
        }
    }

    public static String getItemName() {
        return DISPLAY_NAME;
    }
}
