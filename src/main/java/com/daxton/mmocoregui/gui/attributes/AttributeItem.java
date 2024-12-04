package com.daxton.mmocoregui.gui.attributes;

import com.daxton.mmocoregui.MMOCoreGUI;
import io.lumine.mythic.lib.manager.StatManager;
import lombok.Getter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public class AttributeItem {

    private final Player player;
    private final PlayerAttribute attribute;
    private final int shiftCost;
    private Placeholders placeholders;

    public AttributeItem(String function, ConfigurationSection config, Player player) {
        String attributeName = function.substring("attribute_".length()).toLowerCase().replace(" ", "-").replace("_", "-");
        this.player = player;
        this.attribute = MMOCore.plugin.attributeManager.get(attributeName);
        this.shiftCost = Math.max(config.getInt("shift-cost"), 1);
        this.placeholders = getPlaceholders(player);
    }

    public void rePlaceholders(){
        this.placeholders = getPlaceholders(this.player);
    }

    public Placeholders getPlaceholders(Player player) {

        PlayerData playerData = PlayerData.get(player);
        int total = playerData.getAttributes().getInstance(attribute).getTotal();
        Placeholders holders = new Placeholders();

        holders.register("attribute_points", playerData.getAttributePoints());
        holders.register("points", playerData.getAttributeReallocationPoints());
        holders.register("total", playerData.getAttributes().countPoints());

        holders.register("name", attribute.getName());
        holders.register("buffs", attribute.getBuffs().size());
        holders.register("spent", playerData.getAttributes().getInstance(attribute).getBase());
        holders.register("max", attribute.getMax());
        holders.register("current", total);
        holders.register("attribute_points", playerData.getAttributePoints());
        holders.register("shift_points", shiftCost);
        attribute.getBuffs().forEach(buff -> {
            final String stat = buff.getStat();
            holders.register("buff_" + buff.getStat().toLowerCase(), StatManager.format(stat, buff.getValue()));
            holders.register("total_" + buff.getStat().toLowerCase(), StatManager.format(stat, buff.multiply(total).getValue()));
        });

        return holders;
    }

}
