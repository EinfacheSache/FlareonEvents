package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ingredient.GoldShard;
import de.einfachesache.flareonEvents.item.ingredient.MagmaShard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"removal", "deprecation"})
public class FireSword implements Listener {

    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "fire_sword");
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final String ITEM_NAME = "§c§lFire Sword";

    public static ShapedRecipe getFireSwordRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, createFireSword());
        recipe.shape(" Z ", "APA", "DBD");
        recipe.setIngredient('Z', MagmaShard.getItem());
        recipe.setIngredient('A', Material.NETHERITE_SCRAP);
        recipe.setIngredient('P', GoldShard.getItem());
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', Material.BLAZE_ROD);
        return recipe;
    }


    public static boolean isFireSwordItem(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_SWORD) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ITEM_NAME.equalsIgnoreCase(item.getItemMeta().getDisplayName());
    }


    public static ItemStack createFireSword() {
        ItemStack item = ItemUtils.createCustomItem(Material.GOLDEN_SWORD, ITEM_NAME, namespacedKey);

        ItemMeta meta = item.getItemMeta();

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", 7.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.attack_speed", -2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        double attackDamage = Objects.requireNonNull(meta
                        .getAttributeModifiers())
                        .get(Attribute.ATTACK_DAMAGE)
                        .stream()
                        .mapToDouble(AttributeModifier::getAmount)
                        .sum();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = Stream.of(
                "§f",
                    "§7Right-click: Wirf einen §cFeuerball",
                    "§f",
                    "§e33%§7 Chance, das Ziel §e3s§7 zu entzünden",
                    "§f",
                    "§7Besonderheit: §bStrength §7& §bFire Resistance §7in Hand",
                    "§f",
                    "§7Angriff: §4+" + attackDamage + " Schaden",
                    "§f",
                    "§7§oCooldown: §e15s",
                    "§f"
        ).map(serializer::deserialize).collect(Collectors.toList());

        meta.lore(lore);
        item.setItemMeta(meta);

        item.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        return item;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getItem() == null) {
           return;
        }

        if (!isFireSwordItem(player.getInventory().getItemInMainHand())) return;

        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastUse < 15_000) {
            player.sendMessage(ChatColor.RED + "Du kannst diese Fähigkeit in " + (15 - ((System.currentTimeMillis() - lastUse)/1000) + "s erneut verwenden!"));
            return;
        }

        event.getPlayer().setCooldown(event.getItem(), 15 * 20);

        player.launchProjectile(Fireball.class).setShooter(player);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) return;
        if (!(fireball.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof LivingEntity target)) return;
        if (!isFireSwordItem(shooter.getInventory().getItemInMainHand())) return;

        if (Math.random() < 0.33) {
            target.setFireTicks(60);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isFireSwordItem(player.getInventory().getItemInMainHand())) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        if (Math.random() < 0.33) {
            target.setFireTicks(60);
        }
    }

    public static String getItemName() {
        return ITEM_NAME;
    }
}
