package de.einfachesache.flareonevents.item;

import de.einfachesache.flareonevents.item.armor.assassins.AssassinsBoots;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsChestplate;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsHelmet;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsLeggings;
import de.einfachesache.flareonevents.item.ingredient.*;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.item.misc.GoldenApple;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.tool.ReinforcedPickaxe;
import de.einfachesache.flareonevents.item.tool.SuperiorPickaxe;
import de.einfachesache.flareonevents.item.weapon.BloodSword;
import de.einfachesache.flareonevents.item.weapon.IceBow;
import de.einfachesache.flareonevents.item.weapon.SoulEaterScythe;
import de.einfachesache.flareonevents.item.weapon.ThunderSpear;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

public enum CustomItem {

    BLOOD_SWORD(BloodSword.NAMESPACED_KEY, CustomItemType.WEAPON, BloodSword.getShapedRecipe(), BloodSword::create),
    ICE_BOW(IceBow.NAMESPACED_KEY, CustomItemType.WEAPON, IceBow.getShapedRecipe(), IceBow::create),
    SOUL_EATER_SCYTHE(SoulEaterScythe.NAMESPACED_KEY, CustomItemType.WEAPON, SoulEaterScythe.getShapedRecipe(), SoulEaterScythe::create),
    THUNDER_SPEAR(ThunderSpear.NAMESPACED_KEY, CustomItemType.WEAPON, ThunderSpear.getShapedRecipe(), ThunderSpear::create),

    REINFORCED_PICKAXE(ReinforcedPickaxe.NAMESPACED_KEY, CustomItemType.TOOL, ReinforcedPickaxe.getShapedRecipe(), ReinforcedPickaxe::create),
    SUPERIOR_PICKAXE(SuperiorPickaxe.NAMESPACED_KEY, CustomItemType.TOOL, SuperiorPickaxe.getShapedRecipe(), SuperiorPickaxe::create),

    ASSASSINS_HELMET(AssassinsHelmet.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsHelmet.getShapedRecipe(), AssassinsHelmet::create),
    ASSASSINS_CHESTPLATE(AssassinsChestplate.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsChestplate.getShapedRecipe(), AssassinsChestplate::create),
    ASSASSINS_LEGGINGS(AssassinsLeggings.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsLeggings.getShapedRecipe(), AssassinsLeggings::create),
    ASSASSINS_BOOTS(AssassinsBoots.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsBoots.getShapedRecipe(), AssassinsBoots::create),

    ICE_CRYSTAL(IceCrystal.NAMESPACED_KEY, CustomItemType.INGREDIENT, IceCrystal.getShapedRecipe(), IceCrystal.ITEM::clone),
    BLOOD_SHARD(BloodShard.NAMESPACED_KEY, CustomItemType.INGREDIENT, BloodShard.getShapedRecipe(), BloodShard.ITEM::clone),
    SPEAR_HEAD(SpearHead.NAMESPACED_KEY, CustomItemType.INGREDIENT, SpearHead.getShapedRecipe(), SpearHead.ITEM::clone),
    SPEAR_STICK(SpearStick.NAMESPACED_KEY, CustomItemType.INGREDIENT, SpearStick.getShapedRecipe(), SpearStick.ITEM::clone),
    DEMON_SOUL(DemonSoul.NAMESPACED_KEY, CustomItemType.INGREDIENT, DemonSoul.getShapedRecipe(), DemonSoul.ITEM::clone),
    REINFORCED_STICK(ReinforcedStick.NAMESPACED_KEY, CustomItemType.INGREDIENT, ReinforcedStick.getShapedRecipe(), ReinforcedStick.ITEM::clone),

    GOLDEN_APPLE(GoldenApple.NAMESPACED_KEY, CustomItemType.MISC, GoldenApple.getShapedRecipe(), GoldenApple.ITEM::clone),

    SOUL_HEART_CRYSTAL(SoulHeartCrystal.NAMESPACED_KEY, CustomItemType.MISC, null, SoulHeartCrystal::create),

    EVENT_INFO_BOOK(EventInfoBook.NAMESPACED_KEY, CustomItemType.OTHER, null, EventInfoBook::create);

    private static final Set<CustomItem> disabledWeapons = Set.of(ASSASSINS_HELMET, ASSASSINS_CHESTPLATE, ASSASSINS_LEGGINGS, ASSASSINS_BOOTS);

    private final NamespacedKey namespacedKey;
    private final CustomItemType customItemType;
    private final Supplier<ItemStack> creator;
    private final ShapedRecipe recipe;

    CustomItem(NamespacedKey namespacedKey, CustomItemType customItemType, ShapedRecipe recipe, Supplier<ItemStack> creator) {
        this.namespacedKey = namespacedKey;
        this.customItemType = customItemType;
        this.creator = creator;
        this.recipe = recipe;
    }

    public boolean matches(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(namespacedKey, PersistentDataType.BYTE);
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

    public ShapedRecipe getRecipe() {
        return recipe;
    }

    public static CustomItem[] getEnabledItems() {
        return Arrays.stream(values())
                .filter(item -> !disabledWeapons.contains(item))
                .toArray(CustomItem[]::new);
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