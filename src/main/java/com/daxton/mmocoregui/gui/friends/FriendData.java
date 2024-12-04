package com.daxton.mmocoregui.gui.friends;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;

import net.Indyuce.mmocore.gui.api.item.Placeholders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendData {

    private final UUID uuid;

    public FriendData(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isOnline(){
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline();
    }

    public List<String> getDirections(List<String> stringList){
//        EditableFriendList editableFriendList = InventoryManager.FRIEND_LIST;
//        FileConfiguration config = editableFriendList.getConfig();
        List<String> directions = new ArrayList<>();
        Placeholders placeholders = getPlaceholders();
        if(isOnline()){
            stringList.forEach(s -> {
                directions.add(placeholders.apply(getPlayer(), s));
            });
        }else {
            stringList.forEach(s -> {
                directions.add(placeholders.apply(getOfflinePlayer(), s));
            });
        }

        return directions;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(this.uuid);
    }

    public OfflinePlayer getOfflinePlayer(){
        return Bukkit.getOfflinePlayer(uuid);
    }

    public Placeholders getPlaceholders() {

        Placeholders holders = new Placeholders();
        holders.register("uuid", this.uuid);
        if (isOnline()){
            PlayerData friendData = PlayerData.get(this.uuid);
            holders.register("name", friendData.getPlayer().getName());
            holders.register("class", friendData.getProfess().getName());
            holders.register("level", friendData.getLevel());
            holders.register("online_since", new DelayFormat(2).format(System.currentTimeMillis() - friendData.getLastLogin()));
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - friendData.getLastLogin()));
        }else {
            OfflinePlayer friend = Bukkit.getOfflinePlayer(this.uuid);
            holders.register("name", friend.getName());
            holders.register("last_seen", new DelayFormat(2).format(System.currentTimeMillis() - friend.getLastPlayed()));
        }

        return holders;
    }

}
