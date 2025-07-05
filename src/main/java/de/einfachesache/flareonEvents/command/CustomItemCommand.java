package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.item.FireSword;
import de.einfachesache.flareonEvents.item.ReinforcedPickaxe;
import de.einfachesache.flareonEvents.item.NyxBow;
import de.einfachesache.flareonEvents.item.PoseidonsTrident;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomItemCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)){
            sender.sendMessage(Color.RED + "You must be a player to use this command!");
            return false;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("FireSword")) {
            ItemStack fireSword = FireSword.createFireSword();
            player.getInventory().addItem(fireSword);
            player.sendMessage(Component.text( "Item FireSword created! with ModelData: null", NamedTextColor.GREEN));
            return true;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("ThunderTrident")) {
            ItemStack poseidonsTrident = PoseidonsTrident.createPoseidonsTrident();
            player.getInventory().addItem(poseidonsTrident);
            player.sendMessage(Component.text( "Item Poseidon's Trident created! with ModelData: null" , NamedTextColor.GREEN));
            return true;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("NyxBow")) {
            ItemStack nyxBow = NyxBow.createNyxBow();
            player.getInventory().addItem(nyxBow);
            player.sendMessage(Component.text( "Item Nyx Bow created! with ModelData: null" , NamedTextColor.GREEN));
            return true;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("MiningPickaxe")) {
            ItemStack miningPickaxe = ReinforcedPickaxe.createMiningPickaxe();
            player.getInventory().addItem(miningPickaxe);
            player.sendMessage(Component.text( "Item MiningPickaxe created! with ModelData: null" , NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("--- Verwendung ---", NamedTextColor.RED));
        sender.sendMessage(Component.text("/customitem FireSword", NamedTextColor.RED));
        sender.sendMessage(Component.text("/customitem ThunderTrident",  NamedTextColor.RED));
        sender.sendMessage(Component.text("/customitem NyxBow",  NamedTextColor.RED));
        sender.sendMessage(Component.text("/customitem MiningPickaxe",  NamedTextColor.RED));

        return false;
    }
}
