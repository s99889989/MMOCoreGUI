package com.daxton.mmocoregui.gui.party;

import com.daxton.mmocoregui.application.GUIUtil;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.mmocoregui.gui.friends.FriendData;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.control.ContainerModule;
import com.daxton.unrealcore.display.content.module.display.ImageModule;
import com.daxton.unrealcore.display.content.module.display.TextModule;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.party.provided.Party;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyViewGUI extends UnrealCoreGUI {

    private ContainerModule playerCardNotParty2;
    private ContainerModule playerCardParty2;

    public PartyViewGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");
        this.playerCardNotParty2 = (ContainerModule) playerContainer.getModule("PlayerCardNotParty").copy();
        playerContainer.removeModule("PlayerCardNotParty");
        this.playerCardParty2 = (ContainerModule) playerContainer.getModule("PlayerCardParty").copy();
        playerContainer.removeModule("PlayerCardParty");

        //顯示所有隊友
        displayParty(player);

        //組隊玩家列表
        ButtonModule partyButton = (ButtonModule) getModule("OptionsContainer", "PartyButton");
        partyButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                playerContainer.clearModule();
                displayParty(getPlayer());
                upDate();
            }
        });

        //邀請玩家列表
        ButtonModule playerButton = (ButtonModule) getModule("OptionsContainer", "PlayerButton");
        playerButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                playerContainer.clearModule();
                displayPlayers(getPlayer());
                upDate();
            }
        });

        //離開隊伍
        ButtonModule leaveButton = (ButtonModule) getModule("OptionsContainer", "LeaveButton");
        leaveButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                PlayerData playerData = PlayerData.get(player);
                Party party = (Party) playerData.getParty();
                if(party != null){
                    party.removeMember(playerData);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    GUIController.openGUI(player, MMOCoreGUIType.PARTY_CREATION);
                }

            }
        });

    }


    //顯示所有隊友
    public void displayParty(Player player){
        PlayerData playerData = PlayerData.get(player);

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");

        int width = this.playerCardParty2.getWidth();
        int height = this.playerCardParty2.getHeight();
        int startX = this.playerCardParty2.getX();
        int startY = this.playerCardParty2.getY();

        int x = 0;
        int y = 0;
        Party party = (Party) playerData.getParty();
        if(party == null){
            return;
        }

        for(int i = 0; i < party.getMembers().size(); i++) {
            UUID uuid = party.getMembers().get(i).getUniqueId();
            FriendData friendData = new FriendData(uuid);
            ContainerModule playerCard = this.playerCardParty2.copy();

            playerCard.setModuleID(uuid.toString());
            playerCard.setX(startX+x*width+x*5);
            playerCard.setY(startY+y*height+y*5);

            ImageModule playerImage = (ImageModule) playerCard.getModule("PlayerImage");
            playerImage.setImage(friendData.getDirections(playerImage.getImageList()));

            TextModule playerText = (TextModule) playerCard.getModule("PlayerText");
            playerText.setText(friendData.getDirections(playerText.getText()));

            if(party.getOwner().equals(playerData)){
                ButtonModule kickButton = (ButtonModule) playerCard.getModule("KickButton");
                kickButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                    if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){

                        final String uuidTag = uuid.toString();

                        final OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(uuidTag));
                        if (target.equals(player)) return;

                        party.removeMember(PlayerData.get(target));
                        ConfigMessage.fromKey("kick-from-party", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                });

            }else {
                playerCard.removeModule("KickButton");
            }



            playerContainer.addModule(playerCard);

            if(x == 1){
                y++;
            }
            x++;
            if(x == 2){
                x = 0;
            }

        }
    }

    //顯示所有非好友玩家
    public void displayPlayers(Player player){
        PlayerData playerData = PlayerData.get(player);
        List<UUID> uuidList = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");
        int width = this.playerCardNotParty2.getWidth();
        int height = this.playerCardNotParty2.getHeight();
        int startX = this.playerCardNotParty2.getX();
        int startY = this.playerCardNotParty2.getY();

        int x = 0;
        int y = 0;

        Party party = (Party) playerData.getParty();
        if(party == null){
            return;
        }


        for(int i = 0; i < uuidList.size(); i++){
            UUID uuid = uuidList.get(i);
            if(player.getUniqueId().equals(uuid)){
                continue;
            }

            if (party.getMembers().stream().anyMatch(playerData1 -> playerData1.getUniqueId().equals(uuid))) {
                continue;
            }

            FriendData friendData = new FriendData(uuid);
            ContainerModule playerCard = this.playerCardNotParty2.copy();

            playerCard.setModuleID(uuid.toString());
            playerCard.setX(startX+x*width+x*5);
            playerCard.setY(startY+y*height+y*5);

            ImageModule playerImage = (ImageModule) playerCard.getModule("PlayerImage");
            playerImage.setImage(friendData.getDirections(playerImage.getImageList()));

            TextModule playerText = (TextModule) playerCard.getModule("PlayerText");
            playerText.setText(friendData.getDirections(playerText.getText()));

            ButtonModule addFriendButton = (ButtonModule) playerCard.getModule("AddFriendButton");
            addFriendButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                    Player target = Bukkit.getPlayer(uuid);

                    if (party.getMembers().size() >= MMOCore.plugin.configManager.maxPartyPlayers) {
                        ConfigMessage.fromKey("party-is-full").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return;
                    }


                    if (target == null) {
                        ConfigMessage.fromKey("not-online-player", "player", uuid).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    long remaining = party.getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                    if (remaining > 0) {
                        ConfigMessage.fromKey("party-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);

                        return;
                    }

                    PlayerData targetData = PlayerData.get(target);
                    if (party.hasMember(target)) {
                        ConfigMessage.fromKey("already-in-party", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    int levelDifference = Math.abs(targetData.getLevel() - party.getLevel());
                    if (levelDifference > MMOCore.plugin.configManager.maxPartyLevelDifference) {
                        ConfigMessage.fromKey("high-level-difference", "player", target.getName(), "diff", String.valueOf(levelDifference)).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    party.sendInvite(playerData, targetData);
                    ConfigMessage.fromKey("sent-party-invite", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);


                }
            });

            playerContainer.addModule(playerCard);

            if(x == 1){
                y++;
            }
            x++;
            if(x == 2){
                x = 0;
            }
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
