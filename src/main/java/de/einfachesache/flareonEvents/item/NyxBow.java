package de.einfachesache.flareonEvents.item;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ingredient.MagmaShard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"deprecation"})
public class NyxBow implements Listener {

    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "nyx_bow");
    private static final String ITEM_NAME = "§4§lNyx Bow";

    public static ShapedRecipe getNyxBowRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, createNyxBow());
        recipe.shape("SWS", "EBE", "OIO");
        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('B', Material.BOW);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('I', MagmaShard.getItem());
        return recipe;
    }

    public static boolean isNyxBowItem(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ITEM_NAME.equalsIgnoreCase(item.getItemMeta().getDisplayName());
    }

    public static boolean hasNyxBow(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            if (isNyxBowItem(item)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack createNyxBow() {
        ItemStack item = ItemUtils.createCustomItem(Material.BOW, ITEM_NAME, namespacedKey);

        ItemMeta meta = item.getItemMeta();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = Stream.of(
                "§f",
                "§e50%§7 Chance: §8Wither§7, §8Slowness§7 & §8Blindness§7 für §e3s",
                "§f",
                "§7§oBesonderheit: §bSpeed II§7 in Hand, §bSpeed I§7 im Inventar",
                "§f",
                "§7Enchantment: §4Power II",
                "§f"
        ).map(serializer::deserialize).collect(Collectors.toList());

        meta.lore(lore);
        meta.addEnchant(Enchantment.POWER, 2, true);
        item.setItemMeta(meta);

        item.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        return item;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getBow() != null && event.getBow().hasItemMeta())) return;
        if(!isNyxBowItem(event.getBow())) return;

        Arrow arrow = (Arrow) event.getProjectile();

        arrow.getPersistentDataContainer().set(namespacedKey, PersistentDataType.BYTE, (byte) 1);

        if(arrow.getShooter() instanceof Player player){
            player.playSound(
                    event.getEntity().getLocation(),
                    Sound.ENTITY_WITHER_SHOOT,
                    SoundCategory.MASTER,
                    1.0f,
                    1.0f
            );
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!arrow.getPersistentDataContainer().has(namespacedKey, PersistentDataType.BYTE)) return;

        if (Math.random() < 0.5) {
            Player hitPlayer = (Player) event.getEntity();

            hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 3, 0));
            hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 3, 0));
            hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 0));

            shooter.sendMessage(Component.text("§5" + event.getEntity().getName() + " hat §4Nyx Bow §5Effekt erhalten!"));
        }
    }

    public static String getItemName() {
        return ITEM_NAME;
    }
}
