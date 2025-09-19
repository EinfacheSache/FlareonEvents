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
import de.einfachesache.flareonevents.item.weapon.BloodSword;
import de.einfachesache.flareonevents.item.weapon.IceBow;
import de.einfachesache.flareonevents.item.weapon.ThunderSpear;
import de.einfachesache.flareonevents.item.weapon.SoulEaterScythe;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

public enum CustomItem {

    ICE_BOW(IceBow.DISPLAY_NAME, IceBow.NAMESPACED_KEY, CustomItemType.WEAPON, IceBow.getShapedRecipe(), IceBow::create),
    BLOOD_SWORD(BloodSword.DISPLAY_NAME, BloodSword.NAMESPACED_KEY, CustomItemType.WEAPON, BloodSword.getShapedRecipe(), BloodSword::create),
    SOUL_EATER_SCYTHE(SoulEaterScythe.DISPLAY_NAME, SoulEaterScythe.NAMESPACED_KEY, CustomItemType.WEAPON, SoulEaterScythe.getShapedRecipe(), SoulEaterScythe::create),
    THUNDER_SPEAR(ThunderSpear.DISPLAY_NAME, ThunderSpear.NAMESPACED_KEY, CustomItemType.WEAPON, ThunderSpear.getShapedRecipe(), ThunderSpear::create),

    REINFORCED_PICKAXE(ReinforcedPickaxe.DISPLAY_NAME, ReinforcedPickaxe.NAMESPACED_KEY, CustomItemType.TOOL, ReinforcedPickaxe.getShapedRecipe(), ReinforcedPickaxe::create),
    SUPERIOR_PICKAXE(SuperiorPickaxe.DISPLAY_NAME, SuperiorPickaxe.NAMESPACED_KEY, CustomItemType.TOOL, SuperiorPickaxe.getShapedRecipe(), SuperiorPickaxe::create),

    ASSASSINS_HELMET(AssassinsHelmet.DISPLAY_NAME, AssassinsHelmet.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsHelmet.getShapedRecipe(), AssassinsHelmet::create),
    ASSASSINS_CHESTPLATE(AssassinsChestplate.DISPLAY_NAME, AssassinsChestplate.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsChestplate.getShapedRecipe(), AssassinsChestplate::create),
    ASSASSINS_LEGGINGS(AssassinsLeggings.DISPLAY_NAME, AssassinsLeggings.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsLeggings.getShapedRecipe(), AssassinsLeggings::create),
    ASSASSINS_BOOTS(AssassinsBoots.DISPLAY_NAME, AssassinsBoots.NAMESPACED_KEY, CustomItemType.ARMOR, AssassinsBoots.getShapedRecipe(), AssassinsBoots::create),

    ICE_CRYSTAL(IceCrystal.DISPLAY_NAME, IceCrystal.NAMESPACED_KEY, CustomItemType.INGREDIENT, IceCrystal.getShapedRecipe(), IceCrystal.ITEM::clone),
    BLOOD_SHARD(BloodShard.DISPLAY_NAME, BloodShard.NAMESPACED_KEY, CustomItemType.INGREDIENT, BloodShard.getShapedRecipe(), BloodShard.ITEM::clone),
    SPEAR_HEAD(SpearHead.DISPLAY_NAME, SpearHead.NAMESPACED_KEY, CustomItemType.INGREDIENT, SpearHead.getShapedRecipe(), SpearHead.ITEM::clone),
    SPEAR_STICK(SpearStick.DISPLAY_NAME, SpearStick.NAMESPACED_KEY, CustomItemType.INGREDIENT, SpearStick.getShapedRecipe(), SpearStick.ITEM::clone),
    DEMON_SOUL(DemonSoul.DISPLAY_NAME, DemonSoul.NAMESPACED_KEY, CustomItemType.INGREDIENT, DemonSoul.getShapedRecipe(), DemonSoul.ITEM::clone),
    REINFORCED_STICK(ReinforcedStick.DISPLAY_NAME, ReinforcedStick.NAMESPACED_KEY, CustomItemType.INGREDIENT, ReinforcedStick.getShapedRecipe(), ReinforcedStick.ITEM::clone),

    SOUL_HEART_CRYSTAL(SoulHeartCrystal.DISPLAY_NAME, SoulHeartCrystal.NAMESPACED_KEY, CustomItemType.MISC, null, SoulHeartCrystal::create),

    EVENT_INFO_BOOK(EventInfoBook.DISPLAY_NAME, EventInfoBook.NAMESPACED_KEY, CustomItemType.OTHER, null, EventInfoBook::create);

    private static final Set<CustomItem> disabledWeapons = Set.of();

    private final String displayName;
    private final NamespacedKey namespacedKey;
    private final CustomItemType customItemType;
    private final Supplier<ItemStack> creator;
    private final ShapedRecipe recipe;

    CustomItem(String displayName, NamespacedKey namespacedKey, CustomItemType customItemType, ShapedRecipe recipe, Supplier<ItemStack> creator) {
        this.displayName = displayName;
        this.namespacedKey = namespacedKey;
        this.customItemType = customItemType;
        this.creator = creator;
        this.recipe = recipe;
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