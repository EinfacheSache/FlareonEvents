package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.util.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.MagmaShard;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class SoulEaterScythe implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    public static NamespacedKey KILLS_COUNT = new NamespacedKey(FlareonEvents.getPlugin(), "soul_kills");

    record Perk(String id, int threshold, String display) {
    }

    private static final List<Perk> SOUL_EATER_PERKS = List.of(
            new Perk("REGEN", 1, "Regeneration I"),
            new Perk("STRENGTH", 3, "Stärke I"),
            new Perk("SPEED", 5, "Tempo I"),
            new Perk("LIFESTEAL", 7, "Lebensraub"),
            new Perk("REAPER", 10, "Seelenschnitter")
    );

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createSoulEaterScythe());
        recipe.shape(" Z ", "HPH", "DBD");
        recipe.setIngredient('Z', MagmaShard.ITEM);
        recipe.setIngredient('H', SoulHeartCrystal.createSoulHeartCrystal());
        recipe.setIngredient('P', MATERIAL);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', Material.BLAZE_ROD);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack createSoulEaterScythe() {
        return createSoulEaterScythe(0);
    }

    public static ItemStack createSoulEaterScythe(int killCount) {
        ItemStack item = ItemUtils.createCustomItem(MATERIAL, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = item.getItemMeta();

        for (var entry : ATTRIBUTE_MODIFIERS.entrySet()) {
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Enchantment, Integer> entry : ENCHANTMENTS.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        meta.lore(buildSoulEaterLore(killCount));
        meta.setCustomModelData(69);
        meta.getPersistentDataContainer().set(KILLS_COUNT, PersistentDataType.INTEGER, killCount);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }


    public static List<Component> buildSoulEaterLore(int killCount) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = new ArrayList<>();

        double attackDamage = (ATTRIBUTE_MODIFIERS == null
                ? Stream.<AttributeModifier>empty()
                : Stream.ofNullable(ATTRIBUTE_MODIFIERS.get(Attribute.ATTACK_DAMAGE)))
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7§oKills mit dieser Scythe schalten Perks frei und erhöhen den §5Soul Counter"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Schaden: §4" + attackDamage));
        lore.add(serializer.deserialize("§f"));

        Integer nextThreshold = null;
        for (Perk p : SOUL_EATER_PERKS) {
            if (killCount < p.threshold) {
                nextThreshold = p.threshold;
                break;
            }
        }
        String progress = (nextThreshold == null) ? "§c(Max)" : "§6(" + killCount + "/" + nextThreshold + ")";
        lore.add(serializer.deserialize("§5Soul Counter: " + progress));
        lore.add(serializer.deserialize("§f"));

        lore.add(serializer.deserialize("§7Perks:"));
        for (Perk p : SOUL_EATER_PERKS) {
            boolean unlocked = killCount >= p.threshold;
            String color = unlocked ? "§6" : "§8";
            lore.add(serializer.deserialize(
                    "§7➤ " + color + p.display + " §8(" + p.threshold + (p.threshold == 1 ? " Kill" : " Kills") + ")"
            ));
        }

        lore.add(serializer.deserialize("§f"));

        if (!ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        }

        return lore;
    }

    @EventHandler
    public void onPlayerKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack item = killer.getInventory().getItemInMainHand();
        if (!ItemUtils.isCustomItem(item, CustomItem.SOUL_EATER_SCYTHE)) return;

        ItemMeta itemMeta = item.getItemMeta();
        int oldKills = itemMeta.getPersistentDataContainer().getOrDefault(KILLS_COUNT, PersistentDataType.INTEGER, 0);

        itemMeta.getPersistentDataContainer().set(KILLS_COUNT, PersistentDataType.INTEGER, oldKills + 1);
        itemMeta.lore(buildSoulEaterLore(oldKills + 1));
        item.setItemMeta(itemMeta);
    }

    public static int getKillCount(ItemStack soulHeartCrystal) {
        if (soulHeartCrystal == null || !soulHeartCrystal.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = soulHeartCrystal.getItemMeta();
        if (meta == null) {
            return 0;
        }

        return meta.getPersistentDataContainer().getOrDefault(KILLS_COUNT, PersistentDataType.INTEGER, 0);
    }
}
