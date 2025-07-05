package de.einfachesache.flareonEvents.item;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum CustomWeapons {

    FIRE_SWORD(FireSword.getItemName(), FireSword.getFireSwordRecipe().getKey()),
    NYX_BOW(NyxBow.getItemName(), NyxBow.getNyxBowRecipe().getKey()),
    POSEIDONS_TRIDENT(PoseidonsTrident.getItemName(), PoseidonsTrident.getPoseidonsTridentRecipe().getKey()),
    REINFORCED_PICKAXE(ReinforcedPickaxe.getItemName(), ReinforcedPickaxe.getReinforcedPickaxe().getKey());

    private final String displayName;
    private final NamespacedKey key;

    CustomWeapons(String displayName, NamespacedKey key) {
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
