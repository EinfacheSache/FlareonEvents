package de.einfachesache.flareonevents.item.tool;

import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.MagmaShard;
import net.kyori.adventure.text.Component;
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

import java.util.*;

@SuppressWarnings({"deprecation"})
public class NyxBow implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static double SLOW_BLIND_EFFECT_CHANCE;
    public static double WITHER_EFFECT_CHANCE;
    public static int SLOW_BLIND_EFFECT_TIME;
    public static int WITHER_EFFECT_TIME;
    public static int COOLDOWN;
    public static ItemFlag[] ITEM_FLAGS;
    public static Map<Enchantment, Integer> ENCHANTMENTS;
    public static Map<Attribute, AttributeModifier> ATTRIBUTE_MODIFIERS;

    private static final Map<UUID, Long> cooldownMap = new HashMap<>();
    private static final Map<UUID, Long> preparedCooldownMap = new HashMap<>();

    public static ShapedRecipe getNyxBowRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createNyxBow());
        recipe.shape("SWS", "EBE", "OIO");
        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('B', Material.BOW);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('I', MagmaShard.ITEM);

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static boolean isNyxBowItem(ItemStack item) {
        if (item == null || item.getType() != MATERIAL) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return DISPLAY_NAME.equalsIgnoreCase(item.getItemMeta().getDisplayName());
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

        if (!ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        }

        lore.add(serializer.deserialize("§7Cooldown: §e" + COOLDOWN + "ms"));
        lore.add(serializer.deserialize("§f"));

        meta.lore(lore);
        meta.setCustomModelData(1);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getBow() != null && event.getBow().hasItemMeta())) return;
        if (!isNyxBowItem(event.getBow())) return;

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
    public void onBowShoot(PlayerInteractEvent event) {
        Player shooter = event.getPlayer();

        if (!isNyxBowItem(event.getItem())) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.useItemInHand() == Event.Result.DENY) return;

        long now = System.currentTimeMillis();
        long lastUse = cooldownMap.getOrDefault(shooter.getUniqueId(), 0L);
        if (now - lastUse < COOLDOWN) {
            long remaining = (COOLDOWN - (now - lastUse));
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
        if (!isNyxBowItem(shooter.getInventory().getItemInMainHand())) return;

        arrow.setColor(Color.BLACK);
        arrow.setGlowing(true);

        long preparedCooldown = preparedCooldownMap.get(shooter.getUniqueId());
        int cooldownInMilliSec = Math.max(0,  (int) (COOLDOWN - (System.currentTimeMillis() - preparedCooldown)));
        cooldownMap.put(shooter.getUniqueId(), preparedCooldown);
        shooter.setCooldown(shooter.getInventory().getItemInMainHand(), cooldownInMilliSec / 50);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!arrow.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.BYTE)) return;
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        String message = null;
        double random = Math.random();
        if (random < WITHER_EFFECT_CHANCE) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * WITHER_EFFECT_TIME, 0));
            message = "§5" + livingEntity.getName() + " hat §4§lNyx Bow §5Effekt erhalten!";
        }

        if (random < SLOW_BLIND_EFFECT_CHANCE) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * SLOW_BLIND_EFFECT_TIME, 0));
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * SLOW_BLIND_EFFECT_TIME, 0));
            message = "§5" + livingEntity.getName() + " hat §4§lNyx Bow§6 §kkk§5 Effekt §6§kkk §5erhalten!";
        }

        if(message != null){
            shooter.sendMessage(Component.text(message));
        }
    }
}
