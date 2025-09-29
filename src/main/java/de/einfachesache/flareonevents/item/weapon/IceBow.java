package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.ingredient.IceCrystal;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.util.ItemUtils;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("deprecation")
public class IceBow implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static double FREEZE_CHANCE;
    public static double CRIT_FREEZE_CHANCE;
    public static int FREEZE_TIME;
    public static int DARKNESS_TIME;
    public static int SHOOT_COOLDOWN;
    public static int DASH_COOLDOWN;
    public static double DASH_STRENGTH;
    public static double DASH_LIFT;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    private final Set<UUID> noFallNextLanding = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> dashCooldownMap = new HashMap<>();
    private static final Map<UUID, Long> shootCooldownMap = new HashMap<>();
    private static final Map<UUID, Long> preparedCooldownMap = new HashMap<>();
    private static final Map<UUID, BukkitTask> freezeTimers = new ConcurrentHashMap<>();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, create());
        recipe.shape(" E ", "SBS", "HIH");
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('B', Material.BOW);
        recipe.setIngredient('H', SoulHeartCrystal.create());
        recipe.setIngredient('I', IceCrystal.ITEM);

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
        lore.add(serializer.deserialize("§7Besonderheit:"));
        lore.add(serializer.deserialize("§7➤ Bei Treffer: §bFriert §7das Ziel ein"));
        lore.add(serializer.deserialize("§7➤ §e" + (int) (FREEZE_CHANCE * 100) + "% §7Chance: §bFrostschock"));
        lore.add(serializer.deserialize("§7➤ §e" + (int) (CRIT_FREEZE_CHANCE * 100) + "% §7Chance: §bKritischer Frostschock §7& §8Darkness"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Fähigkeit:"));
        lore.add(serializer.deserialize("§7➤ §oLinksklick§7: §6Dash §7nach vorn (ohne Fallschaden) §8— §7Abklingzeit: §e" + DASH_COOLDOWN + "s"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Schuss:"));
        lore.add(serializer.deserialize("§7➤ Schuss-Abklingzeit: §e" + SHOOT_COOLDOWN + "ms"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Effekte:"));
        lore.add(serializer.deserialize("§bSpeed I §7wenn in Main-Hand"));
        lore.add(serializer.deserialize("§f"));
        lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        lore.add(serializer.deserialize("§f"));

        meta.lore(lore);
        meta.setCustomModelData(69);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(e.getEntity() instanceof Player p)) return;

        if (noFallNextLanding.remove(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getBow() != null && event.getBow().hasItemMeta())) return;
        if (!ItemUtils.isCustomItem(event.getBow(), CustomItem.ICE_BOW)) return;

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
    public void onDash(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        if (!ItemUtils.isCustomItem(event.getItem(), CustomItem.ICE_BOW)) return;
        if (event.getPlayer().getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) return;

        Player player = event.getPlayer();

        long now = System.currentTimeMillis();
        int cooldown = (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) ? 0 : DASH_COOLDOWN;
        long lastUse = dashCooldownMap.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUse < cooldown * 1000L) {
            long remaining = ((cooldown * 1000L) - (now - lastUse));
            player.sendMessage("§cBitte warte noch " + remaining / 1000 + "s, bevor du erneut Dashed!");
            return;
        }

        Vector dir = player.getLocation().getDirection().normalize();
        Vector dash = dir.multiply(DASH_STRENGTH);
        dash.setY(Math.max(dash.getY(), DASH_LIFT));

        player.setVelocity(dash);
        noFallNextLanding.add(player.getUniqueId());

        dashCooldownMap.put(player.getUniqueId(), now);

        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1f, 1.2f);
        player.spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 8, 0.3, 0.2, 0.3, 0.01);
    }


    @EventHandler
    public void onBowShoot(PlayerInteractEvent event) {
        if (!ItemUtils.isCustomItem(event.getItem(), CustomItem.ICE_BOW)) return;
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
        if (!ItemUtils.isCustomItem(shooter.getInventory().getItemInMainHand(), CustomItem.ICE_BOW)) return;

        arrow.setColor(Color.AQUA);
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
        if (livingEntity instanceof Player player && player.getGameMode() == GameMode.CREATIVE) return;

        double roll = Math.random();
        boolean applyFreeze = roll < FREEZE_CHANCE;
        boolean applyCritFreeze = roll < CRIT_FREEZE_CHANCE;

        if (!applyFreeze) {
            livingEntity.setFreezeTicks(livingEntity.getMaxFreezeTicks() - 70);
            return;
        }

        if (!applyCritFreeze) {
            livingEntity.setFreezeTicks(livingEntity.getMaxFreezeTicks() - 1);
        } else {
            livingEntity.setFreezeTicks(livingEntity.getMaxFreezeTicks());
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 20 * DARKNESS_TIME, 0));
        }

        livingEntity.lockFreezeTicks(true);
        removeFreezeTimer(livingEntity);

        String targetName = livingEntity.getName();
        Component prefix = Component.text("✦ ", NamedTextColor.DARK_PURPLE)
                .append(Component.text(IceBow.DISPLAY_NAME))
                .append(Component.text(": ", NamedTextColor.GRAY));

        Component msg;
        if (applyCritFreeze) {
            msg = prefix
                    .append(Component.text(targetName, NamedTextColor.GOLD))
                    .append(Component.text(" erleidet ", NamedTextColor.GRAY))
                    .append(Component.text("kritischen Frostschock", NamedTextColor.AQUA, TextDecoration.BOLD))
                    .append(Component.text(" & ", NamedTextColor.GRAY))
                    .append(Component.text("Darkness", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                    .append(Component.text(".", NamedTextColor.GRAY));
        } else {
            msg = prefix
                    .append(Component.text(targetName, NamedTextColor.GOLD))
                    .append(Component.text(" erleidet ", NamedTextColor.GRAY))
                    .append(Component.text("Frostschock", NamedTextColor.AQUA, TextDecoration.BOLD))
                    .append(Component.text(".", NamedTextColor.GRAY));
        }

        shooter.sendMessage(msg);
    }

    private void removeFreezeTimer(LivingEntity livingEntity) {
        UUID uuid = livingEntity.getUniqueId();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(FlareonEvents.getPlugin(), () -> {
            if (!livingEntity.isValid() || livingEntity.isDead()) return;
            livingEntity.lockFreezeTicks(false);
            freezeTimers.remove(uuid);
        }, 20L * FREEZE_TIME);

        BukkitTask old = freezeTimers.remove(uuid);
        if (old != null) {
            old.cancel();
        }
        freezeTimers.put(uuid, task);
    }
}