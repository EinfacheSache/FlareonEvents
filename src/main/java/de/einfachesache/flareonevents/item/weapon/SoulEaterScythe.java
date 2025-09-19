package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.ingredient.DemonSoul;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.util.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
            new Perk("DAMAGE", 1, "Schaden +1"),
            new Perk("REGEN", 3, "Regeneration II (3 s) nach Kill"),
            new Perk("WITHER", 5, "15 %: Wither (3 s) bei Treffer"),
            new Perk("SLOWNESS", 7, "15 %: Slowness (3 s) bei Treffer"),
            new Perk("LIFESTEAL", 10, "20 %: +1❤ bei Treffer")
    );

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, create());
        recipe.shape(" S ", "HPH", "DBD");
        recipe.setIngredient('S', DemonSoul.ITEM);
        recipe.setIngredient('H', SoulHeartCrystal.create());
        recipe.setIngredient('P', Material.DIAMOND_AXE);
        recipe.setIngredient('D', Material.COAL_BLOCK);
        recipe.setIngredient('B', Material.BLAZE_ROD);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack create() {
        return create(0);
    }

    public static ItemStack create(int killCount) {
        ItemStack item = ItemUtils.createCustomItem(MATERIAL, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = item.getItemMeta();

        for (var entry : ATTRIBUTE_MODIFIERS.entrySet()) {
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Enchantment, Integer> entry : ENCHANTMENTS.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        meta.setCustomModelData(69);
        meta.lore(buildSoulEaterLore(meta, killCount));
        meta.getPersistentDataContainer().set(KILLS_COUNT, PersistentDataType.INTEGER, killCount);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }


    public static List<Component> buildSoulEaterLore(ItemMeta meta, int killCount) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = new ArrayList<>();

        if (killCount >= 1) {
            addPlusOneDamage(meta);
        }

        double attackDamage = java.util.Optional
                .ofNullable(meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE))
                .orElse(java.util.Collections.emptyList())
                .stream()
                .mapToDouble(org.bukkit.attribute.AttributeModifier::getAmount)
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
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack item = killer.getInventory().getItemInMainHand();
        if (!ItemUtils.isCustomItem(item, CustomItem.SOUL_EATER_SCYTHE)) return;

        ItemMeta itemMeta = item.getItemMeta();
        int oldKillCount = itemMeta.getPersistentDataContainer().getOrDefault(KILLS_COUNT, PersistentDataType.INTEGER, 0);
        int newKillCount = oldKillCount + 1;

        checkForKillPerks(killer, newKillCount);

        itemMeta.getPersistentDataContainer().set(KILLS_COUNT, PersistentDataType.INTEGER, newKillCount);
        itemMeta.lore(buildSoulEaterLore(itemMeta, newKillCount));
        item.setItemMeta(itemMeta);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;

        ItemStack item = damager.getInventory().getItemInMainHand();

        if (!ItemUtils.isCustomItem(item, CustomItem.SOUL_EATER_SCYTHE)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int killCount = item.getItemMeta().getPersistentDataContainer().getOrDefault(KILLS_COUNT, PersistentDataType.INTEGER, 0);

        checkForHitPerks(damager, target, killCount);
    }

    private static void checkForKillPerks(Player damager, int killCount) {
        if (killCount >= 3) {
            damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3, 0));
            damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.0f);
        }
    }

    private static void checkForHitPerks(Player damager, LivingEntity target, int killCount) {
        if (killCount >= 5) {
            if (ThreadLocalRandom.current().nextDouble() < 0.15) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 3, 0));
                damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.0f);
            }
        }

        if (killCount >= 7) {
            if (ThreadLocalRandom.current().nextDouble() < 0.15) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3, 0));
                damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.0f);
            }
        }

        if (killCount >= 10) {
            if (ThreadLocalRandom.current().nextDouble() < 0.2) {
                var attr = damager.getAttribute(Attribute.MAX_HEALTH);
                double max = attr != null ? attr.getValue() : 20.0;
                damager.setHealth(Math.min(max, damager.getHealth() + 2.0));
                damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.0f);
            }
        }
    }

    private static void addPlusOneDamage(ItemMeta meta) {
        NamespacedKey key = new NamespacedKey(FlareonEvents.getPlugin(), "add_1_damage");
        var mods = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);
        if (mods == null || mods.stream().noneMatch(m -> key.equals(m.getKey()) || "add_1_damage".equals(m.getName()))) {
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(key, 1.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
        }
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
