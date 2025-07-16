package de.einfachesache.flareonEvents.item.tool;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
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
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@SuppressWarnings({"deprecation"})
public class PoseidonsTrident implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static double THROW_LIGHTNING_CHANCE;
    public static double ON_MELEE_LIGHTNING_CHANCE;
    public static int COOLDOWN;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    private static final Map<UUID, Long> cooldownMap = new HashMap<>();

    public static ShapedRecipe getPoseidonsTridentRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createPoseidonsTrident());
        recipe.shape(" AB", " CA", "C  ");
        recipe.setIngredient('A', Material.DIAMOND_BLOCK);
        recipe.setIngredient('B', TridentSpikes.getItem());
        recipe.setIngredient('C', TridentStick.getItem());

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static boolean isPoseidonsTridentItem(ItemStack item) {
        if (item == null || item.getType() != MATERIAL) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
    }

    public static ItemStack createPoseidonsTrident() {
        ItemStack trident = ItemUtils.createCustomItem(Material.TRIDENT, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = trident.getItemMeta();

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

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§7"));
        lore.add(serializer.deserialize("§7§e" + (int) (THROW_LIGHTNING_CHANCE * 100) + "%§7 Chance: §cBlitz §7beim Wurf"));
        lore.add(serializer.deserialize("§7§e" + (int) (ON_MELEE_LIGHTNING_CHANCE * 100) + "%§7 Chance: §cBlitz §7bei Nahkampftreffer"));
        lore.add(serializer.deserialize("§7"));
        lore.add(serializer.deserialize("§7§oBesonderheit: §bDolphin's Grace §7& §bWater Breathing §7in Hand"));
        lore.add(serializer.deserialize("§7"));
        lore.add(serializer.deserialize("§7Angriff: §4+" + attackDamage + " Schaden"));
        lore.add(serializer.deserialize("§7"));

        if (!ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        }

        lore.add(serializer.deserialize("§7Cooldown: §e" + COOLDOWN + "s"));
        lore.add(serializer.deserialize("§7"));

        meta.lore(lore);
        meta.setCustomModelData(1);

        trident.setItemMeta(meta);
        trident.addItemFlags(ITEM_FLAGS);

        return trident;
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isPoseidonsTridentItem(item)) return;

        if (Math.random() < ON_MELEE_LIGHTNING_CHANCE) {
            player.getWorld().strikeLightning(event.getEntity().getLocation()).getPersistentDataContainer().set(
                    new NamespacedKey(FlareonEvents.getPlugin(), "trident_lightning_" + player.getName().toLowerCase()), PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler
    public void onTridentThrow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isPoseidonsTridentItem(item)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY) return;

        long now = System.currentTimeMillis();
        long lastUse = cooldownMap.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUse < COOLDOWN * 1000L) {
            long remaining = (COOLDOWN - ((now - lastUse)) / 1000);
            player.sendMessage("§cBitte warte noch " + remaining + "s, bevor du erneut wirfst!");
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!isPoseidonsTridentItem(trident.getItemStack())) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;

        trident.setVisualFire(true);

        if (shooter.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        long now = System.currentTimeMillis();
        cooldownMap.put(shooter.getUniqueId(), now);
        shooter.setCooldown(trident.getItemStack(), COOLDOWN * 20);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        if (!isPoseidonsTridentItem(trident.getItemStack())) return;

        if (Math.random() < THROW_LIGHTNING_CHANCE) {
            LightningStrike lightningStrike = trident.getWorld().strikeLightning(trident.getLocation());
            lightningStrike.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), "trident_lightning_" + player.getName().toLowerCase()), PersistentDataType.BYTE, (byte) 1);
        }
    }

    public static String getItemName() {
        return DISPLAY_NAME;
    }
}
