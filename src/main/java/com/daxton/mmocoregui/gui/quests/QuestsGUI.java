package com.daxton.mmocoregui.gui.quests;

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
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.experience.Profession;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class QuestsGUI extends UnrealCoreGUI {

    private final List<Quest> quests = new ArrayList<>(MMOCore.plugin.questManager.getAll());

    private final QuestData questData = new QuestData();

    private Quest currentQuest;

    public QuestsGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);



        ContainerModule questsListContainer = (ContainerModule) getModule("QuestsListContainer");
        ButtonModule questsButton2 = (ButtonModule) questsListContainer.getModule("QuestsButton").copy();
        questsListContainer.removeModule("QuestsButton");

        int height = questsButton2.getHeight();
        int startY = questsButton2.getY();
        int maxHeight = 0;
        for(int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            ButtonModule questsButton = (ButtonModule) questsButton2.copy();
            questsButton.setText(quest.getName());
            questsButton.setModuleID("QuestsButton" + i);
            int y = startY + (height+5) * i;
            questsButton.setY(y);
            questsButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                    currentQuest = quest;
                    displayQuestsContent(player, quest);

                }
            });
            questsListContainer.addModule(questsButton);
            maxHeight += height + 5;
        }
        questsListContainer.setActualHeight(maxHeight);

        PlayerData playerData = PlayerData.get(player);

        ButtonModule startQuestButton = (ButtonModule) getModule("StartQuestButton");
        startQuestButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                if(currentQuest != null){
                    Quest quest = currentQuest;
                    // Check for level requirements.
                    int level;
                    if (playerData.getLevel() < (level = quest.getLevelRestriction(null))) {
                        ConfigMessage.fromKey("quest-level-restriction", "level", "Lvl", "count", "" + level).send(player);
                        return;
                    }

                    for (Profession profession : quest.getLevelRestrictions())
                        if (playerData.getCollectionSkills().getLevel(profession) < (level = quest.getLevelRestriction(profession))) {
                            ConfigMessage.fromKey("quest-level-restriction", "level", profession.getName() + " Lvl", "count", "" + level)
                                    .send(player);
                            return;
                        }

                    if (playerData.getQuestData().hasFinished(quest)) {

                        // If the player has already finished this quest, he can't start it again
                        if (!quest.isRedoable()) {
                            ConfigMessage.fromKey("cant-redo-quest").send(player);
                            return;
                        }

                        // Has the player waited long enough
                        if (!playerData.getQuestData().checkCooldownAvailability(quest)) {
                            ConfigMessage.fromKey("quest-cooldown", "delay", new DelayFormat(2).format(playerData.getQuestData().getDelayFeft(quest))).send(player);
                            return;
                        }
                    }

                    // Eventually start the quest
                    ConfigMessage.fromKey("start-quest", "quest", quest.getName()).send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.START_QUEST).playTo(player);
                    playerData.getQuestData().start(quest);
                }

            }
        });


        ButtonModule cancelQuestButton = (ButtonModule) getModule("CancelQuestButton");
        cancelQuestButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
           if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
               if (playerData.getQuestData().hasCurrent(currentQuest)) {
                   playerData.getQuestData().start(null);
                   MMOCore.plugin.soundManager.getSound(SoundEvent.CANCEL_QUEST).playTo(player);
                   ConfigMessage.fromKey("cancel-quest").send(player);
               }
           }
        });
//        quests.forEach(quest -> {
//
//        });

    }

    public void displayQuestsContent(Player player, Quest quest) {
        TextModule textModule = (TextModule) getModule("QuestsContainer", "QuestsText");
        textModule.setText(questData.display(player, quest));
        upDate();
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
