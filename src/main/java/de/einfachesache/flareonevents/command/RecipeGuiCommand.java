package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItems;
import de.einfachesache.flareonevents.item.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecipeGuiCommand implements CommandExecutor, Listener {

    private static final Component MAIN_GUI_TITLE = Component.text("Custom Item Rezepte", NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC);
    private static final String CATEGORY_GUI_KEY = "category_gui";
    private static final String ITEM_GUI_KEY = "item_gui";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Command benutzen.");
            return true;
        }

        openCategoryGui(player);
        return true;
    }

    private void openCategoryGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, MAIN_GUI_TITLE);
        int[] positions = {10, 12, 14, 16}; // Positionen für die Icons

        CustomItems.CustomItemType[] types = Arrays.stream(CustomItems.CustomItemType.values())
                .filter(t -> !t.equals(CustomItems.CustomItemType.OTHER))
                .toArray(CustomItems.CustomItemType[]::new);

        for (int i = 0; i < types.length; i++) {
            CustomItems.CustomItemType type = types[i];
            Material iconMaterial = switch (type) {
                case TOOL -> Material.GOLDEN_PICKAXE;
                case WEAPON -> Material.GOLDEN_SWORD;
                case MISC -> Material.ENCHANTED_GOLDEN_APPLE;
                case INGREDIENT -> Material.RAW_GOLD;
                default -> Material.BARRIER;
            };

            ItemStack icon = new ItemStack(iconMaterial);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text(type.getDisplayName(), NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
            meta.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), CATEGORY_GUI_KEY), PersistentDataType.STRING, type.name());
            icon.setItemMeta(meta);

            gui.setItem(positions[i], icon);
        }

        player.openInventory(gui);
    }

    private void openItemsOfCategory(Player player, CustomItems.CustomItemType type) {
        List<CustomItems> items = Arrays.stream(CustomItems.values())
                .filter(i -> i.getCustomItemType() == type)
                .toList();

        int itemCount = items.size();
        int totalSlots = ((itemCount * 2 + 1 + 8) / 9) * 9; // +1 für Zurück, auf nächste 9 aufrunden
        totalSlots = Math.max(9, Math.min(totalSlots, 54)); // mindestens 2 Reihen, max 6

        Inventory gui = Bukkit.createInventory(null, totalSlots, getTitleForCategory(type));

        int index = 0; // Start bei 2. Zeile Mitte
        for (CustomItems item : items) {
            if (index >= totalSlots - 1) break; // Platz für "Zurück"-Button lassen

            ItemStack guiItem = ItemUtils.createGuiItem(item.getItem());
            ItemMeta meta = guiItem.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), ITEM_GUI_KEY), PersistentDataType.STRING, item.name());
            guiItem.setItemMeta(meta);

            gui.setItem(index, guiItem);
            index += 2; // Mit Lücke für schönere Verteilung
        }

        gui.setItem(totalSlots - 1, ItemUtils.createGuiItemFromMaterial(Material.ARROW, "§cZurück"));
        player.openInventory(gui);
    }

    private void showRecipeGUI(Player player, ShapedRecipe recipe, String title) {

        if (recipe == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, getTitleForRecipe(title));
        String[] shape = recipe.getShape();
        Map<Character, RecipeChoice> keyMap = recipe.getChoiceMap();

        for (int row = 0; row < shape.length; row++) {
            String line = shape[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c == ' ') continue;

                RecipeChoice choice = keyMap.get(c);
                int slot = row * 9 + col + 3;

                if (choice instanceof RecipeChoice.MaterialChoice matChoice) {
                    inv.setItem(slot, new ItemStack(matChoice.getChoices().getFirst()));
                } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
                    inv.setItem(slot, exactChoice.getChoices().getFirst().clone());
                }
            }
        }

        inv.setItem(inv.getSize() - 1, ItemUtils.createGuiItemFromMaterial(Material.ARROW, "§cZurück"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        ItemStack clicked = e.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        var container = meta.getPersistentDataContainer();

        // Kategorieauswahl
        if (e.getView().title().equals(MAIN_GUI_TITLE)) {
            String categoryName = container.get(new NamespacedKey(FlareonEvents.getPlugin(), CATEGORY_GUI_KEY), PersistentDataType.STRING);
            if (categoryName != null) {
                try {
                    CustomItems.CustomItemType type = CustomItems.CustomItemType.valueOf(categoryName);
                    openItemsOfCategory(player, type);
                } catch (IllegalArgumentException ignored) {}
            }
            return;
        }

        // Rezeptanzeige
        String customItemName = container.get(new NamespacedKey(FlareonEvents.getPlugin(), ITEM_GUI_KEY), PersistentDataType.STRING);
        if (customItemName != null) {
            try {
                CustomItems item = CustomItems.valueOf(customItemName);
                showRecipeGUI(player, getRecipeFor(item), ItemUtils.getExactDisplayName(item.getItem()));
            } catch (Exception exception) {
                e.setCancelled(true);
                FlareonEvents.getLogManager().error(exception.getMessage(), exception);
            }
            return;
        }

        // Zurück
        if (clicked.getType() == Material.ARROW && Objects.equals(clicked.getItemMeta().displayName(), Component.text("§cZurück"))) {
            openCategoryGui(player);
        }
    }

    private ShapedRecipe getRecipeFor(CustomItems item) {
        var recipe = Bukkit.getRecipe(item.getNamespacedKey());
        return (recipe instanceof ShapedRecipe shaped) ? shaped : ItemUtils.getNotFoundRecipe();
    }

    private Component getTitleForCategory(CustomItems.CustomItemType type) {
        return Component.text("Rezepte: " + type.getDisplayName(), NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC);
    }

    private Component getTitleForRecipe(String itemName) {
        return Component.text("Rezept: " + itemName, NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC);
    }
}