package com.daxton.mmocoregui.gui.waypoints;

import com.daxton.mmocoregui.MMOCoreGUI;
import com.daxton.mmocoregui.application.GUIUtil;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.control.ContainerModule;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointPath;
import net.Indyuce.mmocore.waypoint.WaypointPathCalculation;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaypointsGUI extends UnrealCoreGUI {

    private final List<Waypoint> waypointList = new ArrayList<>(MMOCore.plugin.waypointManager.getAll());

    public WaypointsGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);

        ContainerModule pointListContainer = (ContainerModule) getModule("PointListContainer");

        ButtonModule pointButton2 = (ButtonModule) pointListContainer.getModule("PointButton").copy();
        pointListContainer.removeModule("PointButton");
        int height = pointButton2.getHeight();
        int startY = pointButton2.getY();
        int width = pointButton2.getWidth();
        int startX = pointButton2.getX();

        int x = 0;
        int y = 0;
        for(int i = 0; i < waypointList.size(); i++) {
            Waypoint waypoint = waypointList.get(i);
            ButtonModule questsButton = pointButton2.copy();
            questsButton.setText(waypoint.getName());
            questsButton.setModuleID("QuestsButton" + i);
            int setX = startX + (width+5) * x;
            int setY = startY + (height+5) * y;
            questsButton.setX(setX);
            questsButton.setY(setY);

            x++;
            int ww = (width+5) * (x+1) - 5;

            if(ww > pointListContainer.getWidth()){
//                MMOCoreGUI.unrealCorePlugin.sendLogger(ww+" : "+pointListContainer.getWidth());
                y++;
                x = 0;
            }

            questsButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                    PlayerData playerData = PlayerData.get(player);
                    Map<Waypoint, WaypointPath> paths = new WaypointPathCalculation(playerData).run(waypoint).getPaths();
                    DecimalFormat decimal = MythicLib.plugin.getMMOConfig().decimal;


                    if (!playerData.hasWaypoint(waypoint)) {
                        ConfigMessage.fromKey("not-unlocked-waypoint").send(player);
                        return;
                    }

//                    // Cannot teleport to current waypoint
//                    if (waypoint.equals(current)) {
//                        ConfigMessage.fromKey("standing-on-waypoint").send(player);
//                        return;
//                    }

                    // No access to that waypoint
                    if (paths.get(waypoint) == null) {
                        ConfigMessage.fromKey("cannot-teleport-to").send(player);
                        return;
                    }

                    // Stellium cost
                    double withdraw = paths.get(waypoint).getCost();
                    double left = withdraw - playerData.getStellium();
                    if (left > 0) {
                        ConfigMessage.fromKey("not-enough-stellium", "more", decimal.format(left)).send(player);
                        return;
                    }

                    if (playerData.getActivityTimeOut(PlayerActivity.USE_WAYPOINT) > 0)
                        return;

                    player.closeInventory();
                    playerData.warp(waypoint, withdraw);
                }
            });

            pointListContainer.addModule(questsButton);
        }
        int maxHeight = y*(height+5)-5;
        pointListContainer.setActualHeight(maxHeight);

    }

    @Override
    public void buttonClick(ButtonModule buttonModule, MouseButtonType button, MouseActionType action) {
        if(button == MouseButtonType.Left && action == MouseActionType.Off){
            //打開GUI
            String toMenu = getFileConfiguration().getString(buttonModule.getFilePath()+".ToMenu");
            GUIUtil.toMenu(getPlayer(), toMenu);
        }
    }

}
