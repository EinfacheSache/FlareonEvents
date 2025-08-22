package de.einfachesache.flareonevents.item.tool;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.WorldUtils;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
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

@SuppressWarnings("deprecation")
public class SuperiorPickaxe implements Listener {

    public static NamespacedKey NAMESPACED_KEY;
    public static Material MATERIAL;
    public static String DISPLAY_NAME;
    public static int XRAY_ENABLED_TIME, XRAY_RADIUS, XRAY_COOLDOWN;
    public static ItemFlag[] ITEM_FLAGS;
    private static final Map<UUID, Long> cooldownMap = new HashMap<>();
    public static Map<Enchantment, Integer> ENCHANTMENTS;

    public static ShapedRecipe getSuperiorPickaxeRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, createSuperiorPickaxe());
        recipe.shape("AAA", "HPH", "AAA");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(SoulHeartCrystal.createSoulHeartCrystal()));
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(ReinforcedPickaxe.createReinforcedPickaxe()));

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack createSuperiorPickaxe() {
        ItemStack item = ReinforcedPickaxe.createReinforcedPickaxe();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().remove(ReinforcedPickaxe.getReinforcedPickaxeRecipe().getKey());
        meta.displayName(Component.text(DISPLAY_NAME));

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

        lore.add(serializer.deserialize("§7Right-Click → Für §e" + XRAY_ENABLED_TIME + "s §4X-RAY §7(Radius: §e" + XRAY_RADIUS + " Blöcke§7)"));
        lore.add(serializer.deserialize("§7Cooldown: §e" + XRAY_COOLDOWN + "s"));
        lore.add(serializer.deserialize("§f"));

        if (!ENCHANTMENTS.isEmpty()) {
            lore.add(serializer.deserialize(("§7Enchantment" + (ENCHANTMENTS.size() > 1 ? "s" : "") + ":")));
            lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        }

        meta.lore(lore);
        meta.setCustomModelData(69);

        item.setItemMeta(meta);
        item.addItemFlags(ITEM_FLAGS);

        return item.withType(MATERIAL);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!ItemUtils.isCustomItem(item, CustomItem.SUPERIOR_PICKAXE)) return;

        Material blockType = event.getBlock().getType();

        if (!WorldUtils.isOre(blockType)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // (Summe = 73, restliche 27% liefern null)
        Map<Material, Integer> drops = new LinkedHashMap<>();
        drops.put(Material.NETHERITE_SCRAP,    3);
        drops.put(Material.DIAMOND,           10);
        drops.put(Material.GOLD_INGOT,        20);
        drops.put(Material.IRON_INGOT,        25);
        drops.put(Material.COAL,              15);

        Material drop = ItemUtils.getRandomDrop(drops);

        if (drop != null) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(drop));
        }
    }

    @EventHandler
    public void onPlayerInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!ItemUtils.isCustomItem(item, CustomItem.SUPERIOR_PICKAXE)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        int playerXRayCooldown = player.getGameMode() == GameMode.CREATIVE ? XRAY_ENABLED_TIME : XRAY_COOLDOWN;
        long remaining = (playerXRayCooldown - ((System.currentTimeMillis() - cooldownMap.getOrDefault(player.getUniqueId(), 0L))) / 1000);
        if (remaining > 0) {
            player.sendMessage("§cBitte warte noch " + remaining + "s, bevor du §4X-Ray §cerneut benutzt!");
            return;
        }

        if (player.hasCooldown(item)) return;

        xRay(player);

        player.setCooldown(item, playerXRayCooldown * 20);
    }


    private final Map<UUID, Set<Location>> xrayBlocks = new HashMap<>();

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
        int minY = world.getMinHeight();

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
                            if (y <= minY || y >= maxY) continue;

                            Block b = world.getBlockAt(x, y, z);
                            Material type = b.getType();

                            // Luft, Wasser, Lava und Erze überspringen
                            if (type.isAir() || type == Material.WATER || type == Material.LAVA || WorldUtils.isOre(type) ||
                                    type != Material.GRASS_BLOCK && !WorldUtils.isNaturalCeiling(type)) continue;

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
}
