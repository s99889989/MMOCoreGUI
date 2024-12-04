package com.daxton.mmocoregui.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabMain implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> commandList = new ArrayList<>();

        if (args.length == 1){
            String[] subcommandsOP = {"reload", "gui"};
            String[] subcommands = {"gui"};
            commandList = getCommandList(sender, subcommandsOP, subcommands);
        }

        if (args.length == 2){
            if(args[0].equalsIgnoreCase("gui")){
                String[] subcommandsOP = {"select_class", "attributes", "skills", "friends", "party", "guild", "quests", "waypoints"};
                String[] subcommands = {"select_class", "attributes", "skills", "friends", "party", "guild", "quests", "waypoints"};
                commandList = getCommandList(sender, subcommandsOP, subcommands);
            }
        }

        if (args.length == 3){
            if(args[0].equalsIgnoreCase("gui")){
                String[] subcommands = {"select_class", "attributes", "skills", "friends", "party", "guild", "quests", "waypoints"};
                if(equalsIgnoreCase(subcommands, args[1])){
                    commandList = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                }
            }
        }

        return commandList;
    }

    public static boolean equalsIgnoreCase(String[] subcommands, String key){
        for (String command : subcommands) {
            if (command.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    //判斷管理和玩家來回傳使用指令
    public static List<String> getCommandList(CommandSender sender, String[] opCommand, String[] playerCommand){
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(!player.isOp()){
                return Arrays.stream(playerCommand).collect(Collectors.toList());
            }
        }
        return Arrays.stream(opCommand).collect(Collectors.toList());
    }

}
