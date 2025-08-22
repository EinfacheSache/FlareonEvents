package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
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

import java.util.*;

public class RecipeGuiCommand implements CommandExecutor, Listener {

    private static final Component MAIN_GUI_TITLE = Component.text("Custom Item Rezepte", NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC);
    private static final String CATEGORY_GUI_KEY = "category_gui";
    private static final int[] CATEGORY_GUI_POSITIONS = {10, 12, 14, 16, 31};
    private static final int[] ITEM_GUI_POSITIONS = {10, 12, 14, 16, 28, 30, 32, 34};

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
        Inventory gui = Bukkit.createInventory(new GUIHolder(null), 9 * 5, MAIN_GUI_TITLE);

        CustomItem.CustomItemType[] types = Arrays.stream(CustomItem.CustomItemType.values())
                .filter(t -> !t.equals(CustomItem.CustomItemType.OTHER))
                .toArray(CustomItem.CustomItemType[]::new);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.displayName(Component.text(" "));
        filler.setItemMeta(fm);

        int size = gui.getSize();
        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        for (int i = 0; i < types.length; i++) {
            CustomItem.CustomItemType type = types[i];
            Material iconMaterial = switch (type) {
                case TOOL -> Material.GOLDEN_PICKAXE;
                case WEAPON -> Material.GOLDEN_SWORD;
                case ARMOR -> Material.GOLDEN_CHESTPLATE;
                case MISC -> Material.ENCHANTED_GOLDEN_APPLE;
                case INGREDIENT -> Material.RAW_GOLD;
                default -> Material.BARRIER;
            };

            ItemStack icon = new ItemStack(iconMaterial);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text(type.getDisplayName(), NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
            meta.getPersistentDataContainer().set(new NamespacedKey(FlareonEvents.getPlugin(), CATEGORY_GUI_KEY), PersistentDataType.STRING, type.name());
            icon.setItemMeta(meta);

            gui.setItem(CATEGORY_GUI_POSITIONS[i], icon);
        }

        player.openInventory(gui);
    }

    private void openItemsOfCategory(Player player, CustomItem.CustomItemType type, Inventory previusInventory) {
        List<CustomItem> items = Arrays.stream(CustomItem.values())
                .filter(i -> i.getCustomItemType() == type)
                .toList();

        int itemCount = items.size();
        int totalSlots = ((itemCount * 2 + 1 + 8) / 9) * 9; // +1 für Zurück, auf nächste 9 aufrunden
        totalSlots = Math.max(9 * (type.equals(CustomItem.CustomItemType.INGREDIENT) ? 5 : 3), Math.min(totalSlots, 9 * 6)); // mindestens 3 Reihen, max 6

        Inventory gui = Bukkit.createInventory(new GUIHolder(previusInventory), totalSlots, Component.text("Rezepte: " + type.getDisplayName(), NamedTextColor.GOLD).decorate(TextDecoration.BOLD, TextDecoration.ITALIC));

        // Glas-Pane als Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.displayName(Component.text(" "));
        filler.setItemMeta(fm);

        int size = gui.getSize();
        for (int i = 0; i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        int index = 0;
        for (CustomItem item : items) {
            int itemPos = ITEM_GUI_POSITIONS[index];
            if (itemPos >= totalSlots - 1) break;
            gui.setItem(itemPos, item.getItem());
            index++;
        }

        gui.setItem(totalSlots - 1, ItemUtils.createGuiBackButton());
        player.openInventory(gui);
    }

    private void showRecipeGUI(Player player, ShapedRecipe recipe, Component title, Inventory previusInventory) {

        Inventory inv = Bukkit.createInventory(new GUIHolder(previusInventory), 27, Component.text("Rezept: ", NamedTextColor.GOLD).decorate(TextDecoration.ITALIC, TextDecoration.BOLD).append(title).decoration(TextDecoration.BOLD, false));
        String[] shape = recipe.getShape();
        Map<Character, RecipeChoice> keyMap = recipe.getChoiceMap();
        Set<Integer> centerSlots = Set.of(
                3, 4, 5,
                12, 13, 14,
                21, 22, 23
        );

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

        // Glas-Pane als Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.displayName(Component.text(" "));
        filler.setItemMeta(fm);

        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            if (centerSlots.contains(i) || i == size - 1) continue;
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        inv.setItem(inv.getSize() - 1, ItemUtils.createGuiBackButton());
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        ItemStack clicked = e.getCurrentItem();
        InventoryHolder holder = e.getClickedInventory().getHolder();

        if (!(holder instanceof GUIHolder(Inventory previousInventory))) {
            return;
        }

        e.setCancelled(true);

        if (clicked.getType() == Material.BARRIER || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // 1) Zurück-Button
        if (ItemUtils.createGuiBackButton().equals(e.getCurrentItem())) {
            player.openInventory(previousInventory);
            return;
        }

        // 2) show Recipe GUI
        if (title.startsWith("Rezept: ")) {
            Optional<CustomItem> opt = Arrays.stream(CustomItem.values())
                    .filter(ci -> clicked.isSimilar(ci.getItem()))
                    .findFirst();

            if (opt.isPresent()) {
                showRecipeGUI(player, getCustomRecipeFor(opt.get()), clicked.effectiveName(), e.getClickedInventory());
                return;
            }

            ShapedRecipe recipe = getRecipeFor(clicked);
            showRecipeGUI(player, recipe, recipe.getResult().effectiveName().color(NamedTextColor.AQUA), e.getClickedInventory());
            return;
        }

        // 3) Kategorie-Auswahl
        if (e.getView().title().equals(MAIN_GUI_TITLE)) {
            try {
                String cat = clicked.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(FlareonEvents.getPlugin(), CATEGORY_GUI_KEY), PersistentDataType.STRING);
                openItemsOfCategory(player, CustomItem.CustomItemType.valueOf(cat), e.getClickedInventory());
            } catch (IllegalArgumentException | NullPointerException ex) {
                FlareonEvents.getLogManager().error(ex.getMessage(), ex);
            }
            return;
        }

        // 4) Items einer Kategorie
        Arrays.stream(CustomItem.values())
                .filter(ci -> clicked.isSimilar(ci.getItem())).findFirst().ifPresent(
                        items -> showRecipeGUI(player, getCustomRecipeFor(items), items.getItem().effectiveName(), e.getClickedInventory()));
    }

    private ShapedRecipe getRecipeFor(ItemStack item) {
        return Bukkit.getRecipesFor(item).stream()
                .filter(ShapedRecipe.class::isInstance)
                .map(ShapedRecipe.class::cast)
                .findFirst()
                .orElse(ItemUtils.getNotFoundRecipe());
    }

    private ShapedRecipe getCustomRecipeFor(CustomItem item) {
        Recipe r = Bukkit.getRecipe(item.getNamespacedKey());
        return (r instanceof ShapedRecipe s) ? s : ItemUtils.getNotFoundRecipe();
    }

    private record GUIHolder(Inventory previousInventory) implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return previousInventory;
        }
    }
}