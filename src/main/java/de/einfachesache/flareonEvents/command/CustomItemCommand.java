package de.einfachesache.flareonEvents.command;

import de.einfachesache.flareonEvents.item.ingredient.*;
import de.einfachesache.flareonEvents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonEvents.item.tool.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomItemCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("FIRE_SWORD", "POSEIDONS_TRIDENT", "NYX_BOW", "REINFORCED_PICKAXE", "BETTER_REINFORCED_PICKAXE", "INGREDIENT", "MISC", "ALL");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Command benutzen.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(Component.text(
                    "Usage: /" + label + " <PoseidonsTrident §7|§c NyxBow §7|§c MiningPickaxe §7|§c Ingredient §7|§c Misc §7|§c all>",
                    NamedTextColor.RED
            ));
            return true;
        }


        switch (args[0].toLowerCase()) {
            case "fire_sword", "firesword" -> giveFireSword(player);
            case "poseidons_trident", "poseidonstrident" -> givePoseidonsTrident(player);
            case "nyx_bow", "nyxbow" -> giveNyxBow(player);
            case "reinforced_pickaxe", "reinforcedpickaxe" -> giveReinforcedPickaxe(player);
            case "better_reinforced_pickaxe", "betterreinforcedpickaxe" -> giveBetterReinforcedPickaxe(player);
            case "ingredient" -> giveIngredient(player);
            case "misc" -> giveMisc(player);
            case "all" -> giveAllItems(player);
            default -> player.sendMessage(Component.text("Unbekannter Item-Key. " +
                            "Bitte nutze FireSword, PoseidonsTrident, NyxBow, ReinforcedPickaxe, BetterReinforcedPickaxe, Ingredient, Misc oder all.",
                    NamedTextColor.RED
            ));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String sub : SUB_COMMANDS) {
                if (sub.toLowerCase().startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }

    private void giveFireSword(Player player) {
        player.getInventory().addItem(FireSword.createFireSword());
        player.sendMessage(Component.text(
                "Item FireSword created! with ModelData: ", // fireSword.getItemMeta().getCustomModelData(),
                NamedTextColor.GREEN
        ));
        //NamespacedKey key = NamespacedKey.minecraft("item_model");
        //PersistentDataContainer pdc = fireSword.getItemMeta().getPersistentDataContainer();
        //pdc.set(key, PersistentDataType.STRING, "minecraft:diamond");
    }

    private void givePoseidonsTrident(Player player) {
        player.getInventory().addItem(PoseidonsTrident.createPoseidonsTrident());
        player.sendMessage(Component.text(
                "Item Poseidon's Trident created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveNyxBow(Player player) {
        player.getInventory().addItem(NyxBow.createNyxBow());
        player.sendMessage(Component.text(
                "Item Nyx Bow created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveReinforcedPickaxe(Player player) {
        player.getInventory().addItem(ReinforcedPickaxe.createReinforcedPickaxe());
        player.sendMessage(Component.text(
                "Item Reinforced Pickaxe created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveBetterReinforcedPickaxe(Player player) {
        player.getInventory().addItem(BetterReinforcedPickaxe.createBetterReinforcedPickaxe());
        player.sendMessage(Component.text(
                "Item Better Reinforced Pickaxe created! with ModelData: null",
                NamedTextColor.GREEN
        ));
    }

    private void giveMisc(Player player) {
        player.getInventory().addItem(SoulHeartCrystal.createSoulHeartCrystal().asQuantity(64));
        player.sendMessage(Component.text(
                "Item Soul Heart Crystal created! with ModelData: null",
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
