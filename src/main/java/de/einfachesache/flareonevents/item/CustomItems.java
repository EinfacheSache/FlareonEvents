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

    FIRE_SWORD(FireSword.getItemName(), FireSword.getFireSwordRecipe().getKey()),
    NYX_BOW(NyxBow.getItemName(), NyxBow.getNyxBowRecipe().getKey()),
    POSEIDONS_TRIDENT(PoseidonsTrident.getItemName(), PoseidonsTrident.getPoseidonsTridentRecipe().getKey()),

    REINFORCED_PICKAXE(ReinforcedPickaxe.getItemName(), ReinforcedPickaxe.getReinforcedPickaxeRecipe().getKey()),
    BETTER_REINFORCED_PICKAXE(BetterReinforcedPickaxe.getItemName(), BetterReinforcedPickaxe.getBetterReinforcedPickaxeRecipe().getKey()),

    GOLD_SHARD(GoldShard.getItemName(), GoldShard.getGoldShardRecipe().getKey()),
    MAGMA_SHARD(MagmaShard.getItemName(), MagmaShard.getMagmaShardRecipe().getKey()),
    REINFORCED_STICK(ReinforcedStick.getItemName(), ReinforcedStick.getReinforcedStickRecipe().getKey()),
    TRIDENT_SPIKES(TridentSpikes.getItemName(), TridentSpikes.getTridentSpikesRecipe().getKey()),
    TRIDENT_STICK(TridentStick.getItemName(), TridentStick.getTridentStickRecipe().getKey()),

    SOUL_HEART_CRYSTAL(SoulHeartCrystal.getItemName(), SoulHeartCrystal.getNamespacedKey()),

    EVENT_INFO_BOOK(EventInfoBook.getItemName(), EventInfoBook.getNamespacedKey());

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
