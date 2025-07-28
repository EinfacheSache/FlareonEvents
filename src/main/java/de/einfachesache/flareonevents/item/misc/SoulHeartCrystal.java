package de.einfachesache.flareonevents.item.misc;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SoulHeartCrystal implements Listener {

    public static final String DISPLAY_NAME = "§6Soul Heart Crystal";
    public static final double MAX_ALLOWED_HEALTH = 40;
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(FlareonEvents.getPlugin(), "soul_heart_crystal");
    public static final NamespacedKey DROPPED_BY_PLAYER = new NamespacedKey(FlareonEvents.getPlugin(), "dropped_by_player");

    public static ItemStack createSoulHeartCrystal() {
        return createSoulHeartCrystal("ITEM CUSTOM CREATED");
    }
    public static ItemStack createSoulHeartCrystal(String droppedByPlayer) {

        ItemStack soulHeartCrystal = ItemUtils.createCustomItem(Material.NETHER_STAR, DISPLAY_NAME, NAMESPACED_KEY);
        ItemMeta meta = soulHeartCrystal.getItemMeta();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Ein seltener Kristall, geboren aus verlorenen Seelen."));
        lore.add(serializer.deserialize("§7Rechtsklick, um dauerhaft ein zusätzliches §c❤ §7zu erhalten."));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7➤ §aEinmalig benutzbar"));
        lore.add(serializer.deserialize("§7➤ §cMaximal " + (int) MAX_ALLOWED_HEALTH / 2 + " Herzen möglich"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§8Geborgen von: " + droppedByPlayer));
        lore.add(serializer.deserialize("§f"));

        meta.lore(lore);
        meta.getPersistentDataContainer().set(DROPPED_BY_PLAYER, PersistentDataType.STRING, droppedByPlayer);

        soulHeartCrystal.setItemMeta(meta);

        return soulHeartCrystal;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(SoulHeartCrystal.NAMESPACED_KEY, PersistentDataType.BYTE)) return;


        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        double currentMaxHealth = attr.getBaseValue();
        if (currentMaxHealth >= MAX_ALLOWED_HEALTH) {
            player.sendMessage(Component.text("§cDu kannst nicht mehr als 20 Herzen haben!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        double newHealth = Math.min(currentMaxHealth + 2, MAX_ALLOWED_HEALTH);
        attr.setBaseValue(newHealth);
        player.setHealthScale(newHealth);
        player.sendMessage(Component.text("§aDu spürst neue Lebenskraft durchströmen dich... +1 Herz!", NamedTextColor.GREEN));

        item.setAmount(item.getAmount() - 1);
        player.getInventory().setItemInMainHand(item.getAmount() > 0 ? item : null);

        event.setCancelled(true);
    }

    public static String getDroppedByPlayer(ItemStack soulHeartCrystal) {
        if (soulHeartCrystal == null || !soulHeartCrystal.hasItemMeta()) {
            return "ITEM CUSTOM CREATED";
        }

        ItemMeta meta = soulHeartCrystal.getItemMeta();
        if (meta == null) {
            return "ITEM CUSTOM CREATED";
        }

        String droppedBy = meta.getPersistentDataContainer().get(DROPPED_BY_PLAYER, PersistentDataType.STRING);
        return droppedBy != null ? droppedBy : "ITEM CUSTOM CREATED";
    }
}
