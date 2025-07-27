package de.einfachesache.flareonEvents.item;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.io.File;

public class WorldUtils {

    public static boolean isPlayerInCave(Player player) {
        Location eyeLoc = player.getEyeLocation();
        World world = player.getWorld();

        if (eyeLoc.getY() > 100) {
            return false;
        }

        double maxDistance = world.getMaxHeight() - eyeLoc.getY();
        RayTraceResult hit = world.rayTraceBlocks(
                eyeLoc,
                new Vector(0, 1, 0),
                maxDistance,
                FluidCollisionMode.NEVER,
                true
        );

        if (hit == null) {
            return false;
        }
        Block ceilingBlock = hit.getHitBlock();
        if (ceilingBlock == null) {
            return false;
        }

        return isNaturalCeiling(ceilingBlock.getType());
    }

    public static boolean isOre(Material material) {
        return switch (material) {
            case COAL_ORE,
                 IRON_ORE,
                 GOLD_ORE,
                 COPPER_ORE,
                 REDSTONE_ORE,
                 DIAMOND_ORE,
                 EMERALD_ORE,
                 LAPIS_ORE,
                 DEEPSLATE_COAL_ORE,
                 DEEPSLATE_IRON_ORE,
                 DEEPSLATE_GOLD_ORE,
                 DEEPSLATE_COPPER_ORE,
                 DEEPSLATE_REDSTONE_ORE,
                 DEEPSLATE_DIAMOND_ORE,
                 DEEPSLATE_EMERALD_ORE,
                 DEEPSLATE_LAPIS_ORE,
                 NETHER_GOLD_ORE -> true;
            default -> false;
        };
    }

    public static boolean isNaturalCeiling(Material material) {
        return switch (material) {
            // Stone, dirt and variants
            case STONE,
                 DIRT,
                 COARSE_DIRT,
                 ANDESITE,
                 POLISHED_ANDESITE,
                 DIORITE,
                 POLISHED_DIORITE,
                 GRANITE,
                 POLISHED_GRANITE,
                 DEEPSLATE,
                 COBBLED_DEEPSLATE,
                 DEEPSLATE_BRICKS,
                 DEEPSLATE_TILES,
                 CALCITE,
                 TUFF,
                 DRIPSTONE_BLOCK,
                 COBBLESTONE,
                 MOSSY_COBBLESTONE,
                 GRAVEL,
                 // Standard ores
                 COAL_ORE,
                 IRON_ORE,
                 GOLD_ORE,
                 DIAMOND_ORE,
                 REDSTONE_ORE,
                 COPPER_ORE,
                 EMERALD_ORE,
                 LAPIS_ORE,
                 // Deepslate ore variants
                 DEEPSLATE_COAL_ORE,
                 DEEPSLATE_IRON_ORE,
                 DEEPSLATE_GOLD_ORE,
                 DEEPSLATE_DIAMOND_ORE,
                 DEEPSLATE_REDSTONE_ORE,
                 DEEPSLATE_COPPER_ORE,
                 DEEPSLATE_EMERALD_ORE,
                 DEEPSLATE_LAPIS_ORE,
                 // Terracotta variants
                 TERRACOTTA,
                 WHITE_TERRACOTTA,
                 ORANGE_TERRACOTTA,
                 MAGENTA_TERRACOTTA,
                 LIGHT_BLUE_TERRACOTTA,
                 YELLOW_TERRACOTTA,
                 LIME_TERRACOTTA,
                 PINK_TERRACOTTA,
                 GRAY_TERRACOTTA,
                 LIGHT_GRAY_TERRACOTTA,
                 CYAN_TERRACOTTA,
                 PURPLE_TERRACOTTA,
                 BLUE_TERRACOTTA,
                 BROWN_TERRACOTTA,
                 GREEN_TERRACOTTA,
                 RED_TERRACOTTA,
                 BLACK_TERRACOTTA,
                 // Sandstone variants
                 SAND,
                 SANDSTONE,
                 SMOOTH_SANDSTONE,
                 CUT_SANDSTONE,
                 CHISELED_SANDSTONE,
                 RED_SAND,
                 RED_SANDSTONE,
                 SMOOTH_RED_SANDSTONE,
                 CUT_RED_SANDSTONE,
                 CHISELED_RED_SANDSTONE -> true;
            default -> false;
        };
    }

    public static boolean isWorldGeneratedFresh() {
        File levelDat = new File(Bukkit.getWorldContainer() + "/world", "level.dat");
        return !levelDat.exists();
    }
}
