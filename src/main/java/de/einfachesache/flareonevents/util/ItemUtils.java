package de.einfachesache.flareonevents.util;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.weapon.SoulEaterScythe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("deprecation")
public class ItemUtils {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    static NamespacedKey invulnerable = new NamespacedKey(FlareonEvents.getPlugin(), "invulnerable");

    public static ItemStack createCustomItem(Material mat, String name, NamespacedKey itemKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setUnbreakable(true);
        meta.displayName(Component.text(name));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(invulnerable, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);

        item.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        return item;
    }

    public static boolean isInvulnerable(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(FlareonEvents.getPlugin(), "invulnerable");
        return container.has(key, PersistentDataType.BYTE);
    }


    public static boolean isCustomItem(ItemStack item, CustomItem customItem) {
        if (item == null || customItem == null) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        NamespacedKey key = customItem.getNamespacedKey();
        if (key == null) return false;

        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public static void updateInventorys(CustomItem... customItems) {
        for (Player player : Bukkit.getOnlinePlayers()) {

            PlayerInventory inventory = player.getInventory();

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, replaceInSlot(inventory.getItem(i), customItems));
            }

            ItemStack[] armor = inventory.getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                armor[i] = replaceInSlot(armor[i], customItems);
            }

            inventory.setArmorContents(armor);
            inventory.setItemInOffHand(replaceInSlot(inventory.getItemInOffHand(), customItems));
        }
    }

    public static ItemStack replaceInSlot(ItemStack oldItem, CustomItem... customItems) {
        if (oldItem == null || !oldItem.hasItemMeta()) return oldItem;
        for (CustomItem customItem : customItems) {
            if (customItem.matches(oldItem)) {
                ItemStack newItem = customItem.getItem();
                newItem.setAmount(oldItem.getAmount());

                return switch (customItem) {
                    case SOUL_HEART_CRYSTAL -> SoulHeartCrystal.create(SoulHeartCrystal.getDroppedByPlayer(oldItem));
                    case SOUL_EATER_SCYTHE -> SoulEaterScythe.create(SoulEaterScythe.getKillCount(oldItem));
                    default -> newItem;
                };
            }
        }
        return oldItem;
    }

    public static ItemStack createGuiBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Zurück", NamedTextColor.RED));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getComingSoonItem() {
        ItemStack notFoundItem = ItemStack.of(Material.BARRIER);
        ItemMeta meta = notFoundItem.getItemMeta();
        meta.displayName(Component.text("Kommt bald", NamedTextColor.RED).decorate(TextDecoration.BOLD, TextDecoration.ITALIC));
        notFoundItem.setItemMeta(meta);
        return notFoundItem;
    }

    public static ShapedRecipe getNotFoundRecipe() {
        ItemStack notFoundItem = ItemStack.of(Material.BARRIER);
        ItemMeta meta = notFoundItem.getItemMeta();
        meta.displayName(Component.text("Kein Rezept Verfügbar", NamedTextColor.RED).decorate(TextDecoration.BOLD, TextDecoration.ITALIC));
        notFoundItem.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(FlareonEvents.getPlugin(), "not_found"), notFoundItem);
        recipe.shape("   ", " B ", "   ");
        recipe.setIngredient('B', notFoundItem);
        return recipe;
    }

    private static final String enchantmentKey = "Verzauberung";

    public static List<Component> getEnchantments(Map<Enchantment, Integer> enchantments) {
        List<Component> lore = new ArrayList<>();

        lore.add(deserialize("§7" + enchantmentKey + (enchantments.size() > 1 ? "en" : "") + ":"));

        if (enchantments.isEmpty()) {
            lore.add(deserialize("§7➤ §8(noch keine)"));
            return lore;
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            String key = entry.getKey().getKey().getKey();
            String name = Arrays.stream(key.split("_")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
            String roman = ItemUtils.toRoman(entry.getValue());
            lore.add(deserialize("§7➤ §4" + name + " " + roman));
        }

        return lore;
    }

    public static void rewriteEnchantLoreWithNewEnchantments(ItemStack result) {
        List<Component> lore = Objects.requireNonNullElseGet(result.lore(), Collections::emptyList);
        Map<Enchantment, Integer> enchantments = result.getEnchantments().entrySet().stream()
                .filter(e -> e.getKey() != Enchantment.UNBREAKING)
                .sorted(Comparator.comparing(e -> e.getKey().getKey().asString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        int enchantmentHeader = -1;
        int enchantmentsEnd = -1;
        for (int i = 0; i < lore.size(); i++) {

            String serialized = serialize(lore.get(i)).trim();

            if (serialized.startsWith(enchantmentKey)) {
                enchantmentHeader = i;
            }

            if (enchantmentHeader != -1 && serialized.equalsIgnoreCase("")) {
                enchantmentsEnd = i;
                break;
            }
        }

        final int enchantmentHeaderFinal = enchantmentHeader;
        final int enchantmentsEndFinal = enchantmentsEnd;
        List<Component> finalLore = (enchantmentHeader < 0)
                ? new ArrayList<>(lore)
                : IntStream.range(0, lore.size())
                .filter(i -> (i < enchantmentHeaderFinal || enchantmentsEndFinal < i) || (!serialize(lore.get(i)).trim().startsWith("➤") && !serialize(lore.get(i)).trim().startsWith(enchantmentKey)))
                .mapToObj(lore::get).collect(Collectors.toCollection(ArrayList::new));

        finalLore.addAll(enchantmentHeader != -1 ? enchantmentHeader : finalLore.size(), ItemUtils.getEnchantments(enchantments));

        if (!serialize(finalLore.getLast()).isEmpty()) {
            finalLore.add(deserialize("§7"));
        }

        result.lore(finalLore);
    }

    public static Material getRandomDrop(Map<Material, Integer> chances) {
        int sumWeights = chances.values().stream().mapToInt(Integer::intValue).sum();
        int roll = ThreadLocalRandom.current().nextInt(100) + 1;

        if (roll > sumWeights) {
            return null;
        }

        int cum = 0;
        for (var e : chances.entrySet()) {
            cum += e.getValue();
            if (roll <= cum) {
                return e.getKey();
            }
        }
        return null;
    }

    public static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    public static Integer getCustomModelDataIfSet(ItemStack item) {
        if (!item.hasItemMeta()) {
            return null;
        }

        if (!item.getItemMeta().hasCustomModelData()) {
            return null;
        }

        return item.getItemMeta().getCustomModelData();
    }

    public static Component deserialize(String string) {
        return LegacyComponentSerializer.legacySection().deserialize(string);
    }

    public static String serialize(Component component) {
        return PLAIN.serialize(component);
    }
}
