package com.daxton.mmocoregui.application;

import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;

public class GUIUtil {

    public static void toMenu(Player player, String toMenu) {
        if(toMenu != null){
            PlayerData playerData = PlayerData.get(player);
            if(toMenu.equalsIgnoreCase("Party")){

                if (playerData.getParty() != null){
                    toMenu = "PARTY_VIEW";
                }else {
                    toMenu = "PARTY_CREATION";
                }
            }
            if(toMenu.equalsIgnoreCase("GUILD")){
                if(playerData.inGuild()){
                    toMenu = "GUILD_VIEW";
                }else {
                    toMenu = "GUILD_CREATION";
                }
            }
            MMOCoreGUIType mmoCoreGUIType = MMOCoreGUIType.fromString(toMenu);
            GUIController.openGUI(player, mmoCoreGUIType);
        }
    }

}
