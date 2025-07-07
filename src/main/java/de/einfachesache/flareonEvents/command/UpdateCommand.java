package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.Config;
import de.einfachesache.flareonEvents.item.CustomItems;
import de.einfachesache.flareonEvents.item.EventInfoBook;
import de.einfachesache.flareonEvents.item.ingredient.*;
import de.einfachesache.flareonEvents.item.tool.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UpdateCommand implements CommandExecutor {

    private static final UUID ALLOWED_UUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof Player player && player.getUniqueId().equals(ALLOWED_UUID))) {
            sender.sendMessage("§cDu darfst diesen Command nicht verwenden.");
            return false;
        }

        Config.reloadFiles();

        for (Player player : Bukkit.getOnlinePlayers()) {
            updateInventory(player.getInventory());
        }

        sender.sendMessage("§aAlle Configs & Custom-Items wurden aktualisiert!");

        return true;
    }

    private void updateInventory(PlayerInventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, replaceInSlot(inv.getItem(i)));
        }
        // Rüstung
        ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            armor[i] = replaceInSlot(armor[i]);
        }
        inv.setArmorContents(armor);
        inv.setItemInOffHand(replaceInSlot(inv.getItemInOffHand()));
    }

    private ItemStack replaceInSlot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;
        for (CustomItems ci : CustomItems.values()) {
            if (ci.matches(item)) {
                ItemStack newItem;
                switch (ci) {
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
}
