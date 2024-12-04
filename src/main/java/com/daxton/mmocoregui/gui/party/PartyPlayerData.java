package com.daxton.mmocoregui.gui.party;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PartyPlayerData {

    private final UUID uuid;

    public PartyPlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public Placeholders getPlaceholders() {

        Placeholders holders = new Placeholders();
        holders.register("uuid", this.uuid);
        PlayerData friendData = PlayerData.get(this.uuid);
        holders.register("name", friendData.getPlayer().getName());
        holders.register("class", friendData.getProfess().getName());
        holders.register("level", friendData.getLevel());
        holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - friendData.getLastLogin()));

        return holders;
    }

}
