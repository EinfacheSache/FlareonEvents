package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.MagmaShard;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;


@SuppressWarnings("deprecation")
public class NyxBow implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static double SLOW_BLIND_EFFECT_CHANCE;
    public static double WITHER_EFFECT_CHANCE;
    public static int SLOW_BLIND_EFFECT_TIME;
    public static int WITHER_EFFECT_TIME;
    public static int SHOOT_COOLDOWN;
    public static int DASH_COOLDOWN;
    public static double DASH_STRENGTH;
    public static double DASH_LIFT;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    private static final Map<UUID, Long> dashCooldownMap = new HashMap<>();
    private static final Map<UUID, Long> shootCooldownMap = new HashMap<>();
    private static final Map<UUID, Long> preparedCooldownMap = new HashMap<>();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createNyxBow());
        recipe.shape("SWS", "EBE", "HIH");
        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('B', Material.BOW);
        recipe.setIngredient('H', SoulHeartCrystal.createSoulHeartCrystal());
        recipe.setIngredient('I', MagmaShard.ITEM);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack createNyxBow() {
        ItemStack item = ItemUtils.createCustomItem(MATERIAL, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = item.getItemMeta();

        for (var entry : ATTRIBUTE_MODIFIERS.entrySet()) {
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Enchantment, Integer> entry : ENCHANTMENTS.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§e" + (int) (WITHER_EFFECT_CHANCE * 100) + "%§7 Chance: §8Wither§7 für §e" + WITHER_EFFECT_TIME + "s"));
        lore.add(serializer.deserialize("§e" + (int) (SLOW_BLIND_EFFECT_CHANCE * 100) + "%§7 Chance: §8Slowness§7 & §8Blindness§7 für §e" + SLOW_BLIND_EFFECT_TIME + "s"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7§oBesonderheit: §bSpeed I§7 in Hand"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Left-Click: §eDash nach vorne"));
        lore.add(serializer.deserialize("§7Cooldown: §e" + DASH_COOLDOWN + "s"));
        lore.add(serializer.deserialize("§f"));

        if (!ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        }

        lore.add(serializer.deserialize("§7Cooldown: §e" + SHOOT_COOLDOWN + "ms"));
        lore.add(serializer.deserialize("§f"));

        meta.lore(lore);
        meta.setCustomModelData(69);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getBow() != null && event.getBow().hasItemMeta())) return;
        if (!ItemUtils.isCustomItem(event.getBow(), CustomItem.NYX_BOW)) return;

        Arrow arrow = (Arrow) event.getProjectile();

        arrow.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.BYTE, (byte) 1);

        if (arrow.getShooter() instanceof Player player) {
            player.playSound(
                    event.getEntity().getLocation(),
                    Sound.ENTITY_WITHER_SHOOT,
                    SoundCategory.PLAYERS,
                    0.75f,
                    1.0f
            );
        }
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        if (!ItemUtils.isCustomItem(event.getItem(), CustomItem.NYX_BOW)) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        long now = System.currentTimeMillis();
        long lastUse = dashCooldownMap.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUse < DASH_COOLDOWN * 1000L) {
            long remaining = ((DASH_COOLDOWN * 1000L) - (now - lastUse));
            player.sendMessage("§cBitte warte noch " + remaining / 1000 + "s, bevor du erneut Dashed!");
            return;
        }

        Vector dir = player.getLocation().getDirection().normalize();
        Vector dash = dir.multiply(DASH_STRENGTH);
        dash.setY(Math.max(dash.getY(), DASH_LIFT));

        player.setVelocity(dash);

        dashCooldownMap.put(player.getUniqueId(), now);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
        player.spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 8, 0.3, 0.2, 0.3, 0.01);
    }


    @EventHandler
    public void onBowShoot(PlayerInteractEvent event) {
        if (!ItemUtils.isCustomItem(event.getItem(), CustomItem.NYX_BOW)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY) return;

        Player shooter = event.getPlayer();

        long now = System.currentTimeMillis();
        long lastUse = shootCooldownMap.getOrDefault(shooter.getUniqueId(), 0L);
        if (now - lastUse < SHOOT_COOLDOWN) {
            long remaining = (SHOOT_COOLDOWN - (now - lastUse));
            shooter.sendMessage("§cBitte warte noch " + remaining + "ms, bevor du erneut schießt!");
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        preparedCooldownMap.put(shooter.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!ItemUtils.isCustomItem(shooter.getInventory().getItemInMainHand(), CustomItem.NYX_BOW)) return;

        arrow.setColor(Color.BLACK);
        arrow.setGlowing(true);

        long preparedCooldown = preparedCooldownMap.get(shooter.getUniqueId());
        int cooldownInMilliSec = Math.max(0, (int) (SHOOT_COOLDOWN - (System.currentTimeMillis() - preparedCooldown)));
        shootCooldownMap.put(shooter.getUniqueId(), preparedCooldown);
        shooter.setCooldown(shooter.getInventory().getItemInMainHand(), cooldownInMilliSec / 50);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!arrow.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.BYTE)) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        double roll = Math.random();
        boolean applyWither = roll < WITHER_EFFECT_CHANCE;
        boolean applySlowBlind = roll < (WITHER_EFFECT_CHANCE * SLOW_BLIND_EFFECT_CHANCE);

        if (!applyWither) return;

        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * WITHER_EFFECT_TIME, 0));
        if (applySlowBlind) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * SLOW_BLIND_EFFECT_TIME, 0));
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * SLOW_BLIND_EFFECT_TIME, 0));
        }

        String targetName = livingEntity.getName();
        Component prefix = Component.text("✦ ", NamedTextColor.DARK_PURPLE)
                .append(Component.text(NyxBow.DISPLAY_NAME))
                .append(Component.text(": ", NamedTextColor.GRAY));

        Component msg;
        if (applySlowBlind) {
            msg = prefix.append(Component.text(targetName, NamedTextColor.GOLD))
                    .append(Component.text(" erleidet ", NamedTextColor.GRAY))
                    .append(Component.text("Wither", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                    .append(Component.text(", ", NamedTextColor.GRAY))
                    .append(Component.text("Slowness", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                    .append(Component.text(" & ", NamedTextColor.GRAY))
                    .append(Component.text("Blindness", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                    .append(Component.text(".", NamedTextColor.GRAY));
        } else {
            msg = prefix.append(Component.text(targetName, NamedTextColor.GOLD))
                    .append(Component.text(" erleidet ", NamedTextColor.GRAY))
                    .append(Component.text("Wither", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                    .append(Component.text(".", NamedTextColor.GRAY));
        }

        shooter.sendMessage(msg);
    }
}