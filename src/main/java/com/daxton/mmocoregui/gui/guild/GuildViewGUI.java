package com.daxton.mmocoregui.gui.guild;

import com.daxton.mmocoregui.application.GUIUtil;
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
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.guild.provided.Guild;
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

public class GuildViewGUI extends UnrealCoreGUI {

    private ContainerModule playerCardNotGuild2;
    private ContainerModule playerCardGuild2;

    public GuildViewGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");
        this.playerCardNotGuild2 = (ContainerModule) playerContainer.getModule("PlayerCardNotGuild").copy();
        playerContainer.removeModule("PlayerCardNotGuild");
        this.playerCardGuild2 = (ContainerModule) playerContainer.getModule("PlayerCardGuild").copy();
        playerContainer.removeModule("PlayerCardGuild");

        //顯示所有隊友
        displayParty(player);

        //組隊玩家列表
        ButtonModule guildPlayerButton = (ButtonModule) getModule("OptionsContainer", "GuildPlayerButton");
        guildPlayerButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                playerContainer.clearModule();
                displayParty(getPlayer());
                upDate();
            }
        });

        //邀請玩家列表
        ButtonModule invitePlayerButton = (ButtonModule) getModule("OptionsContainer", "InvitePlayerButton");
        invitePlayerButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                playerContainer.clearModule();
                displayPlayers(getPlayer());
                upDate();
            }
        });

        //離開公會
        ButtonModule leaveGuildButton = (ButtonModule) getModule("OptionsContainer", "LeaveGuildButton");
        leaveGuildButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                PlayerData playerData = PlayerData.get(player);
                playerData.getGuild().removeMember(playerData.getUniqueId());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
        });

        //解散公會
        ButtonModule disbandGuildButton = (ButtonModule) getModule("OptionsContainer", "DisbandGuildButton");
        disbandGuildButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
            if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                PlayerData playerData = PlayerData.get(player);
                if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
                    return;
                MMOCore.plugin.nativeGuildManager.unregisterGuild(playerData.getGuild());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                close();
            }
        });


    }


    //顯示所有隊友
    public void displayParty(Player player){
        PlayerData playerData = PlayerData.get(player);

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");

        int width = this.playerCardGuild2.getWidth();
        int height = this.playerCardGuild2.getHeight();
        int startX = this.playerCardGuild2.getX();
        int startY = this.playerCardGuild2.getY();

        int x = 0;
        int y = 0;

        Guild guild = playerData.getGuild();

        if(guild == null){
            return;
        }

        for(int i = 0; i < guild.listMembers().size(); i++) {
            UUID uuid = guild.listMembers().get(i);
            FriendData friendData = new FriendData(uuid);
            ContainerModule playerCard = this.playerCardGuild2.copy();

            playerCard.setModuleID(uuid.toString());
            playerCard.setX(startX+x*width+x*5);
            playerCard.setY(startY+y*height+y*5);

            ImageModule playerImage = (ImageModule) playerCard.getModule("PlayerImage");
            playerImage.setImage(friendData.getDirections(playerImage.getImageList()));

            TextModule playerText = (TextModule) playerCard.getModule("PlayerText");
            playerText.setText(friendData.getDirections(playerText.getText()));

            if(guild.getOwner().equals(playerData.getUniqueId())){
                ButtonModule kickButton = (ButtonModule) playerCard.getModule("KickButton");
                kickButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                    if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){

                        if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
                            return;

                        OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                        if (target.equals(player))
                            return;

                        playerData.getGuild().removeMember(target.getUniqueId());
                        ConfigMessage.fromKey("kick-from-guild", "player", target.getName()).send(player);
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

    //顯示所有非公會玩家
    public void displayPlayers(Player player){
        PlayerData playerData = PlayerData.get(player);
        List<UUID> uuidList = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());

        ContainerModule playerContainer = (ContainerModule) getModule("PlayerContainer");
        int width = this.playerCardNotGuild2.getWidth();
        int height = this.playerCardNotGuild2.getHeight();
        int startX = this.playerCardNotGuild2.getX();
        int startY = this.playerCardNotGuild2.getY();

        int x = 0;
        int y = 0;

        Guild guild = playerData.getGuild();
        if(guild == null){
            return;
        }


        for(int i = 0; i < uuidList.size(); i++){
            UUID uuid = uuidList.get(i);
            if(player.getUniqueId().equals(uuid) || guild.listMembers().contains(uuid)){
                continue;
            }

            FriendData friendData = new FriendData(uuid);
            ContainerModule playerCard = this.playerCardNotGuild2.copy();

            playerCard.setModuleID(uuid.toString());
            playerCard.setX(startX+x*width+x*5);
            playerCard.setY(startY+y*height+y*5);

            ImageModule playerImage = (ImageModule) playerCard.getModule("PlayerImage");
            playerImage.setImage(friendData.getDirections(playerImage.getImageList()));

            TextModule playerText = (TextModule) playerCard.getModule("PlayerText");
            playerText.setText(friendData.getDirections(playerText.getText()));

            ButtonModule addGuildButton = (ButtonModule) playerCard.getModule("AddGuildButton");
            addGuildButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                    if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
                        return;
                    Player target = Bukkit.getPlayer(uuid);
                    if (target == null) {
                        ConfigMessage.fromKey("not-online-player", "player", uuid).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    long remaining = playerData.getGuild().getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                    if (remaining > 0) {
                        ConfigMessage.fromKey("guild-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);

                        return;
                    }

                    PlayerData targetData = PlayerData.get(target);
                    if (playerData.getGuild().hasMember(targetData.getUniqueId())) {
                        ConfigMessage.fromKey("already-in-guild", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

                        return;
                    }

                    playerData.getGuild().sendGuildInvite(playerData, targetData);
                    ConfigMessage.fromKey("sent-guild-invite", "player", target.getName()).send(player);
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
