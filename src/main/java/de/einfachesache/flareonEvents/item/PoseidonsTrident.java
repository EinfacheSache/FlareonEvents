package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ingredient.TridentSpikes;
import de.einfachesache.flareonEvents.item.ingredient.TridentStick;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"removal", "deprecation"})
public class PoseidonsTrident implements Listener {

    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "poseidons_trident");
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final String ITEM_NAME = "§6§lPoseidon's Trident";

    public static ShapedRecipe getPoseidonsTridentRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, createPoseidonsTrident());
        recipe.shape(" AB", " CA", "C  ");
        recipe.setIngredient('A', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', TridentSpikes.getItem());
        recipe.setIngredient('C', TridentStick.getItem());
        return recipe;
    }

    public static boolean isPoseidonsTridentItem(ItemStack item) {
        if (item == null || item.getType() != Material.TRIDENT) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ITEM_NAME.equals(item.getItemMeta().getDisplayName());
    }

    public static ItemStack createPoseidonsTrident() {
        ItemStack trident = ItemUtils.createCustomItem(Material.TRIDENT, ITEM_NAME, namespacedKey);
        ItemMeta meta = trident.getItemMeta();

        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 9.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(),"generic.attack_speed",1.6-4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        double attackDamage = Objects.requireNonNull(meta
                        .getAttributeModifiers())
                .get(Attribute.ATTACK_DAMAGE)
                .stream()
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = Stream.of(
                "§7",
                "§7§e100%§7 Chance: §cBlitz §7beim Wurf",
                "§7§e25%§7 Chance: §cBlitz §7bei Nahkampftreffer",
                "§7",
                "§7§oBesonderheit: §bDolphin's Grace §7& §bWater Breathing §7in Hand",
                "§7",
                "§7Angriff: §4+" + attackDamage + " Schaden",
                "§7",
                "§7Enchantment: §4Loyalty III",
                "§7",
                "§7Cooldown: §e15s",
                "§7"
        ).map(serializer::deserialize).collect(Collectors.toList());

        meta.lore(lore);
        meta.addEnchant(Enchantment.LOYALTY, 3, true);

        trident.setItemMeta(meta);
        trident.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        return trident;
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isPoseidonsTridentItem(item)) return;

        if (ThreadLocalRandom.current().nextDouble() < 0.25) {
            player.getWorld().strikeLightning(player.getLocation()).getPersistentDataContainer().set(
                    new NamespacedKey(FlareonEvents.getPlugin(), "trident_lightning_" + player.getName().toLowerCase()), PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler
    public void onTridentThrow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isPoseidonsTridentItem(item)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUse < 15_000) {
            long remaining = (15 - ((now - lastUse)) / 1000);
            player.sendMessage("§cBitte warte noch " + remaining + "s, bevor du erneut wirfst!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!isPoseidonsTridentItem(trident.getItemStack())) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;

        trident.setVisualFire(true);

        if(shooter.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        long now = System.currentTimeMillis();
        cooldowns.put(shooter.getUniqueId(), now);
        shooter.setCooldown(trident.getItemStack(), 15 * 20);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        ItemStack item = trident.getItemStack();
        if (!isPoseidonsTridentItem(item)) return;

        LightningStrike lightningStrike = trident.getWorld().strikeLightning(trident.getLocation());
        lightningStrike.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), "trident_lightning_" + player.getName().toLowerCase()), PersistentDataType.BYTE, (byte) 1);
    }

    public static String getItemName() {
        return ITEM_NAME;
    }
}
