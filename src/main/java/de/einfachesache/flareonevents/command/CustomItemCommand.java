package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.ingredient.*;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.tool.ReinforcedPickaxe;
import de.einfachesache.flareonevents.item.tool.SuperiorPickaxe;
import de.einfachesache.flareonevents.item.weapon.FireSword;
import de.einfachesache.flareonevents.item.weapon.NyxBow;
import de.einfachesache.flareonevents.item.weapon.PoseidonsTrident;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomItemCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("FIRE_SWORD", "POSEIDONS_TRIDENT", "NYX_BOW", "REINFORCED_PICKAXE", "SUPERIOR_PICKAXE", "INGREDIENT", "MISC", "ALL_GEAR", "ALL");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Command benutzen.");
            return true;
        }

        if (args.length != 1) {
            sendUsage(player, alias);
            return true;
        }


        switch (args[0].toLowerCase()) {
            case "fire_sword" -> giveItem(player, FireSword.createFireSword());
            case "poseidons_trident" -> giveItem(player, PoseidonsTrident.createPoseidonsTrident());
            case "nyx_bow" -> giveItem(player, NyxBow.createNyxBow());
            case "reinforced_pickaxe" -> giveItem(player, ReinforcedPickaxe.createReinforcedPickaxe());
            case "superior_pickaxe" -> giveItem(player, SuperiorPickaxe.createSuperiorPickaxe());
            case "ingredient" -> giveAllIngredients(player);
            case "misc" -> giveItem(player, SoulHeartCrystal.createSoulHeartCrystal());
            case "all_gear" -> giveAllGear(player);
            case "all" -> giveAllItems(player);
            default -> {
                player.sendMessage(Component.text("Unbekannter Item-Key. ", NamedTextColor.RED));
                sendUsage(player, alias);
            }
        }

        return true;
    }

    private void sendUsage(Player player, String label) {
        player.sendMessage(Component.text("§cVerwendung: /" + label + " <key>").color(NamedTextColor.RED));
        player.sendMessage(Component.text("§7Verfügbare Keys:"));
        SUB_COMMANDS.forEach(command -> player.sendMessage(Component.text(" §8- §c" + command)));
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

    private void giveItem(Player player, ItemStack item) {
        player.getInventory().addItem(item);
        player.sendMessage(
                Component.text("Item ", NamedTextColor.GREEN)
                        .append(item.effectiveName())
                        .append(Component.text(" created with ModelData: §e" + ItemUtils.getCustomModelDataIfSet(item), NamedTextColor.GREEN)));
    }

    private void giveAllGear(Player player) {
        giveItem(player, FireSword.createFireSword());
        giveItem(player, PoseidonsTrident.createPoseidonsTrident());
        giveItem(player, NyxBow.createNyxBow());
        giveItem(player, SuperiorPickaxe.createSuperiorPickaxe());
        giveItem(player, ReinforcedPickaxe.createReinforcedPickaxe());
    }

    private void giveAllIngredients(Player player) {
        player.getInventory().addItem(GoldShard.ITEM);
        player.getInventory().addItem(MagmaShard.ITEM);
        player.getInventory().addItem(TridentSpikes.ITEM);
        player.getInventory().addItem(TridentStick.ITEM);
        player.getInventory().addItem(ReinforcedStick.ITEM);

        player.sendMessage(Component.text("Ingredient items created!", NamedTextColor.GREEN));
    }

    private void giveAllItems(Player player) {
        giveItem(player, FireSword.createFireSword());
        giveItem(player, PoseidonsTrident.createPoseidonsTrident());
        giveItem(player, NyxBow.createNyxBow());
        giveItem(player, SuperiorPickaxe.createSuperiorPickaxe());
        giveItem(player, ReinforcedPickaxe.createReinforcedPickaxe());

        giveItem(player, SoulHeartCrystal.createSoulHeartCrystal());

        giveAllIngredients(player);
    }
}
