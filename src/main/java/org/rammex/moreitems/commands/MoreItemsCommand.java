package org.rammex.moreitems.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rammex.moreitems.Moreitems;

import java.util.ArrayList;
import java.util.List;

import static org.rammex.moreitems.utils.ColorUtil.hex;

public class MoreItemsCommand implements CommandExecutor {
    private final Moreitems plugin;

    public MoreItemsCommand(Moreitems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        Player player = (Player) sender;
        if(args.length == 0) {
            displayHelp(player);
            return false;
        }
        switch (args[0]) {
            case "reload":
                plugin.reloadItemsConfig();
                player.sendMessage(hex("#57E2A9Items configuration reloaded."));
                break;
            case "give":
                if (args.length < 4) {
                    player.sendMessage(hex("#57E2A9Usage: /moreitems give <itemname> <level> <player> <quantity>"));
                } else {
                    handleGiveCommand(args, player);
                }
                break;
        }

        return false;
    }

    private void displayHelp(Player player){
        player.sendMessage(hex("&7-----------------------------"));
        player.sendMessage(hex("#57E2A9/moreitems - Return the command of the plugin"));
        player.sendMessage(hex("#57E2A9/moreitems give <itemname> <level> <player> <quantity>  - Give an item to a player"));
        player.sendMessage(hex("#57E2A9/moreitems reload - Reload the plugin"));
        player.sendMessage(hex("&7-----------------------------"));
    }

    private void handleGiveCommand(String[] args, Player player) {
        String itemName = args[1];
        int level = Integer.parseInt(args[2]);
        Player targetPlayer = Bukkit.getPlayer(args[3]);
        int quantity = Integer.parseInt(args[4]);

        if (itemName.equalsIgnoreCase("entonnoir") && targetPlayer != null) {
            giveFunnel(targetPlayer, level, quantity);
            player.sendMessage(hex("#57E2A9Gave " + quantity + " level " + level + " funnel(s) to " + targetPlayer.getName() + "."));
        }
    }

    private void giveFunnel(Player player, int level, int quantity) {
        String funnelName = plugin.getItemConf().getString("entonnoir.name");
        String funnelDescription = plugin.getItemConf().getString("entonnoir.description");
        int funnelRadius = plugin.getItemConf().getInt("entonnoir.level." + level);

        ItemStack funnel = new ItemStack(Material.HOPPER, quantity);

        ItemMeta meta = funnel.getItemMeta();
        meta.setDisplayName(hex(funnelName));
        List<String> lore = new ArrayList<>();
        lore.add(hex(funnelDescription));
        lore.add(hex("Rayon: " + funnelRadius + " blocs"));
        meta.setLore(lore);
        funnel.setItemMeta(meta);

        player.getInventory().addItem(funnel);
    }
}