package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.GoldShard;
import de.einfachesache.flareonevents.item.ingredient.MagmaShard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.*;

@SuppressWarnings("deprecation")
public class FireSword implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static double FIRE_TICKS_CHANCE;
    public static int FIRE_TICKS_TIME, COOLDOWN;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    public static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();

    public static ShapedRecipe getFireSwordRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createFireSword());
        recipe.shape(" Z ", "APA", "DBD");
        recipe.setIngredient('Z', MagmaShard.ITEM);
        recipe.setIngredient('A', Material.NETHERITE_SCRAP);
        recipe.setIngredient('P', GoldShard.ITEM);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', Material.BLAZE_ROD);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static boolean isFireSwordItem(ItemStack item) {
        if (item == null || item.getType() != MATERIAL) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return DISPLAY_NAME.equalsIgnoreCase((ItemUtils.legacyString(item.getItemMeta().displayName())));
    }

    public static ItemStack createFireSword() {
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
                .stream()
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Right-click: Wirf einen §cFeuerball"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§e" + (int) (FIRE_TICKS_CHANCE * 100) + "%§7 Chance, das Ziel §e" + FIRE_TICKS_TIME + "s§7 zu entzünden"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Besonderheit: §bStrength §7& §bFire Resistance §7in Hand"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Schaden: §4" + attackDamage));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7§oCooldown: §e" + COOLDOWN + "s"));
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

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (!isFireSwordItem(event.getItem())) return;

        long lastUse = COOLDOWN_MAP.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastUse < COOLDOWN * 1000L) {
            player.sendMessage(NamedTextColor.RED + "Du kannst diese Fähigkeit in " + (COOLDOWN - ((System.currentTimeMillis() - lastUse) / 1000) + "s erneut verwenden!"));
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            event.getPlayer().setCooldown(event.getItem(), COOLDOWN * 20);
            COOLDOWN_MAP.put(player.getUniqueId(), System.currentTimeMillis());
        }

        player.launchProjectile(Fireball.class).setShooter(player);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof LivingEntity target)) return;
        if (!isFireSwordItem(shooter.getInventory().getItemInMainHand())) return;

        target.setFireTicks(FIRE_TICKS_TIME * 20);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isFireSwordItem(player.getInventory().getItemInMainHand())) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        if (Math.random() < FIRE_TICKS_CHANCE) {
            target.setFireTicks(FIRE_TICKS_TIME * 20);
        }
    }
}
