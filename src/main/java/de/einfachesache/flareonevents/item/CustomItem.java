package de.einfachesache.flareonevents.item;

import de.einfachesache.flareonevents.item.armor.assassins.AssassinsBoots;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsChestplate;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsHelmet;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsLeggings;
import de.einfachesache.flareonevents.item.ingredient.*;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.tool.ReinforcedPickaxe;
import de.einfachesache.flareonevents.item.tool.SuperiorPickaxe;
import de.einfachesache.flareonevents.item.weapon.FireSword;
import de.einfachesache.flareonevents.item.weapon.NyxBow;
import de.einfachesache.flareonevents.item.weapon.PoseidonsTrident;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Supplier;

public enum CustomItem {

    FIRE_SWORD(FireSword.DISPLAY_NAME, FireSword.NAMESPACED_KEY, CustomItemType.WEAPON, FireSword::createFireSword),
    NYX_BOW(NyxBow.DISPLAY_NAME, NyxBow.NAMESPACED_KEY, CustomItemType.WEAPON, NyxBow::createNyxBow),
    POSEIDONS_TRIDENT(PoseidonsTrident.DISPLAY_NAME, PoseidonsTrident.NAMESPACED_KEY, CustomItemType.WEAPON, PoseidonsTrident::createPoseidonsTrident),

    REINFORCED_PICKAXE(ReinforcedPickaxe.DISPLAY_NAME, ReinforcedPickaxe.NAMESPACED_KEY, CustomItemType.TOOL, ReinforcedPickaxe::createReinforcedPickaxe),
    SUPERIOR_PICKAXE(SuperiorPickaxe.DISPLAY_NAME, SuperiorPickaxe.NAMESPACED_KEY, CustomItemType.TOOL, SuperiorPickaxe::createSuperiorPickaxe),

    SOUL_HEART_CRYSTAL(SoulHeartCrystal.DISPLAY_NAME, SoulHeartCrystal.NAMESPACED_KEY, CustomItemType.MISC, SoulHeartCrystal::createSoulHeartCrystal),

    ASSASSINS_HELMET(AssassinsHelmet.DISPLAY_NAME, AssassinsHelmet.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsHelmet::createAssassinsAmor),
    ASSASSINS_CHESTPLATE(AssassinsChestplate.DISPLAY_NAME, AssassinsChestplate.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsChestplate::createAssassinsAmor),
    ASSASSINS_LEGGINGS(AssassinsLeggings.DISPLAY_NAME, AssassinsLeggings.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsLeggings::createAssassinsAmor),
    ASSASSINS_BOOTS(AssassinsBoots.DISPLAY_NAME, AssassinsBoots.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsBoots::createAssassinsAmor),

    GOLD_SHARD(GoldShard.DISPLAY_NAME, GoldShard.NAMESPACED_KEY, CustomItemType.INGREDIENT, GoldShard.ITEM::clone),
    MAGMA_SHARD(MagmaShard.DISPLAY_NAME, MagmaShard.NAMESPACED_KEY, CustomItemType.INGREDIENT, MagmaShard.ITEM::clone),
    TRIDENT_SPIKES(TridentSpikes.DISPLAY_NAME, TridentSpikes.NAMESPACED_KEY, CustomItemType.INGREDIENT, TridentSpikes.ITEM::clone),
    TRIDENT_STICK(TridentStick.DISPLAY_NAME, TridentStick.NAMESPACED_KEY, CustomItemType.INGREDIENT, TridentStick.ITEM::clone),
    REINFORCED_STICK(ReinforcedStick.DISPLAY_NAME, ReinforcedStick.NAMESPACED_KEY, CustomItemType.INGREDIENT, ReinforcedStick.ITEM::clone),

    EVENT_INFO_BOOK(EventInfoBook.DISPLAY_NAME, EventInfoBook.NAMESPACED_KEY, CustomItemType.OTHER, EventInfoBook::createEventInfoBook);

    private final String displayName; private final NamespacedKey namespacedKey; private final CustomItemType customItemType; private final Supplier<ItemStack> creator;

    CustomItem(String displayName, NamespacedKey namespacedKey, CustomItemType customItemType, Supplier<ItemStack> creator) {
        this.displayName = displayName; this.namespacedKey = namespacedKey; this.customItemType = customItemType; this.creator = creator;
    }

    public boolean matches(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.BYTE);
    }

    public void applyName(ItemMeta meta) {
        meta.displayName(Component.text(displayName));
    }

    public ItemStack getItem() {
        return creator.get();
    }

    public CustomItemType getCustomItemType() {
        return customItemType;
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    public enum CustomItemType {

        WEAPON("Waffen"),
        TOOL("Werkzeuge"),
        ARMOR("Rüstungen"),
        INGREDIENT("Ingredient"),
        MISC("Misc"),
        OTHER(null);

        private final String displayName;

        CustomItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}