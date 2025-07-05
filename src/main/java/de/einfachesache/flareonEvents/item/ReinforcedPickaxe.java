package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"removal","deprecation"})
public class ReinforcedPickaxe implements Listener {

    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "reinforced_pickaxe");
    private static final String ITEM_NAME = "§8§lReinforced Pickaxe";

    public static ShapedRecipe getReinforcedPickaxe() {
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, createMiningPickaxe());
        recipe.shape("GBG", "DSD", " S ");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('B', Material.GOLD_BLOCK);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }

    public static boolean isReinforcedPickaxeItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ITEM_NAME.equalsIgnoreCase(item.getItemMeta().getDisplayName());
    }

    public static ItemStack createMiningPickaxe() {
        ItemStack item = ItemUtils.createCustomItem(Material.NETHERITE_PICKAXE, ITEM_NAME, namespacedKey);

        ItemMeta meta = item.getItemMeta();

        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.attack_speed", 1.2-4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = Stream.of(
                "§f",
                "§7Besonderheit: Beim §bAbbauen §7von §6Erzen §7können",
                "§7zufällig andere §6Erze §7droppen:",
                "§f",
                "§7➤ §e10%§7 §bDiamant",
                "§7➤ §e20%§7 §6Gold",
                "§7➤ §e20%§7 §fEisen",
                "§7➤ §e10%§7 §8Kohle",
                "§f",
                "§7Enchantment: §4Efficiency III",
                "§f"
        ).map(serializer::deserialize).collect(Collectors.toList());

        meta.lore(lore);
        meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
        item.setItemMeta(meta);

        item.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        return item;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isReinforcedPickaxeItem(item)) return;

        Material blockType = event.getBlock().getType();

        if (!isOre(blockType)) return;

        int roll = new Random().nextInt(10) + 1;
        Material drop = switch (roll) {
            case 1 -> Material.DIAMOND;
            case 2, 3 -> Material.GOLD_INGOT;
            case 4, 5 -> Material.IRON_INGOT;
            case 6 -> Material.COAL;
            default -> null;
        };

        if (drop != null) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(drop));
        }
    }

    private boolean isOre(Material material) {
        return switch (material) {
            case COAL_ORE, IRON_ORE, GOLD_ORE, COPPER_ORE, DIAMOND_ORE, EMERALD_ORE,
                 DEEPSLATE_COAL_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_GOLD_ORE, DEEPSLATE_COPPER_ORE,
                 DEEPSLATE_DIAMOND_ORE, DEEPSLATE_EMERALD_ORE -> true;
            default -> false;
        };
    }

    public static String getItemName() {
        return ITEM_NAME;
    }
}
