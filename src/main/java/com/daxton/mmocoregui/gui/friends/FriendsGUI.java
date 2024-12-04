package com.daxton.mmocoregui.gui.friends;

import com.daxton.mmocoregui.application.GUIUtil;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.control.ContainerModule;
import com.daxton.unrealcore.display.content.module.display.ImageModule;
import com.daxton.unrealcore.display.content.module.display.TextModule;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;

import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FriendsGUI extends UnrealCoreGUI {

    private ContainerModule playerCardNotFriend2;
    private ContainerModule playerCardOnline2;
    private  ContainerModule playerCardOffline2;


    public FriendsGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");

        this.playerCardNotFriend2 = (ContainerModule) playerContainer.getModule("PlayerCardNotFriend").copy();
        playerContainer.removeModule("PlayerCardNotFriend");
        this.playerCardOnline2 = (ContainerModule) playerContainer.getModule("PlayerCardOnline").copy();
        playerContainer.removeModule("PlayerCardOnline");
        this.playerCardOffline2 = (ContainerModule) playerContainer.getModule("PlayerCardOffline").copy();
         playerContainer.removeModule("PlayerCardOffline");

        displayFriends(player);


        ButtonModule friendButton = (ButtonModule) getModule("OptionsContainer", "FriendButton");
        friendButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                playerContainer.clearModule();
                displayFriends(getPlayer());
                upDate();
            }
        });

        ButtonModule playerButton = (ButtonModule) getModule("OptionsContainer", "PlayerButton");
        playerButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                playerContainer.clearModule();
                displayPlayers(getPlayer());
                upDate();
            }
        });

    }

    //顯示所有非好友玩家
    public void displayPlayers(Player player){
        PlayerData playerData = PlayerData.get(player);
        List<UUID> uuidList = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");
        int width = this.playerCardNotFriend2.getWidth();
        int height = this.playerCardNotFriend2.getHeight();
        int startX = this.playerCardNotFriend2.getX();
        int startY = this.playerCardNotFriend2.getY();

        int x = 0;
        int y = 0;

        for(int i = 0; i < uuidList.size(); i++){
            UUID uuid = uuidList.get(i);
            if(playerData.getFriends().contains(uuid) || player.getUniqueId().equals(uuid)){
                continue;
            }
            FriendData friendData = new FriendData(uuid);
            ContainerModule playerCard = this.playerCardNotFriend2.copy();

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
                    long remaining = playerData.getActivityTimeOut(PlayerActivity.FRIEND_REQUEST);
                    if (remaining > 0) {
                        ConfigMessage.fromKey("friend-request-cooldown", "cooldown", new DelayFormat().format(remaining))
                                .send(player);
                        return;
                    }

                    Player target = Bukkit.getPlayer(uuid);
                    if (target == null) {
                        ConfigMessage.fromKey("not-online-player", "player", uuid).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    if (playerData.hasFriend(target.getUniqueId())) {
                        ConfigMessage.fromKey("already-friends", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    if (playerData.getUniqueId().equals(target.getUniqueId())) {
                        ConfigMessage.fromKey("cant-request-to-yourself").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    playerData.sendFriendRequest(PlayerData.get(target));
                    ConfigMessage.fromKey("sent-friend-request", "player", target.getName()).send(player);
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

    //顯示所有好友
    public void displayFriends(Player player){

        PlayerData playerData = PlayerData.get(player);

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");

        int width = this.playerCardOnline2.getWidth();
        int height = this.playerCardOnline2.getHeight();
        int startX = this.playerCardOnline2.getX();
        int startY = this.playerCardOnline2.getY();

        int x = 0;
        int y = 0;
        for(int i = 0; i < playerData.getFriends().size(); i++) {
            UUID uuid = playerData.getFriends().get(i);
            FriendData friendData = new FriendData(uuid);
            ContainerModule playerCard;

            if(friendData.isOnline()){
                playerCard = playerCardOnline2.copy();
            }else {
                playerCard = playerCardOffline2.copy();
            }

            playerCard.setModuleID(uuid.toString());
            playerCard.setX(startX+x*width+x*5);
            playerCard.setY(startY+y*height+y*5);

            ImageModule playerImage = (ImageModule) playerCard.getModule("PlayerImage");
            playerImage.setImage(friendData.getDirections(playerImage.getImageList()));

            TextModule playerText = (TextModule) playerCard.getModule("PlayerText");
            playerText.setText(friendData.getDirections(playerText.getText()));

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
