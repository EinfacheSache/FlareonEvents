package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.item.CustomItem;
import de.einfachesache.flareonevents.util.ItemUtils;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomItemCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS;

    static {
        List<String> items = Arrays.stream(CustomItem.values())
                .filter(customItem ->
                        customItem.getCustomItemType() != CustomItem.CustomItemType.ARMOR
                        && customItem.getCustomItemType() != CustomItem.CustomItemType.INGREDIENT
                        && customItem.getCustomItemType() != CustomItem.CustomItemType.OTHER)
                .map(customItem -> customItem.getNamespacedKey().getKey().toUpperCase())
                .collect(Collectors.toList());

        items.add("INGREDIENTS");
        items.add("ALL_GEAR");
        items.add("ALL_ARMOR");
        items.add("ALL");

        SUB_COMMANDS = Collections.unmodifiableList(items);
    }

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
            case "ingredients" -> giveAllIngredients(player);
            case "all_gear" -> giveAllGear(player);
            case "all_armor" -> giveAllArmor(player);
            case "all" -> giveAllItems(player);
            default -> {
                try {
                    CustomItem item = CustomItem.valueOf(args[0].toUpperCase());
                    giveItem(player, item.getItem());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Component.text("Unbekannter Item-Key. ", NamedTextColor.RED));
                    sendUsage(player, alias);
                }
            }
        }

        return true;
    }

    private void sendUsage(Player player, String label) {
        player.sendMessage(Component.text("§cVerwendung: /" + label + " <key>").color(NamedTextColor.RED));
        player.sendMessage(Component.text("§7Verfügbare Subcommands:"));
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
        Arrays.stream(CustomItem.values()).filter(customItem ->
                customItem.getCustomItemType().equals(CustomItem.CustomItemType.WEAPON)
                        || customItem.getCustomItemType().equals(CustomItem.CustomItemType.TOOL)).forEach(gear ->
                giveItem(player, gear.getItem()));
    }

    private void giveAllArmor(Player player) {
        Arrays.stream(CustomItem.values()).filter(customItem ->
                customItem.getCustomItemType().equals(CustomItem.CustomItemType.ARMOR)).forEach(armor ->
                giveItem(player, armor.getItem()));
    }

    private void giveAllIngredients(Player player) {
        Arrays.stream(CustomItem.values()).filter(customItem ->
                customItem.getCustomItemType().equals(CustomItem.CustomItemType.INGREDIENT)).forEach(ingredient ->
                giveItem(player, ingredient.getItem()));
    }

    private void giveAllItems(Player player) {

        giveAllGear(player);
        giveAllArmor(player);
        giveAllIngredients(player);

        giveItem(player, SoulHeartCrystal.createSoulHeartCrystal());
    }
}
