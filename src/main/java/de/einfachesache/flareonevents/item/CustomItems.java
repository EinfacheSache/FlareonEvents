package de.einfachesache.flareonevents.item;

import de.einfachesache.flareonevents.item.ingredient.*;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.tool.*;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum CustomItems {

    FIRE_SWORD(FireSword.DISPLAY_NAME, FireSword.getFireSwordRecipe().getKey()),
    NYX_BOW(NyxBow.DISPLAY_NAME, NyxBow.getNyxBowRecipe().getKey()),
    POSEIDONS_TRIDENT(PoseidonsTrident.DISPLAY_NAME, PoseidonsTrident.getPoseidonsTridentRecipe().getKey()),

    REINFORCED_PICKAXE(ReinforcedPickaxe.DISPLAY_NAME, ReinforcedPickaxe.getReinforcedPickaxeRecipe().getKey()),
    BETTER_REINFORCED_PICKAXE(BetterReinforcedPickaxe.DISPLAY_NAME, BetterReinforcedPickaxe.getBetterReinforcedPickaxeRecipe().getKey()),

    GOLD_SHARD(GoldShard.DISPLAY_NAME, GoldShard.getGoldShardRecipe().getKey()),
    MAGMA_SHARD(MagmaShard.DISPLAY_NAME, MagmaShard.getMagmaShardRecipe().getKey()),
    REINFORCED_STICK(ReinforcedStick.DISPLAY_NAME, ReinforcedStick.getReinforcedStickRecipe().getKey()),
    TRIDENT_SPIKES(TridentSpikes.DISPLAY_NAME, TridentSpikes.getTridentSpikesRecipe().getKey()),
    TRIDENT_STICK(TridentStick.DISPLAY_NAME, TridentStick.getTridentStickRecipe().getKey()),

    SOUL_HEART_CRYSTAL(SoulHeartCrystal.DISPLAY_NAME, SoulHeartCrystal.NAMESPACED_KEY),

    EVENT_INFO_BOOK(EventInfoBook.DISPLAY_NAME, EventInfoBook.NAMESPACED_KEY);

    private final String displayName;
    private final NamespacedKey key;

    CustomItems(String displayName, NamespacedKey key) {
        this.displayName = displayName;
        this.key = key;
    }

    public boolean matches(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public void applyName(ItemMeta meta) {
        meta.displayName(Component.text(displayName));

    }
}
