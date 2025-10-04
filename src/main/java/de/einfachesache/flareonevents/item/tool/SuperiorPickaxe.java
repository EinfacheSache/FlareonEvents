package de.einfachesache.flareonevents.item.tool;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.util.ItemUtils;
import de.einfachesache.flareonevents.util.WorldUtils;
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

    public static ShapedRecipe getShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(NAMESPACED_KEY, create());
        recipe.shape("AAA", "HPH", "AAA");
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(SoulHeartCrystal.create()));
        recipe.setIngredient('P', new RecipeChoice.ExactChoice(ReinforcedPickaxe.create()));

        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        return recipe;
    }

    public static ItemStack create() {
        ItemStack item = ReinforcedPickaxe.create();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().remove(ReinforcedPickaxe.getShapedRecipe().getKey());
        meta.displayName(Component.text(DISPLAY_NAME));

        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Besonderheit:"));
        lore.add(serializer.deserialize("§7Beim §bAbbauen §7von §6Erzen §7können"));
        lore.add(serializer.deserialize("§7zufällig andere §6Erze §7droppen:"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7➤ §e3%§7\u2009\u2009§cNetherite Scrap"));
        lore.add(serializer.deserialize("§7➤ §e10%§7 §bDiamant"));
        lore.add(serializer.deserialize("§7➤ §e20%§7 §6Gold"));
        lore.add(serializer.deserialize("§7➤ §e25%§7 §fEisen"));
        lore.add(serializer.deserialize("§7➤ §e15%§7 §8Kohle"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Fähigkeit:"));
        lore.add(serializer.deserialize("§7Rechtsklick: §e" + XRAY_ENABLED_TIME + "s §cX-RAY §7(Radius: §e" + XRAY_RADIUS + " Blöcke§7) §8— §7Abklingzeit: §e" + XRAY_COOLDOWN + "s"));
        lore.add(serializer.deserialize("§f"));
        lore.add(serializer.deserialize("§7Effekte:"));
        lore.add(serializer.deserialize("§bNight Vision §7in Höhlen"));
        lore.add(serializer.deserialize("§f"));
        lore.addAll(ItemUtils.getEnchantments(ENCHANTMENTS));
        lore.add(serializer.deserialize("§f"));

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
        drops.put(Material.NETHERITE_SCRAP, 3);
        drops.put(Material.DIAMOND, 10);
        drops.put(Material.GOLD_INGOT, 20);
        drops.put(Material.IRON_INGOT, 25);
        drops.put(Material.COAL, 15);

        Material drop = ItemUtils.getRandomDrop(drops);

        if (drop != null) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(drop));
        }
    }

    @EventHandler
    public void onPlayerInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!ItemUtils.isCustomItem(item, CustomItem.SUPERIOR_PICKAXE)) return;
        if (WorldUtils.isUsingBlock(event)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

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

        final int radiusSqPlusOne = (XRAY_RADIUS + 1) * (XRAY_RADIUS + 1);
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

        Bukkit.getScheduler().runTaskAsynchronously(FlareonEvents.getPlugin(), () -> {
            for (int dx = -XRAY_RADIUS; dx <= XRAY_RADIUS; dx++) {
                for (int dy = -XRAY_RADIUS; dy <= XRAY_RADIUS; dy++) {
                    for (int dz = -XRAY_RADIUS; dz <= XRAY_RADIUS; dz++) {
                        int distanceSq = dx * dx + dy * dy + dz * dz;
                        if (distanceSq > radiusSqPlusOne) continue;

                        int x = cx + dx;
                        int y = cy + dy;
                        int z = cz + dz;
                        if (y <= minY || y >= maxY) continue;

                        Block block = world.getBlockAt(x, y, z);
                        Material type = block.getType();

                        // Luft, Wasser, Lava, etc überspringen
                        if (type.isAir() || type == Material.WATER || type == Material.LAVA || type != Material.GRASS_BLOCK && !WorldUtils.isNaturalCeiling(type))
                            continue;

                        BlockData blockData = WorldUtils.isOre(type) || (distanceSq > radiusSq) ? type.createBlockData() : Material.BARRIER.createBlockData();

                        fake.add(block.getLocation());
                        locations.add(block.getLocation());
                        player.sendBlockChange(
                                block.getLocation(),
                                blockData
                        );
                    }
                }
            }

            xrayBlocks.put(player.getUniqueId(), fake);
            player.sendMessage("§4X-Ray §eaktiviert für §6" + XRAY_ENABLED_TIME + "§e Sekunden. §7(Radius " + XRAY_RADIUS + ") ");

            Bukkit.getScheduler().runTaskLaterAsynchronously(FlareonEvents.getPlugin(), () -> {
                for (Location loc : locations) {
                    BlockData current = loc.getWorld().getBlockAt(loc).getBlockData();
                    player.sendBlockChange(loc, current);
                }
                xrayBlocks.remove(player.getUniqueId());
                player.sendMessage(Component.text("§4X-Ray §cbeendet!"));
            }, XRAY_ENABLED_TIME * 20L);
        });
    }
}
