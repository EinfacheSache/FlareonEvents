package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.GoldShard;
import de.einfachesache.flareonevents.item.ingredient.MagmaShard;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("deprecation")
public class FireSword implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static double FIRE_TICKS_CHANCE;
    public static int FIRE_TICKS_TIME, COOLDOWN;

    private static final int RANGE = 20;
    private static final int FIREBALL_COUNT = 5;
    private static final double HEIGHT_ABOVE = 7.5;
    private static final double RING_RADIUS = 2;     // Abstand um das Ziel
    private static final double DOWNWARD_SPEED = 0.5; // Fallgeschwindigkeit

    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    public static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();

    public static ShapedRecipe getFireSwordRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createFireSword());
        recipe.shape(" Z ", "HPH", "DBD");
        recipe.setIngredient('Z', MagmaShard.ITEM);
        recipe.setIngredient('H', SoulHeartCrystal.createSoulHeartCrystal());
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
                .stream().filter(Objects::nonNull)
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
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!isFireSwordItem(event.getItem())) return;

        long now = System.currentTimeMillis();
        long last = COOLDOWN_MAP.getOrDefault(player.getUniqueId(), 0L);
        long diff = now - last;
        if (diff < COOLDOWN * 1000L) {
            long remaining = (COOLDOWN * 1000L - diff + 999) / 1000; // auf volle Sekunde runden
            player.sendMessage(Component.text("Du kannst diese Fähigkeit in " + remaining + "s erneut verwenden!", NamedTextColor.RED));
            return;
        }

        // Ziel ermitteln (Block oder Entity) bis 20 Blöcke
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();

        RayTraceResult hit = world.rayTrace(
                eye,
                dir,
                RANGE,
                FluidCollisionMode.NEVER,
                true,           // passierbare Blöcke ignorieren (Laub etc.)
                0.2,
                e -> e != player
        );
        if (hit == null) {
            player.sendMessage(Component.text("Kein Ziel innerhalb von 20 Blöcken.", NamedTextColor.GRAY));
            return;
        } else {
            hit.getHitPosition();
        }

        Location target = hit.getHitPosition().toLocation(world);

        // 5 Positionen im Kreis um das Ziel (horizontal), alle 6 Blöcke höher
        for (int i = 0; i < FIREBALL_COUNT; i++) {
            double angle = (2 * Math.PI / FIREBALL_COUNT) * i;
            double xOff = Math.cos(angle) * RING_RADIUS;
            double zOff = Math.sin(angle) * RING_RADIUS;

            Location spawnLoc = target.clone().add(xOff, HEIGHT_ABOVE, zOff);

            // Fireball = keine Explosion. Auch kein Feuer:
            Fireball fb = world.spawn(spawnLoc, Fireball.class, f -> {
                f.setIsIncendiary(false);
                f.setDirection(new Vector(0, -1, 0)); // senkrecht nach unten
            });

            // direkte Abwärtsgeschwindigkeit für „gerade runter“
            fb.setVelocity(new Vector(0, -DOWNWARD_SPEED, 0));
        }

        // Cooldown & Feedback
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.getPlayer().setCooldown(event.getItem(), COOLDOWN * 20);
            COOLDOWN_MAP.put(player.getUniqueId(), now);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1f, 1f);
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
