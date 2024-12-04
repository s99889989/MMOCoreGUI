package com.daxton.mmocoregui.command;

import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMain implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){
            Player player = (Player) sender;

            if (args.length == 2){
                if(args[0].equalsIgnoreCase("gui")){
                    if(args[1].equalsIgnoreCase("select_class")){
                        GUIController.openGUI(player, MMOCoreGUIType.SELECT_CLASS);
                    }
                    if(args[1].equalsIgnoreCase("attributes")){
                        GUIController.openGUI(player, MMOCoreGUIType.ATTRIBUTES);
                    }
                    if(args[1].equalsIgnoreCase("skills")){
                        GUIController.openGUI(player, MMOCoreGUIType.SKILLS);
                    }
                    if(args[1].equalsIgnoreCase("friends")){
                        GUIController.openGUI(player, MMOCoreGUIType.FRIENDS);
                    }
                    if(args[1].equalsIgnoreCase("party")){
                        PlayerData playerData = PlayerData.get(player);
                        if (playerData.getParty() != null){
                            GUIController.openGUI(player, MMOCoreGUIType.PARTY_VIEW);
                        }else {
                            GUIController.openGUI(player, MMOCoreGUIType.PARTY_CREATION);
                        }
                    }
                    if(args[1].equalsIgnoreCase("guild")){
                        PlayerData playerData = PlayerData.get(player);
                        if (playerData.inGuild()){
                            GUIController.openGUI(player, MMOCoreGUIType.GUILD_VIEW);
                        }else {
                            GUIController.openGUI(player, MMOCoreGUIType.GUILD_CREATION);
                        }
                    }
                    if(args[1].equalsIgnoreCase("quests")){
                        GUIController.openGUI(player, MMOCoreGUIType.QUESTS);
                    }
                    if(args[1].equalsIgnoreCase("waypoints")){
                        GUIController.openGUI(player, MMOCoreGUIType.WAYPOINTS);
                    }
                }
            }
        }else {
            if (args.length == 3){
                Player player = Bukkit.getPlayer(args[2]);
                if(player == null){
                    return true;
                }
                if(args[0].equalsIgnoreCase("gui")){
                    if(args[1].equalsIgnoreCase("select_class")){
                        GUIController.openGUI(player, MMOCoreGUIType.SELECT_CLASS);
                    }
                    if(args[1].equalsIgnoreCase("attributes")){
                        GUIController.openGUI(player, MMOCoreGUIType.ATTRIBUTES);
                    }
                    if(args[1].equalsIgnoreCase("skills")){
                        GUIController.openGUI(player, MMOCoreGUIType.SKILLS);
                    }
                    if(args[1].equalsIgnoreCase("friends")){
                        GUIController.openGUI(player, MMOCoreGUIType.FRIENDS);
                    }
                    if(args[1].equalsIgnoreCase("party")){
                        PlayerData playerData = PlayerData.get(player);
                        if (playerData.getParty() != null){
                            GUIController.openGUI(player, MMOCoreGUIType.PARTY_VIEW);
                        }else {
                            GUIController.openGUI(player, MMOCoreGUIType.PARTY_CREATION);
                        }
                    }
                    if(args[1].equalsIgnoreCase("guild")){
                        PlayerData playerData = PlayerData.get(player);
                        if (playerData.inGuild()){
                            GUIController.openGUI(player, MMOCoreGUIType.GUILD_VIEW);
                        }else {
                            GUIController.openGUI(player, MMOCoreGUIType.GUILD_CREATION);
                        }
                    }
                    if(args[1].equalsIgnoreCase("quests")){
                        GUIController.openGUI(player, MMOCoreGUIType.QUESTS);
                    }
                    if(args[1].equalsIgnoreCase("waypoints")){
                        GUIController.openGUI(player, MMOCoreGUIType.WAYPOINTS);
                    }
                }
            }
        }

        return true;
    }

}
