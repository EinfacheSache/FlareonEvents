package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItems;
import de.einfachesache.flareonevents.item.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RecipeGuiCommand implements CommandExecutor, Listener {

    private static final Component MAIN_GUI_TITLE = Component.text("Custom Item Rezepte", NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC);
    private static final String CATEGORY_GUI_KEY = "category_gui";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Command benutzen.");
            return true;
        }

        openCategoryGui(player);
        return true;
    }

    private void openCategoryGui(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder(null), 27, MAIN_GUI_TITLE);
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

    private void openItemsOfCategory(Player player, CustomItems.CustomItemType type, Inventory previusInventory) {
        List<CustomItems> items = Arrays.stream(CustomItems.values())
                .filter(i -> i.getCustomItemType() == type)
                .toList();

        int itemCount = items.size();
        int totalSlots = ((itemCount * 2 + 1 + 8) / 9) * 9; // +1 für Zurück, auf nächste 9 aufrunden
        totalSlots = Math.max(9, Math.min(totalSlots, 54)); // mindestens 2 Reihen, max 6

        Inventory gui = Bukkit.createInventory(new GUIHolder(previusInventory), totalSlots, Component.text("Rezepte: " + type.getDisplayName(), NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC));

        int index = 0;
        for (CustomItems item : items) {
            if (index >= totalSlots - 1) break;
            gui.setItem(index, item.getItem().clone());
            index += 2;
        }

        gui.setItem(totalSlots - 1, ItemUtils.createGuiItemFromMaterial(Material.ARROW, "§cZurück"));
        player.openInventory(gui);
    }

    private void showRecipeGUI(Player player, ShapedRecipe recipe, String title, Inventory previusInventory) {
        if (recipe == null) return;

        Inventory inv = Bukkit.createInventory(new GUIHolder(previusInventory), 27, Component.text("Rezept: " + title, NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC));
        String[] shape = recipe.getShape();
        Map<Character, RecipeChoice> keyMap = recipe.getChoiceMap();

        for (int row = 0; row < shape.length; row++) {
            String line = shape[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c == ' ') continue;

                RecipeChoice choice = keyMap.get(c);
                ItemStack guiItem;
                if (choice instanceof RecipeChoice.MaterialChoice mc) {
                    guiItem = new ItemStack(mc.getChoices().getFirst());
                } else if (choice instanceof RecipeChoice.ExactChoice ec) {
                    guiItem = ec.getChoices().getFirst();
                } else {
                    continue;
                }

                int slot = row * 9 + col + 3;
                inv.setItem(slot, guiItem);
            }
        }

        inv.setItem(inv.getSize() - 1, ItemUtils.createGuiItemFromMaterial(Material.ARROW, "§cZurück"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        ItemStack clicked = e.getCurrentItem();
        InventoryHolder holder = e.getClickedInventory().getHolder();

        if(!(holder instanceof GUIHolder(Inventory previusInventory))) {
            return;
        }

        e.setCancelled(true);

        if (title.startsWith("Rezept: ")) {
            e.setCancelled(true);
            if (clicked.getType() != Material.ARROW) {

                Optional<CustomItems> opt = Arrays.stream(CustomItems.values())
                        .filter(ci -> clicked.isSimilar(ci.getItem()))
                        .findFirst();

                if (opt.isPresent()) {
                    showRecipeGUI(player, getCustomRecipeFor(opt.get()), ItemUtils.getExactDisplayName(clicked), e.getClickedInventory());
                    return;
                }

                showRecipeGUI(player, getRecipeFor(clicked), ItemUtils.getExactDisplayName(clicked), e.getClickedInventory());
                return;
            }

            openCategoryGui(player);
        }

        // 2) Kategorie-Auswahl
        if (e.getView().title().equals(MAIN_GUI_TITLE)) {
            String cat = clicked.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(FlareonEvents.getPlugin(), CATEGORY_GUI_KEY), PersistentDataType.STRING);
            if (cat != null) {
                try {
                    openItemsOfCategory(player, CustomItems.CustomItemType.valueOf(cat), e.getClickedInventory());
                } catch (Exception ex) {
                    e.setCancelled(true);
                    FlareonEvents.getLogManager().error(ex.getMessage(), ex);
                }
            }
            return;
        }

        // 3) Items einer Kategorie
        if (clicked != null) {
            try {
                Optional<CustomItems> customItems = Arrays.stream(CustomItems.values())
                        .filter(ci -> clicked.isSimilar(ci.getItem()))
                        .findFirst();
                if (customItems.isPresent()) {
                    showRecipeGUI(player, getCustomRecipeFor(customItems.get()), ItemUtils.getExactDisplayName(customItems.get().getItem()), e.getClickedInventory());
                    return;
                }

            } catch (Exception ex) {
                e.setCancelled(true);
                FlareonEvents.getLogManager().error(ex.getMessage(), ex);
            }
        }

        // 4) Zurück-Button
        if (e.getCurrentItem().getType() == Material.ARROW && Component.text("§cZurück").equals(e.getCurrentItem().getItemMeta().displayName())) {
            player.openInventory(previusInventory);
        }
    }

    private ShapedRecipe getRecipeFor(ItemStack item) {
        return Bukkit.getRecipesFor(item).stream()
                .filter(ShapedRecipe.class::isInstance)
                .map(ShapedRecipe.class::cast)
                .findFirst()
                .orElse(ItemUtils.getNotFoundRecipe());
    }

    private ShapedRecipe getCustomRecipeFor(CustomItems item) {
        Recipe r = Bukkit.getRecipe(item.getNamespacedKey());
        return (r instanceof ShapedRecipe s) ? s : ItemUtils.getNotFoundRecipe();
    }

    record GUIHolder(Inventory previusInventory) implements InventoryHolder {

        @Override
        public @NotNull Inventory getInventory() {
            return previusInventory;
        }
    }
}