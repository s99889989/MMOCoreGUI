package com.daxton.mmocoregui.gui.select_class;

import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.display.TextModule;
import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.gui.ClassSelect;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SelectClassGUI extends UnrealCoreGUI {

    private final String id;

    private String name;

    private List<SelectClassGUI.ClassItem> classItemList = new ArrayList<>();
    //職業選擇按鈕
    private ButtonModule selectClassButton;

    private int selectedClass = 0;

    public SelectClassGUI(String guiName, FileConfiguration fileConfiguration,Player player) {
        super(guiName, fileConfiguration);

        id = "class-select";
        ClassSelect classSelect = InventoryManager.CLASS_SELECT;
        FileConfiguration config = classSelect.getConfig();
        this.name = config.getString("name");
        Validate.notNull(name, "Name must not be null");


        TextModule titleModule = (TextModule) getModule("TitleText");
        titleModule.setText(this.name);

        setClassItem(config);

        ButtonModule selectClassButton = (ButtonModule) getModule("SelectClassButton");
        if(selectClassButton != null) {
            this.selectClassButton = selectClassButton.copy();
            removeModule("SelectClassButton");
        }

        if(this.selectClassButton != null) {

            int height = this.selectClassButton.getHeight();
            int startY = this.selectClassButton.getY();
            for(int i = 0; i < this.classItemList.size(); i++) {

                SelectClassGUI.ClassItem classItem = this.classItemList.get(i);

                ButtonModule selectClassButton2 = this.selectClassButton.copy();
                if(i == 0){
                    selectClassButton2.setColor(this.selectClassButton.getClickColor());
                }
                selectClassButton2.setText(classItem.name);
                selectClassButton2.setModuleID("Class" + i);
                int y = startY + (height+5) * i;
                selectClassButton2.setY(y);
                addModule(selectClassButton2);

            }


        }

        setSelectedClass(0, false);

    }

    @Override
    public void opening() {
        for(int i = 0; i < this.classItemList.size(); i++) {
            ButtonModule selectClassButton2 = (ButtonModule) getModule("Class" + i);
            int finalI = i;
            selectClassButton2.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                    setSelectedClass(finalI, true);
                }
            });
        }

        ButtonModule confirmClassButton = (ButtonModule) getModule("ConfirmClassButton");

        confirmClassButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                SelectClassGUI.ClassItem classItem = this.classItemList.get(this.selectedClass);
                if(classItem == null) {return;}
                PlayerClass profess = classItem.playerClass;
                Player player = getPlayer();
                PlayerData playerData = PlayerData.get(player);

                if (playerData.getClassPoints() < 1) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(player);
                    ConfigMessage.fromKey("cant-choose-new-class").send(player);
                    return;
                }

                if (profess.hasOption(ClassOption.NEEDS_PERMISSION) && !player.hasPermission("mmocore.class." + profess.getId().toLowerCase())) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(player);
                    ConfigMessage.fromKey("no-permission-for-class").send(player);
                    return;
                }

                if (profess.equals(playerData.getProfess())) {
                    MMOCore.plugin.soundManager.getSound(SoundEvent.CANT_SELECT_CLASS).playTo(player);
                    ConfigMessage.fromKey("already-on-class", "class", profess.getName()).send(player);
                    return;
                }
                boolean subclass= true;
                if (subclass) {
                    playerData.setClass(profess);
                } else {
                    (playerData.hasSavedClass(profess) ? playerData.getClassInfo(profess)
                            : new SavedClassInformation(MMOCore.plugin.playerDataManager.getDefaultData()))
                            .load(profess, playerData);
                }
                ConfigMessage.fromKey("class-select", "class", profess.getName()).send(player);
                MMOCore.plugin.soundManager.getSound(SoundEvent.SELECT_CLASS).playTo(player);

                playerData.setClass(profess);
            }
        });


    }

    public void setSelectedClass(int selectedClass, boolean update) {
        SelectClassGUI.ClassItem classItem = this.classItemList.get(selectedClass);
        if(classItem == null) {return;}
        this.selectedClass = selectedClass;
        TextModule directionsModule = (TextModule) getModule("DirectionsContainer", "DirectionsText");


        List<String> lore = new ArrayList<>(classItem.lore);

        int index = lore.indexOf("{lore}");
        if (index >= 0) {
            lore.remove(index);
            for (int j = 0; j < classItem.playerClass.getDescription().size(); j++)
                lore.add(index + j, classItem.playerClass.getDescription().get(j));
        }

        index = lore.indexOf("{attribute-lore}");
        if (index >= 0) {
            lore.remove(index);
            for (int j = 0; j < classItem.playerClass.getAttributeDescription().size(); j++)
                lore.add(index + j, classItem.playerClass.getAttributeDescription().get(j));
        }
        directionsModule.setText(lore);

        if(update) {

            for(int i = 0; i < this.classItemList.size(); i++) {

                ButtonModule selectClassButton2 = (ButtonModule) getModule("Class" + i);

                if(i == selectedClass){
                    selectClassButton2.setColor(this.selectClassButton.getClickColor());
                }else {
                    selectClassButton2.setColor(this.selectClassButton.getColor());
                }

                addModule(selectClassButton2);

            }

            addModule(directionsModule);
            upDate();
        }
    }

    public void setClassItem(FileConfiguration config){
        if (config.contains("items")) {
            Validate.notNull(config.getConfigurationSection("items"), "Could not load item list");
            for (String key : config.getConfigurationSection("items").getKeys(false))
                try {
                    ConfigurationSection section = config.getConfigurationSection("items." + key);
                    Validate.notNull(section, "Could not load config");
                    loadInventoryItem(section);
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load item '" + key + "' from inventory '" + id + "': " + exception.getMessage());
                }
        }
    }

    private void loadInventoryItem(ConfigurationSection config) {
        String function = config.contains("function") ? config.getString("function").toLowerCase() : "";

        SelectClassGUI.ClassItem classItem = load(function, config);
        classItemList.add(classItem);
//        MMOCoreGUI.unrealCorePlugin.sendLogger("Name: "+classItem.name);
    }

    public SelectClassGUI.ClassItem load(String function, ConfigurationSection config) {
        return function.startsWith("class") ? new SelectClassGUI.ClassItem(config) : null;
    }

    public class ClassItem{
        private final String name;
        private final List<String> lore;
        private final PlayerClass playerClass;

        public ClassItem(ConfigurationSection config) {

            Validate.isTrue(config.getString("function").length() > 6, "Couldn't find the class associated to: " + config.getString("function"));
            String classId = UtilityMethods.enumName(config.getString("function").substring(6));
            this.playerClass = MMOCore.plugin.classManager.getOrThrow(classId);
            this.name = config.getString("name").replace("&", "§");
            this.lore = config.getStringList("lore");


        }

    }




    @Override
    public void close() {

    }



}
