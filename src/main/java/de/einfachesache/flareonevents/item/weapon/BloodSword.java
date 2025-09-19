package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.ingredient.BloodShard;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.util.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

@SuppressWarnings("deprecation")
public class BloodSword implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static int BLOOD_HUNGER_COOLDOWN, BLOOD_HUNGER_DURATION, BLEED_DURATION, BLEED_TICK;

    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    private static final List<UUID> playerInBloodlust = new ArrayList<>();
    private static final Map<UUID, Long> cooldownMap = new HashMap<>();
    private static final Map<UUID, UUID> bloodingCause = new HashMap<>();
    private static final Map<UUID, Long> bloodingLastHitAt = new HashMap<>();
    private static final Map<UUID, Long> bloodingNextTickAt = new HashMap<>();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, create());
        recipe.shape(" D ", "ZDZ", "HBH");
        recipe.setIngredient('Z', BloodShard.ITEM);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('H', SoulHeartCrystal.create());
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

        var modifiers = meta.getAttributeModifiers();
        double attackDamage = ((modifiers == null)
                ? Collections.<AttributeModifier>emptyList()
                : modifiers.get(Attribute.ATTACK_DAMAGE))
                .stream().filter(Objects::nonNull)
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Du verfällst dem §cBluthunger§7 für §e" + BLOOD_HUNGER_DURATION + "s§7 (Rechtsklick)"));
        lore.add(serializer.deserialize("§7Treffer lassen das Ziel §e" + BLEED_DURATION + "s §cbluten§7 und verursachen alle §e" + BLEED_TICK + "s §c0.5❤ schaden§7."));
        lore.add(serializer.deserialize("§7Jeder Blutschaden heilt dich."));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Besonderheit: §bStrength §7& §bFire Resistance §7in Hand"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Schaden: §4" + attackDamage));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7§oCooldown: §e" + BLOOD_HUNGER_COOLDOWN + "s"));
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
        ItemStack item = event.getItem();
        if (!ItemUtils.isCustomItem(item, CustomItem.BLOOD_SWORD)) return;

        int bloodHungerCooldown = player.isOp() ? BLOOD_HUNGER_DURATION : BLOOD_HUNGER_COOLDOWN;
        long remaining = (bloodHungerCooldown - ((System.currentTimeMillis() - cooldownMap.getOrDefault(player.getUniqueId(), 0L))) / 1000);
        if (remaining > 0) {
            player.sendMessage(Component.text("Du kannst Bluthunger in " + remaining + "s erneut verwenden!", NamedTextColor.RED));
            return;
        }

        if (player.hasCooldown(item)) return;

        enterBloodlust(player);
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
        event.getPlayer().setCooldown(item, bloodHungerCooldown * 20);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!ItemUtils.isCustomItem(damager.getInventory().getItemInMainHand(), CustomItem.BLOOD_SWORD)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        if (!playerInBloodlust.contains(damager.getUniqueId())) {
            return;
        }

        long now = System.currentTimeMillis();
        bloodingCause.put(target.getUniqueId(), damager.getUniqueId());
        bloodingLastHitAt.put(target.getUniqueId(), now);
        bloodingNextTickAt.putIfAbsent(target.getUniqueId(), now + BLEED_TICK * 1000L);
    }

    private void enterBloodlust(Player player) {

        playerInBloodlust.add(player.getUniqueId());

        player.setGlowing(true);
        player.sendMessage("§cBluthunger§7 flammt in dir auf – §f" + BLOOD_HUNGER_DURATION + "s §7lang dürstet deine Klinge nach Blut.");
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2f, 1.6f);

        final String teamName = ("blood_" + player.getUniqueId().toString().substring(0, 8)).replace("-", "");
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                Scoreboard sb = viewer.getScoreboard();
                Team t = sb.getTeam(teamName);
                if (t == null) {
                    t = sb.registerNewTeam(teamName);
                    t.setColor(ChatColor.RED);
                    t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                }
                if (!t.hasEntry(player.getName())) t.addEntry(player.getName());
            } catch (IllegalArgumentException ignore) {}
        }

        final long endAt = System.currentTimeMillis() + BLOOD_HUNGER_DURATION * 1000L;
        final int particleTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(FlareonEvents.getPlugin(), () -> {
                    if (!player.isOnline() || System.currentTimeMillis() >= endAt) return;
                    var loc = player.getLocation().add(0, 1.0, 0);
                    player.getWorld().spawnParticle(
                            Particle.DUST,
                            loc,
                            12, 0.55, 0.9, 0.55, 0,
                            new Particle.DustOptions(Color.fromRGB(200, 20, 20), 1.4f)
                    );
                },
                0L, 4L
        );

        Bukkit.getScheduler().runTaskLater(FlareonEvents.getPlugin(), () -> {
            player.setGlowing(false);
            Bukkit.getScheduler().cancelTask(particleTaskId);

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                try {
                    Scoreboard sb = viewer.getScoreboard();
                    Team t = sb.getTeam(teamName);
                    if (t != null) {
                        t.removeEntry(player.getName());
                        if (t.getEntries().isEmpty()) t.unregister();
                    }
                } catch (Exception ignore) {}
            }

            playerInBloodlust.remove(player.getUniqueId());
            player.sendMessage("§7Der §cBluthunger§7 verflacht. Deine Klinge stillt ihren Durst.");
        }, BLOOD_HUNGER_DURATION * 20L);
    }

    public static void startBleedTask() {
        Bukkit.getScheduler().runTaskTimer(FlareonEvents.getPlugin(), () -> {
            if (bloodingLastHitAt.isEmpty()) return;

            final long now = System.currentTimeMillis();

            // Copy, um ConcurrentModification zu vermeiden
            for (UUID targetId : new HashSet<>(bloodingLastHitAt.keySet())) {
                Long last = bloodingLastHitAt.get(targetId);
                if (last == null) continue;

                // Abgelaufen?
                if (now - last >= (BLEED_DURATION + 1) * 1000L) {
                    cleanupBleed(targetId);
                    continue;
                }

                // Nächster Tick fällig?
                long next = bloodingNextTickAt.getOrDefault(targetId, 0L);
                if (now < next) continue;

                // Entitäten auflösen
                var entity = Bukkit.getEntity(targetId);
                if (!(entity instanceof LivingEntity victim) || victim.isDead()) {
                    cleanupBleed(targetId);
                    continue;
                }
                UUID damagerId = bloodingCause.get(targetId);
                Player damager = damagerId != null ? Bukkit.getPlayer(damagerId) : null;
                if (damager == null || !damager.isOnline()) {
                    cleanupBleed(targetId);
                    continue;
                }

                // 0.5❤ Schaden (1.0 HP) an Opfer, Quelle = Angreifer (gut für Combat-Log)
                victim.damage(1.0);

                // Heal den Angreifer um 0.5❤ (clampen)
                var attr = damager.getAttribute(Attribute.MAX_HEALTH);
                double max = attr != null ? attr.getValue() : 20.0;
                damager.setHealth(Math.min(max, damager.getHealth() + 1.0));
                damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.0f);

                // Optional: kleiner Blut-Partikelimpuls am Opfer (sehr sparsam)
                victim.getWorld().spawnParticle(
                        Particle.DUST, victim.getLocation().add(0, 1.0, 0),
                        12, 0.45, 0.6, 0.45, 0,
                        new Particle.DustOptions(Color.fromRGB(170, 10, 10), 2.5f)
                );

                // nächsten Tick planen
                bloodingNextTickAt.put(targetId, now + BLEED_TICK * 1000L);
            }
        }, 20L, 5L); // start nach 1s, dann alle 0.25s prüfen
    }

    private static void cleanupBleed(UUID targetId) {
        bloodingCause.remove(targetId);
        bloodingLastHitAt.remove(targetId);
        bloodingNextTickAt.remove(targetId);
    }
}
