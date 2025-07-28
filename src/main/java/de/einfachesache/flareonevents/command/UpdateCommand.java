package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.Config;
import de.einfachesache.flareonevents.FlareonEvents;
import de.einfachesache.flareonevents.item.CustomItems;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.item.ingredient.*;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.tool.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UpdateCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("book")) {

            Config.reloadBook();

            updateInventorys(CustomItems.EVENT_INFO_BOOK);

            sender.sendMessage("§aDas Event-Buch wurden aktualisiert!");

            return true;
        }


        if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof Player player && player.getUniqueId().equals(FlareonEvents.DEV_UUID))) {
            sender.sendMessage("§cDu darfst diesen Command nicht verwenden.");
            return false;
        }

        Config.reloadFiles();

        updateInventorys(CustomItems.values());

        sender.sendMessage("§aAlle Configs & Custom-Items wurden aktualisiert!");

        return true;
    }

    private void updateInventorys(CustomItems... customItems) {
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

    private ItemStack replaceInSlot(ItemStack item, CustomItems... customItems) {
        if (item == null || !item.hasItemMeta()) return item;
        for (CustomItems customItem : customItems) {
            if (customItem.matches(item)) {
                ItemStack newItem;
                switch (customItem) {
                    case FIRE_SWORD:
                        newItem = FireSword.createFireSword();
                        break;
                    case NYX_BOW:
                        newItem = NyxBow.createNyxBow();
                        break;
                    case POSEIDONS_TRIDENT:
                        newItem = PoseidonsTrident.createPoseidonsTrident();
                        break;
                    case REINFORCED_PICKAXE:
                        newItem = ReinforcedPickaxe.createReinforcedPickaxe();
                        break;
                    case BETTER_REINFORCED_PICKAXE:
                        newItem = BetterReinforcedPickaxe.createBetterReinforcedPickaxe();
                        break;
                    case GOLD_SHARD:
                        newItem = GoldShard.getItem();
                        break;
                    case MAGMA_SHARD:
                        newItem = MagmaShard.getItem();
                        break;
                    case REINFORCED_STICK:
                        newItem = ReinforcedStick.getItem();
                        break;
                    case TRIDENT_SPIKES:
                        newItem = TridentSpikes.getItem();
                        break;
                    case TRIDENT_STICK:
                        newItem = TridentStick.getItem();
                        break;
                    case SOUL_HEART_CRYSTAL:
                        newItem = SoulHeartCrystal.createSoulHeartCrystal();
                        break;
                    case EVENT_INFO_BOOK:
                        newItem = EventInfoBook.createEventInfoBook();
                        break;
                    default:
                        return item;
                }

                newItem.setAmount(item.getAmount());
                return newItem;
            }
        }
        return item;
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
