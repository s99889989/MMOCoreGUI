package com.daxton.mmocoregui.controller;

import com.daxton.mmocoregui.MMOCoreGUI;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.gui.attributes.AttributesGUI;
import com.daxton.mmocoregui.gui.friends.FriendsGUI;
import com.daxton.mmocoregui.gui.guild.GuildCreationGUI;
import com.daxton.mmocoregui.gui.guild.GuildViewGUI;
import com.daxton.mmocoregui.gui.party.PartyCreationGUI;
import com.daxton.mmocoregui.gui.party.PartyViewGUI;
import com.daxton.mmocoregui.gui.quests.QuestsGUI;
import com.daxton.mmocoregui.gui.select_class.SelectClassGUI;
import com.daxton.mmocoregui.gui.skills.SkillsGUI;
import com.daxton.mmocoregui.gui.waypoints.WaypointsGUI;
import com.daxton.unrealcore.application.UnrealCoreAPI;

import com.daxton.unrealcore.application.base.PluginUtil;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;

import org.bukkit.entity.Player;

public class GUIController {

    //讀取設定
    public static void load(){

        //建立設定檔
        createConfig();

    }

    //重新讀取設定
    public static void reload(){
        load();
    }


    //打開GUI
    public static void openGUI(Player player, MMOCoreGUIType mmoCoreGUIType){

        UnrealCoreGUI unrealCoreGUI = null;

        switch (mmoCoreGUIType){
            case ATTRIBUTES:
                unrealCoreGUI = new AttributesGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/Attributes.yml"), player);
                break;
            case FRIENDS:
                unrealCoreGUI = new FriendsGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/Friends.yml"), player);
                break;
            case PARTY_CREATION:
                unrealCoreGUI = new PartyCreationGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/PartyCreation.yml"), player);
                break;
            case PARTY_VIEW:
                unrealCoreGUI = new PartyViewGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/PartyView.yml"), player);
                break;
            case GUILD_CREATION:
                unrealCoreGUI = new GuildCreationGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/GuildCreation.yml"), player);
                break;
            case GUILD_VIEW:
                unrealCoreGUI = new GuildViewGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/GuildView.yml"), player);
                break;
            case QUESTS:
                unrealCoreGUI = new QuestsGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/Quests.yml"), player);
                break;
            case SELECT_CLASS:
                unrealCoreGUI = new SelectClassGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/SelectClass.yml"), player);
                break;
            case SKILLS:
                unrealCoreGUI = new SkillsGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/Skills.yml"), player);
                break;
            case WAYPOINTS:
                unrealCoreGUI = new WaypointsGUI(mmoCoreGUIType.name(), MMOCoreGUI.unrealCorePlugin.getYmlFile("gui/Waypoints.yml"), player);
                break;
        }

        if(unrealCoreGUI != null){
            UnrealCoreAPI.inst(player).getGUIHelper().openCoreGUI(unrealCoreGUI);
        }

    }

    //建立設定檔
    public static void createConfig(){
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/Attributes.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/Friends.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/GuildCreation.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/GuildView.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/PartyCreation.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/PartyView.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/Quests.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/SelectClass.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/Skills.yml", false);
        PluginUtil.resourceCopy(MMOCoreGUI.unrealCorePlugin.getJavaPlugin(), "gui/Waypoints.yml", false);
    }



}
