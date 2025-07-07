package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.item.tool.*;
import de.einfachesache.flareonEvents.item.ingredient.*;
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

        if (args.length != 1) {
            player.sendMessage(Component.text(
                    "Usage: /" + label + " <PoseidonsTrident §7|§c NyxBow §7|§c MiningPickaxe §7|§c Ingredient §7|§c all>",
                    NamedTextColor.RED
            ));
            return true;
        }


         switch (args[0].toLowerCase()) {
            case "firesword" -> giveFireSword(player);
            case "thundertrident", "poseidonstrident" -> givePoseidonsTrident(player);
            case "nyxbow" -> giveNyxBow(player);
            case "miningpickaxe" -> giveReinforcedPickaxe(player);
             case "ingredient" -> giveIngredient(player);
            case "all" -> giveAllItems(player);
            default -> player.sendMessage(Component.text(
                    "Unbekannter Item-Key. Bitte nutze FireSword, ThunderTrident, NyxBow, MiningPickaxe, Ingredient oder all.",
                    NamedTextColor.RED
            ));
        }

        return true;
    }

    private void giveFireSword(Player player) {
        ItemStack fireSword = FireSword.createFireSword();
        player.getInventory().addItem(fireSword);
        player.sendMessage(Component.text(
                "Item FireSword created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void givePoseidonsTrident(Player player) {
        ItemStack trident = PoseidonsTrident.createPoseidonsTrident();
        player.getInventory().addItem(trident);
        player.sendMessage(Component.text(
                "Item Poseidon's Trident created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveNyxBow(Player player) {
        ItemStack nyxBow = NyxBow.createNyxBow();
        player.getInventory().addItem(nyxBow);
        player.sendMessage(Component.text(
                "Item Nyx Bow created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveReinforcedPickaxe(Player player) {
        ItemStack pickaxe = ReinforcedPickaxe.createReinforcedPickaxe();
        player.getInventory().addItem(pickaxe);
        player.sendMessage(Component.text(
                "Item Reinforced Pickaxe created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveBetterReinforcedPickaxe(Player player) {
        ItemStack pickaxe = BetterReinforcedPickaxe.createBetterReinforcedPickaxe();
        player.getInventory().addItem(pickaxe);
        player.sendMessage(Component.text(
                "Item Better Reinforced Pickaxe created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveIngredient(Player player) {

        player.getInventory().addItem(GoldShard.getItem());
        player.getInventory().addItem(MagmaShard.getItem());
        player.getInventory().addItem(TridentSpikes.getItem());
        player.getInventory().addItem(TridentStick.getItem());
        player.getInventory().addItem(ReinforcedStick.getItem());

        player.sendMessage(Component.text(
                "Ingredient created!",
                NamedTextColor.GREEN
        ));
    }

    private void giveAllItems(Player player) {
        giveFireSword(player);
        givePoseidonsTrident(player);
        giveNyxBow(player);
        giveBetterReinforcedPickaxe(player);
        giveReinforcedPickaxe(player);
    }
}
