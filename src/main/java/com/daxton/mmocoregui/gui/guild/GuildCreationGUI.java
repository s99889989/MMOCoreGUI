package com.daxton.mmocoregui.gui.guild;

import com.daxton.mmocoregui.MMOCoreGUI;
import com.daxton.mmocoregui.application.GUIUtil;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.input.InputModule;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class GuildCreationGUI extends UnrealCoreGUI {

    private String input;

    public GuildCreationGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);


        InputModule guildInput = (InputModule) getModule("GuildInput");

        guildInput.onInputChange((inputModule, text, finish) -> {

            input = text;
        });

        ButtonModule guildCreationButton = (ButtonModule) getModule("GuildCreationButton");

        guildCreationButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                PlayerData playerData = PlayerData.get(player);
                if(MMOCore.plugin.nativeGuildManager.getConfig().shouldUppercaseTags())
                    input = input.toUpperCase();
                if(check(player, input, MMOCore.plugin.nativeGuildManager.getConfig().getTagRules())) {
                    String tag = input;
                    String name = input;
                    if(check(player, name, MMOCore.plugin.nativeGuildManager.getConfig().getNameRules())) {
                        MMOCore.plugin.nativeGuildManager.newRegisteredGuild(playerData.getUniqueId(), name, tag);
                        MMOCore.plugin.nativeGuildManager.getGuild(tag.toLowerCase()).addMember(playerData.getUniqueId());

                        GUIController.openGUI(player, MMOCoreGUIType.GUILD_VIEW);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                }
            }
        });

    }

    public boolean check(Player player, String input, GuildDataManager.GuildConfiguration.NamingRules rules) {
        String reason;

        if(input.length() <= rules.getMax() && input.length() >= rules.getMin())
            if(input.matches(rules.getRegex()))
                if(!MMOCore.plugin.nativeGuildManager.isRegistered(input))
                    return true;
                else
                    reason = ConfigMessage.fromKey("guild-creation.reasons.already-taken").asLine();
            else
                reason = ConfigMessage.fromKey("guild-creation.reasons.invalid-characters").asLine();
        else
            reason = ConfigMessage.fromKey("guild-creation.reasons.invalid-length", "min", "" + rules.getMin(), "max", "" + rules.getMax()).asLine();

        ConfigMessage.fromKey("guild-creation.failed", "reason", reason).send(player);
        return false;
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
