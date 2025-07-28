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

    private static final String ITEM_NAME = "§6Soul Heart Crystal";
    private static final double maxAllowedHealth = 40;
    private static final NamespacedKey namespacedKey = new NamespacedKey(FlareonEvents.getPlugin(), "soul_heart_crystal");

    public static ItemStack createSoulHeartCrystal() {
        return createSoulHeartCrystal(Component.text("ITEM CUSTOM CREATED"));
    }
    public static ItemStack createSoulHeartCrystal(Component droppedByPlayer) {

        ItemStack soulHeartCrystal = ItemUtils.createCustomItem(Material.NETHER_STAR, ITEM_NAME, namespacedKey);
        ItemMeta meta = soulHeartCrystal.getItemMeta();

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Ein seltener Kristall, geboren aus verlorenen Seelen."));
        lore.add(serializer.deserialize("§7Rechtsklick, um dauerhaft ein zusätzliches §c❤ §7zu erhalten."));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7➤ §aEinmalig benutzbar"));
        lore.add(serializer.deserialize("§7➤ §cMaximal " + (int) maxAllowedHealth / 2 + " Herzen möglich"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§8Geborgen von: §7").append(droppedByPlayer));
        lore.add(serializer.deserialize("§f"));

        meta.lore(lore);
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
        if (meta == null || !meta.getPersistentDataContainer().has(SoulHeartCrystal.getNamespacedKey(), PersistentDataType.BYTE)) return;


        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        double currentMaxHealth = attr.getBaseValue();
        if (currentMaxHealth >= maxAllowedHealth) {
            player.sendMessage(Component.text("§cDu kannst nicht mehr als 20 Herzen haben!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        double newHealth = Math.min(currentMaxHealth + 2, maxAllowedHealth);
        attr.setBaseValue(newHealth);
        player.setHealthScale(newHealth);
        player.sendMessage(Component.text("§aDu spürst neue Lebenskraft durchströmen dich... +1 Herz!", NamedTextColor.GREEN));

        item.setAmount(item.getAmount() - 1);
        player.getInventory().setItemInMainHand(item.getAmount() > 0 ? item : null);

        event.setCancelled(true);
    }


    public static String getItemName() {
        return ITEM_NAME;
    }

    public static NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }
}
