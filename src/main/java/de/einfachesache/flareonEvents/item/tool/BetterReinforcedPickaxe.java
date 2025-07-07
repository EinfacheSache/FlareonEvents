package de.einfachesache.flareonEvents.item.tool;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import de.einfachesache.flareonEvents.item.WorldUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings({"deprecation"})
public class BetterReinforcedPickaxe implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static int XRAY_ENABLED_TIME, XRAY_RADIUS, XRAY_COOLDOWN;
    public static ItemFlag[] ITEM_FLAGS;
    private static final Map<UUID, Long> cooldownMap = new HashMap<>();

    public static ShapedRecipe getBetterReinforcedPickaxeRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createBetterReinforcedPickaxe());
        recipe.shape("EEE", "EPE", "EEE");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(ReinforcedPickaxe.createReinforcedPickaxe()));

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static boolean isBetterReinforcedPickaxeItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return DISPLAY_NAME.equalsIgnoreCase(item.getItemMeta().getDisplayName());
    }

    public static ItemStack createBetterReinforcedPickaxe() {
        ItemStack item = ReinforcedPickaxe.createReinforcedPickaxe();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        ItemMeta meta = item.getItemMeta();

        // Persistent Data & Material
        meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().remove(ReinforcedPickaxe.getReinforcedPickaxeRecipe().getKey());
        meta.displayName(Component.text(DISPLAY_NAME));

        // Lore zusammenbauen
        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Beim §bAbbauen §7von §6Erzen §7können"));
        lore.add(serializer.deserialize("§7zufällig andere §6Erze §7droppen:"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7➤ §e3%§7\u2009\u2009§cNetherite Scrap"));
        lore.add(serializer.deserialize("§7➤ §e10%§7 §bDiamant"));
        lore.add(serializer.deserialize("§7➤ §e20%§7 §6Gold"));
        lore.add(serializer.deserialize("§7➤ §e25%§7 §fEisen"));
        lore.add(serializer.deserialize("§7➤ §e15%§7 §8Kohle"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Besonderheit: §bNight Vision §7in Höhlen"));
        lore.add(serializer.deserialize("§f"));

        // X-Ray-Info dynamisch aus Konfiguration
        lore.add(serializer.deserialize("§7Right-Click → Für §e" + XRAY_ENABLED_TIME + "s §4X-RAY §7(Radius: §e" + XRAY_RADIUS + " Blöcke§7)"));
        lore.add(serializer.deserialize("§7Cooldown: §e" + XRAY_COOLDOWN + "s"));
        lore.add(serializer.deserialize("§f"));

        // Dynamisch aus ENCHANTMENTS-Map
        if(!ReinforcedPickaxe.ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ReinforcedPickaxe.ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ReinforcedPickaxe.ENCHANTMENTS));
        }

        meta.lore(lore);
        meta.addItemFlags(ITEM_FLAGS);

        item.setItemMeta(meta);

        // Material anpassen und zurückgeben
        return item.withType(MATERIAL);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isBetterReinforcedPickaxeItem(item)) return;

        Material blockType = event.getBlock().getType();

        if (WorldUtils.isNonOre(blockType)) return;

        Material drop = null;
        int roll = FlareonEvents.getRandom().nextInt(100) + 1;
        if(roll <= 10){
            drop = Material.DIAMOND;
        }else if(roll <= 30){
            drop = Material.GOLD_INGOT;
        }else if(roll <= 55){
            drop = Material.IRON_INGOT;
        }else if(roll <= 70){
            drop = Material.COAL;
        }else if(roll <= 73) {
            drop = Material.NETHERITE_SCRAP;
        }

        if (drop != null) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(drop));
        }
    }

    @EventHandler
    public void onPlayerInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isBetterReinforcedPickaxeItem(item)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        long remaining = (XRAY_COOLDOWN - ((System.currentTimeMillis() - cooldownMap.getOrDefault(player.getUniqueId(), 0L))) / 1000);
        if (remaining > 0) {
            player.sendMessage("§cBitte warte noch " + remaining + "s, bevor du §4X-Ray §cerneut benutzt!");
            return;
        }

        if(player.hasCooldown(item)) return;

        xRay(player);

        player.setCooldown(item, XRAY_COOLDOWN * 20);
    }


    private final Map<UUID,Set<Location>> xrayBlocks = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        Set<Location> overlay = xrayBlocks.get(player.getUniqueId());
        if (overlay == null) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        Location loc = clicked.getLocation();

        if (overlay.contains(loc)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendBlockChange(loc, Material.BARRIER.createBlockData());
                }
            }.runTaskLater(FlareonEvents.getPlugin(), 0L);
        }
    }

    public void xRay(Player player) {

        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());

        final int radiusSq = XRAY_RADIUS * XRAY_RADIUS;
        World world = player.getWorld();
        Location center = player.getLocation();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int maxY = world.getMaxHeight();

        Set<Location> fake = new HashSet<>();
        List<Location> locations = new ArrayList<>();

        // Scan und clientseitig ersetzen
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int dx = -XRAY_RADIUS; dx <= XRAY_RADIUS; dx++) {
                    for (int dy = -XRAY_RADIUS; dy <= XRAY_RADIUS; dy++) {
                        for (int dz = -XRAY_RADIUS; dz <= XRAY_RADIUS; dz++) {
                            if (dx * dx + dy * dy + dz * dz > radiusSq) continue;

                            int x = cx + dx;
                            int y = cy + dy;
                            int z = cz + dz;
                            if (y < 0 || y >= maxY) continue;

                            Block b = world.getBlockAt(x, y, z);
                            Material type = b.getType();

                            // Luft, Wasser, Lava und Erze überspringen
                            if (type == Material.AIR || type == Material.WATER  || type == Material.LAVA  ||
                                    !WorldUtils.isNonOre(type) || !WorldUtils.isNaturalCeiling(type)) continue;

                            fake.add(b.getLocation());
                            locations.add(b.getLocation());
                            player.sendBlockChange(
                                    b.getLocation(),
                                    Material.BARRIER.createBlockData()
                            );
                        }
                    }
                }

                xrayBlocks.put(player.getUniqueId(), fake);
                player.sendMessage("§eX-Ray (Radius " + XRAY_RADIUS + ") aktiviert für §6" + XRAY_ENABLED_TIME + "§e Sekunden.");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Location loc : locations) {
                            BlockData current = loc.getWorld().getBlockAt(loc).getBlockData();
                            player.sendBlockChange(loc, current);
                        }
                        xrayBlocks.remove(player.getUniqueId());
                        player.sendMessage("§aX-Ray beendet.");
                    }
                }.runTaskLater(FlareonEvents.getPlugin(), XRAY_ENABLED_TIME * 20L);
            }
        }.runTask(FlareonEvents.getPlugin());

    }

    public static String getItemName() {
        return DISPLAY_NAME;
    }
}
