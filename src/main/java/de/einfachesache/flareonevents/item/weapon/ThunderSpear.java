package de.einfachesache.flareonevents.item.weapon;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.util.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.SpearHead;
import de.einfachesache.flareonevents.item.ingredient.SpearStick;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
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

@SuppressWarnings("deprecation")
public class ThunderSpear implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static float THROW_LIGHTNING_CHANCE;
    public static float ON_MELEE_LIGHTNING_CHANCE;
    public static int THROW_COOLDOWN_TICKS;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    private static final Map<UUID, Long> cooldownMap = new HashMap<>();

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, create());
        recipe.shape(" AC", " SA", "S  ");
        recipe.setIngredient('A', Material.DIAMOND);
        recipe.setIngredient('C', SpearHead.ITEM);
        recipe.setIngredient('S', SpearStick.ITEM);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack create() {
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
                .stream().filter(Objects::nonNull)
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Besonderheit:"));
        lore.add(serializer.deserialize("§7➤ §e" + (int) (THROW_LIGHTNING_CHANCE * 100) + "% §7Chance: §cBlitz §7beim Wurf"));
        lore.add(serializer.deserialize("§7➤ §e" + (int) (ON_MELEE_LIGHTNING_CHANCE * 100) + "% §7Chance: §cBlitz §7bei Nahkampftreffer"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Wurf:"));
        lore.add(serializer.deserialize("§7➤ Abklingzeit: §e" + THROW_COOLDOWN_TICKS / 20 + "s"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Effekte:"));
        lore.add(serializer.deserialize("§bDolphin's Grace §7& §bWater Breathing §7wenn in Main-Hand"));
        lore.add(serializer.deserialize("§f"));
        lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Schaden: §c" + attackDamage));
        lore.add(serializer.deserialize("§f"));


        meta.lore(lore);
        meta.setCustomModelData(69);

        trident.setItemMeta(meta);
        trident.addItemFlags(ITEM_FLAGS);

        return trident;
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isCustomItem(item, CustomItem.THUNDER_SPEAR)) return;

        if (Math.random() < ON_MELEE_LIGHTNING_CHANCE) {
            player.getWorld().strikeLightning(event.getEntity().getLocation()).getPersistentDataContainer().set(
                    new NamespacedKey(FlareonEvents.getPlugin(), "lightning_trident_" + player.getName().toLowerCase()), PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler
    public void onTridentThrow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!ItemUtils.isCustomItem(item, CustomItem.THUNDER_SPEAR)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY) return;

        long now = System.currentTimeMillis();
        long lastUse = cooldownMap.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastUse < THROW_COOLDOWN_TICKS * 50L) {
            long remaining_ticks = (THROW_COOLDOWN_TICKS - ((now - lastUse)) / 50);
            player.sendMessage("§cBitte warte noch " + remaining_ticks / 20 + "s, bevor du erneut wirfst!");
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1f, 1f);
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!ItemUtils.isCustomItem(trident.getItemStack(), CustomItem.THUNDER_SPEAR)) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;

        trident.setVisualFire(TriState.TRUE);

        if (shooter.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        long now = System.currentTimeMillis();
        cooldownMap.put(shooter.getUniqueId(), now);
        shooter.setCooldown(trident.getItemStack(), THROW_COOLDOWN_TICKS);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        if (!ItemUtils.isCustomItem(trident.getItemStack(), CustomItem.THUNDER_SPEAR)) return;

        if (Math.random() < THROW_LIGHTNING_CHANCE) {
            LightningStrike lightningStrike = trident.getWorld().strikeLightning(trident.getLocation());
            lightningStrike.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), "lightning_trident_" + player.getName().toLowerCase()), PersistentDataType.BYTE, (byte) 1);
        }
    }
}
