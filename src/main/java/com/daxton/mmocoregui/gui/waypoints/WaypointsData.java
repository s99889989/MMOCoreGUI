package com.daxton.mmocoregui.gui.waypoints;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.WaypointViewer;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointPath;
import net.Indyuce.mmocore.waypoint.WaypointPathCalculation;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaypointsData {

    private final SimplePlaceholderItem noWaypoint, locked;
    private final WaypointItemHandler availWaypoint, noStellium, notLinked, currentWayPoint;

    public WaypointsData() {
        WaypointViewer waypointViewer = InventoryManager.WAYPOINTS;
        FileConfiguration fileConfiguration = waypointViewer.getConfig();
        ConfigurationSection config = fileConfiguration.getConfigurationSection("items.waypoint");

        Validate.notNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config");
        Validate.notNull(config.getConfigurationSection("locked"), "Could not load 'locked' config");
        Validate.notNull(config.getConfigurationSection("not-a-destination"), "Could not load 'not-a-destination' config");
        //Validate.notNull(config.getConfigurationSection("not-dynamic"), "Could not load 'not-dynamic' config");
        Validate.notNull(config.getConfigurationSection("current-waypoint"), "Could not load 'current-waypoint' config");
        Validate.notNull(config.getConfigurationSection("not-enough-stellium"), "Could not load 'not-enough-stellium' config");
        Validate.notNull(config.getConfigurationSection("display"), "Could not load 'display' config");

        noWaypoint = new SimplePlaceholderItem(config.getConfigurationSection("no-waypoint"));
        locked = new SimplePlaceholderItem(config.getConfigurationSection("locked"));
        notLinked = new WaypointItemHandler(config.getConfigurationSection("not-a-destination"), true);
        //notDynamic = new WaypointItemHandler(config.getConfigurationSection("not-dynamic"), true);
        currentWayPoint = new WaypointItemHandler(config.getConfigurationSection("current-waypoint"), true);
        noStellium = new WaypointItemHandler(config.getConfigurationSection("not-enough-stellium"), false);
        availWaypoint = new WaypointItemHandler(config.getConfigurationSection("display"), false);

    }

    public class WaypointItemHandler extends InventoryItem<WaypointViewer.WaypointViewerInventory> {
        private final boolean onlyName;
        private final String splitter, none;

        public WaypointItemHandler(ConfigurationSection config, boolean onlyName) {
            super(config);

            this.onlyName = onlyName;
            this.splitter = config.getString("format_path.splitter", ", ");
            this.none = config.getString("format_path.none", "None");
        }

        public List<String> display(Player player, Waypoint waypoint) {
            PlayerData playerData = PlayerData.get(player);
            // TODO refactor code
            final Placeholders placeholders = getPlaceholders(playerData, waypoint);



            List<String> lore = new ArrayList<>();
            if (hasLore()) {

                getLore().forEach(line -> {
                    if (line.equals("{lore}")) for (String added : waypoint.getLore())
                        lore.add(ChatColor.GRAY + placeholders.apply(player, added));
                    else lore.add(ChatColor.GRAY + placeholders.apply(player, line));
                });

            }

            return lore;
        }

        @Override
        public ItemStack display(WaypointViewer.WaypointViewerInventory inv, int n) {

            // TODO refactor code
            final Placeholders placeholders = getPlaceholders(inv, n);
            final OfflinePlayer effectivePlayer = getEffectivePlayer(inv, n);

            final ItemStack item = new ItemStack(getMaterial());
            final ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(getModelData());
            // if (texture != null && meta instanceof SkullMeta)
            //    UtilityMethods.setTextureValue((SkullMeta) meta, texture);

            if (hasName()) meta.setDisplayName(placeholders.apply(effectivePlayer, getName()));

            if (hideFlags()) MMOCoreUtils.addAllItemFlags(meta);
//            if (hideTooltip()) meta.setHideTooltip(true);
//            // If a player can teleport to another waypoint given his location
//            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n);

//            if (hasLore()) {
//                List<String> lore = new ArrayList<>();
//                getLore().forEach(line -> {
//                    if (line.equals("{lore}")) for (String added : waypoint.getLore())
//                        lore.add(ChatColor.GRAY + placeholders.apply(effectivePlayer, added));
//                    else lore.add(ChatColor.GRAY + placeholders.apply(effectivePlayer, line));
//                });
//                meta.setLore(lore);
//            }

            item.setItemMeta(meta);

            // Extra code
            PersistentDataContainer container = meta.getPersistentDataContainer();
//            container.set(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING, waypoint.getId());
            item.setItemMeta(meta);
            return item;
        }


        public Placeholders getPlaceholders(PlayerData playerData, Waypoint waypoint) {
            Placeholders holders = new Placeholders();
            Map<Waypoint, WaypointPath> paths = new WaypointPathCalculation(playerData).run(waypoint).getPaths();
            DecimalFormat decimal = MythicLib.plugin.getMMOConfig().decimal;
            holders.register("name", waypoint.getName());

            if (!onlyName) {
                holders.register("current_cost", paths.get(waypoint).getCost());
                holders.register("normal_cost", decimal.format(paths.containsKey(waypoint) ? paths.get(waypoint).getCost() : Double.POSITIVE_INFINITY));
                holders.register("dynamic_cost", decimal.format(waypoint.getDynamicCost()));
                holders.register("intermediary_waypoints", paths.containsKey(waypoint) ? paths.get(waypoint).displayIntermediaryWayPoints(splitter, none) : none);
            }

            return holders;
        }

        @Override
        public Placeholders getPlaceholders(WaypointViewer.WaypointViewerInventory inv, int n) {
            Placeholders holders = new Placeholders();

//            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getByFunction("waypoint").getSlots().size() + n);
//            holders.register("name", waypoint.getName());
//
//            if (!onlyName) {
//                holders.register("current_cost", inv.paths.get(waypoint).getCost());
//                holders.register("normal_cost", decimal.format(inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).getCost() : Double.POSITIVE_INFINITY));
//                holders.register("dynamic_cost", decimal.format(waypoint.getDynamicCost()));
//                holders.register("intermediary_waypoints", inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).displayIntermediaryWayPoints(splitter, none) : none);
//            }

            return holders;
        }

    }

}
