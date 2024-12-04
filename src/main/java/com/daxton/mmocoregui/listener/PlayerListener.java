package com.daxton.mmocoregui.listener;

import com.daxton.mmocoregui.MMOCoreGUI;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.unrealcore.common.event.PlayerKeyBoardEvent;
import com.daxton.unrealcore.display.event.gui.module.PlayerButtonEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {

//    //按下按鍵
//    @EventHandler
//    public void onPlayerKeyBoard(PlayerKeyBoardEvent event){
//        Player player = event.getPlayer();
//        String keyName = event.getKeyName();
//        boolean inputNow = event.isInputNow();
//        int keyAction = event.getKeyAction();
//        if(!inputNow){
//            if(keyAction == 1) {
//                if(keyName.equals("G")){
//                    PlayerData playerData = PlayerData.get(player);
//                    if (playerData.inGuild())
//                        GUIController.openGUI(player, MMOCoreGUIType.GUILD_VIEW);
//                    else
//                        GUIController.openGUI(player, MMOCoreGUIType.GUILD_CREATION);
//                }
//                if(keyName.equals("H")){
//
//                    PlayerData playerData = PlayerData.get(player);
//
//                    if (playerData.inGuild())
//                        InventoryManager.GUILD_VIEW.newInventory(playerData).open();
//                    else
//                        InventoryManager.GUILD_CREATION.newInventory(playerData).open();
//                }
//            }
//        }
//    }

//    @EventHandler//當玩家按下按鈕
//    public void onButton(PlayerButtonEvent event) {
//        MMOCoreGUI.unrealCorePlugin.sendLogger(event.getPath()+" : "+event.getModuleID());
//    }

}
