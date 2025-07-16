package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.FlareonEvents;
import de.einfachesache.flareonEvents.item.ItemUtils;
import de.einfachesache.flareonEvents.item.tool.FireSword;
import de.einfachesache.flareonEvents.item.tool.NyxBow;
import de.einfachesache.flareonEvents.item.tool.PoseidonsTrident;
import de.einfachesache.flareonEvents.item.tool.ReinforcedPickaxe;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RecipeGuiCommand implements CommandExecutor, Listener {

    private final Component guiTitle = Component.text("Custom Recipes");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Command benutzen.");
            return true;
        }

        openGUI(player);

        return true;
    }

    private void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        gui.setItem(1 + 9, ItemUtils.createGuiItem(FireSword.createFireSword()));
        gui.setItem(3 + 9, ItemUtils.createGuiItem(NyxBow.createNyxBow()));
        gui.setItem(5 + 9, ItemUtils.createGuiItem(PoseidonsTrident.createPoseidonsTrident()));
        gui.setItem(7 + 9, ItemUtils.createGuiItem(ReinforcedPickaxe.createReinforcedPickaxe()));

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(guiTitle) && !ItemUtils.legacyString(e.getView().title()).startsWith("Rezept: "))
            return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        if (clicked == null || !clicked.hasItemMeta()) return;

        String namespaceKey = clicked.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(FlareonEvents.getPlugin(), "gui_id"), org.bukkit.persistence.PersistentDataType.STRING);

        if (namespaceKey == null) {
            return;
        }

        if (namespaceKey.equalsIgnoreCase(FireSword.getItemName())) {
            showRecipeGUI(player, FireSword.getFireSwordRecipe(), FireSword.getItemName());
        } else if (namespaceKey.equalsIgnoreCase(NyxBow.getItemName())) {
            showRecipeGUI(player, NyxBow.getNyxBowRecipe(), NyxBow.getItemName());
        } else if (namespaceKey.equalsIgnoreCase(PoseidonsTrident.getItemName())) {
            showRecipeGUI(player, PoseidonsTrident.getPoseidonsTridentRecipe(), PoseidonsTrident.getItemName());
        } else if (namespaceKey.equalsIgnoreCase(ReinforcedPickaxe.getItemName())) {
            showRecipeGUI(player, ReinforcedPickaxe.getReinforcedPickaxeRecipe(), ReinforcedPickaxe.getItemName());
        } else if (namespaceKey.equalsIgnoreCase("§cZurück")) {
            openGUI(player);
        }
    }

    private void showRecipeGUI(Player player, ShapedRecipe recipe, String title) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Rezept: " + title));
        String[] shape = recipe.getShape();
        Map<Character, RecipeChoice> keyMap = recipe.getChoiceMap();

        for (int row = 0; row < shape.length; row++) {
            String line = shape[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c == ' ') continue;

                RecipeChoice choice = keyMap.get(c);
                int slot = row * 9 + col + 3;

                if (choice instanceof RecipeChoice.MaterialChoice) {
                    // Standard-Ingredient (Material)
                    Material mat = ((RecipeChoice.MaterialChoice) choice).getChoices().getFirst();
                    inv.setItem(slot, new ItemStack(mat));

                } else if (choice instanceof RecipeChoice.ExactChoice) {
                    // Custom-Ingredient (ItemStack mit eigenem Namen, Meta etc.)
                    // Wir klonen das erste Item aus der Liste, damit wir die Original-Instanz nicht verändern
                    ItemStack custom = ((RecipeChoice.ExactChoice) choice).getChoices().getFirst().clone();
                    inv.setItem(slot, custom);

                }
            }
        }

        inv.setItem(inv.getSize() - 1,
                ItemUtils.createGuiItemFromMaterial(Material.ARROW, "§cZurück")
        );

        player.openInventory(inv);
    }
}

