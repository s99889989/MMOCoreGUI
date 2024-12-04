package com.daxton.mmocoregui.gui.attributes;

import com.daxton.mmocoregui.application.GUIUtil;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.control.ContainerModule;
import com.daxton.unrealcore.display.content.module.display.TextModule;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.gui.AttributeView;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AttributesGUI extends UnrealCoreGUI {

    private final String id;

    private String name;

    private List<AttributeItem> attributeItems = new ArrayList<>();

    //說明表
    private List<String> attributeList = new ArrayList<>();
    //點數字串
    private String pointString = "";

    private List<String> attributePoints = new ArrayList<>();

    private ContainerModule attributesContainer;

    public AttributesGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);

        this.id = "class-select";

        AttributeView attributeView = InventoryManager.ATTRIBUTE_VIEW;
        FileConfiguration config = attributeView.getConfig();
        this.name = config.getString("name");
        setClassItem(config, player);

        TextModule attributesTextModule = (TextModule) getModule("ContentContainer", "AttributesText");
        this.attributeList = new ArrayList<>(attributesTextModule.getText());

        ContainerModule attributesContainer = (ContainerModule) getModule("AttributesContainer");
        if(attributesContainer != null) {
            this.attributesContainer = attributesContainer.copy();
            removeModule("AttributesContainer");
        }

        TextModule attributesPointText = (TextModule) getModule("AttributesPointContainer" ,"AttributesPointText");
        this.pointString = attributesPointText.getText(0);

        AttributeItem attributeItem0 = this.attributeItems.get(0);
        attributesPointText.setText(attributeItem0.getPlaceholders().apply(player, this.pointString));

        int height = this.attributesContainer.getHeight();
        int startY = this.attributesContainer.getY();
        for(int i = 0; i < this.attributeItems.size(); i++) {
            AttributeItem attributeItem = this.attributeItems.get(i);
            ContainerModule attributesContainer2 = this.attributesContainer.copy();
            attributesContainer2.setModuleID("Attributes"+attributeItem.getAttribute().getId());
            TextModule attributesTextModule2 = (TextModule) attributesContainer2.getModule("AttributesText");
            this.attributePoints.add(attributesTextModule2.getText(0));
            List<String> stringList = new ArrayList<>();
            attributesTextModule2.getText().forEach(s -> {
                stringList.add(attributeItem.getPlaceholders().apply(player, s));
            });
            attributesTextModule2.setText(stringList);

            int y = startY + (height+5) * i;
            attributesContainer2.setY(y);
            addModule(attributesContainer2);

        }

        List<String> stringList = new ArrayList<>();
        this.attributeList.forEach(s -> {

            for(AttributeItem attributeItem : this.attributeItems) {
                s = attributeItem.getPlaceholders().apply(player, s);
            }
            stringList.add(s);

        });
        attributesTextModule.setText(stringList);
    }

    public void selectItem(Player player, AttributeItem attributeItem, int index) {

        PlayerData playerData = PlayerData.get(player);
        PlayerAttribute attribute = attributeItem.getAttribute();

        if (playerData.getAttributePoints() < 1) {
            ConfigMessage.fromKey("not-attribute-point").send(player);
            MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
            return;
        }

        PlayerAttributes.AttributeInstance ins = playerData.getAttributes().getInstance(attribute);
        if (attribute.hasMax() && ins.getBase() >= attribute.getMax()) {
            ConfigMessage.fromKey("attribute-max-points-hit").send(player);
            MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
            return;
        }

        // 花費的積分數量
        final boolean shiftClick = false;
        int pointsSpent = shiftClick ? attributeItem.getShiftCost() : 1;
        if (attribute.hasMax())
            pointsSpent = Math.min(pointsSpent, attribute.getMax() - ins.getBase());

        if (shiftClick && playerData.getAttributePoints() < pointsSpent) {
            ConfigMessage.fromKey("not-attribute-point-shift", "shift_points", String.valueOf(pointsSpent)).send(player);
            MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
            return;
        }
//        MMOCoreGUI.unrealCorePlugin.sendLogger("Point: "+pointsSpent);
        ins.addBase(pointsSpent);
        playerData.giveAttributePoints(-pointsSpent);

        // 根據需要多次應用 exp 表
        while (pointsSpent-- > 0){
            attribute.updateAdvancement(playerData, ins.getBase());
        }

        ConfigMessage.fromKey("attribute-level-up", "attribute", attribute.getName(), "level", String.valueOf(ins.getBase())).send(player);
        MMOCore.plugin.soundManager.getSound(SoundEvent.LEVEL_ATTRIBUTE).playTo(getPlayer());

        this.attributeItems.forEach(AttributeItem::rePlaceholders);



        String string = this.attributePoints.get(index);
        ContainerModule attributesContainer = (ContainerModule) getModule("Attributes"+attribute.getId());
        TextModule attributesTextModule2 = (TextModule) attributesContainer.getModule("AttributesText");

        attributesTextModule2.setText(attributeItem.getPlaceholders().apply(player, string));



        TextModule attributesPointText = (TextModule) getModule("AttributesPointContainer" ,"AttributesPointText");
        attributesPointText.setText(attributeItem.getPlaceholders().apply(player, this.pointString));

        addModule(attributesPointText);

        addModule(attributesTextModule2);
        upDate();

    }

    public void setClassItem(FileConfiguration config, Player player){
        if (config.contains("items")) {
            Validate.notNull(config.getConfigurationSection("items"), "Could not load item list");
            for (String key : config.getConfigurationSection("items").getKeys(false))
                try {
                    ConfigurationSection section = config.getConfigurationSection("items." + key);
                    Validate.notNull(section, "Could not load config");
                    loadInventoryItem(section, player);
                } catch (IllegalArgumentException exception) {
                    MMOCore.log(Level.WARNING, "Could not load item '" + key + "' from inventory '" + this.id + "': " + exception.getMessage());
                }
        }
    }

    @Override
    public void opening() {
        for(int i = 0; i < this.attributeItems.size(); i++) {
            AttributeItem attributeItem = this.attributeItems.get(i);
            ContainerModule attributesContainer = (ContainerModule) getModule("Attributes"+attributeItem.getAttribute().getId());
            ButtonModule attributesButtonModule = (ButtonModule) attributesContainer.getModule("AttributesButton");
            if(attributesButtonModule != null){
                int finalI = i;
                attributesButtonModule.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {

                    if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){

                        selectItem(getPlayer(), attributeItem, finalI);
                    }
                });
            }

        }
    }

    private void loadInventoryItem(ConfigurationSection config, Player player) {
        String function = config.contains("function") ? config.getString("function").toLowerCase() : "";
        if(function.startsWith("attribute_")){
            AttributeItem attributeItem = new AttributeItem(function, config, player);
            attributeItems.add(attributeItem);

//            MMOCoreGUI.unrealCorePlugin.sendLogger("屬性: "+attributeItem.getAttribute().getName());
        }


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
