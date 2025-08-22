package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.item.ItemRecipe;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UpdateCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String @NotNull [] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("book")) {

            Config.reloadBook();

            updateInventorys(CustomItem.EVENT_INFO_BOOK);

            sender.sendMessage("§aDas Event-Buch wurden aktualisiert!");

            return true;
        }


        if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof Player player && player.getUniqueId().equals(FlareonEvents.DEV_UUID))) {
            sender.sendMessage("§cDu darfst diesen Command nicht verwenden.");
            return false;
        }

        CompletableFuture.allOf(Config.reloadFiles()).thenRun(() -> updateInventorys(CustomItem.values()));
        ItemRecipe.reloadAllPluginRecipes();

        sender.sendMessage("§aAlle Configs & Custom-Items wurden aktualisiert!");

        return true;
    }

    private void updateInventorys(CustomItem... customItems) {
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

    private ItemStack replaceInSlot(ItemStack oldItem, CustomItem... customItems) {
        if (oldItem == null || !oldItem.hasItemMeta()) return oldItem;
        for (CustomItem customItem : customItems) {
            if (customItem.matches(oldItem)) {
                ItemStack newItem = customItem.getItem();
                newItem.setAmount(oldItem.getAmount());
                return customItem.equals(CustomItem.SOUL_HEART_CRYSTAL) ? SoulHeartCrystal.createSoulHeartCrystal(SoulHeartCrystal.getDroppedByPlayer(oldItem)) : newItem;
            }
        }
        return oldItem;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            if ("book".startsWith(input)) {
                completions.add("book");
            }

            return completions;
        }
        return new ArrayList<>();
    }
}
