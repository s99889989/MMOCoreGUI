package com.daxton.mmocoregui.gui.quests;

import com.daxton.mmocoregui.MMOCoreGUI;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.quest.Quest;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.gui.QuestViewer;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestData {

    private final SimplePlaceholderItem noQuest, locked;

    private final String mainHit, mainNotHit, professionHit, professionNotHit;
    private final SimpleDateFormat dateFormat;

    private final List<String> lore;

    public QuestData() {
        QuestViewer questViewer = InventoryManager.QUEST_LIST;
        FileConfiguration fileConfiguration = questViewer.getConfig();
        ConfigurationSection config = fileConfiguration.getConfigurationSection("items.skill");

        this.lore = config.getStringList("lore");

        Validate.isTrue(config.contains("no-quest"), "Could not load config 'no-quest'");
        Validate.isTrue(config.contains("locked"), "Could not load config 'locked'");

        locked = new SimplePlaceholderItem(config.getConfigurationSection("locked"));
        noQuest = new SimplePlaceholderItem(config.getConfigurationSection("no-quest"));

        Validate.isTrue(config.contains("date-format"), "Could not find date-format");
        dateFormat = new SimpleDateFormat(config.getString("date-format"));

        Validate.notNull(mainHit = config.getString("level-requirement.main.hit"), "Could not load 'level-requirement.main.hit'");
        Validate.notNull(mainNotHit = config.getString("level-requirement.main.not-hit"), "Could not load 'level-requirement.main.not-hit'");
        Validate.notNull(professionHit = config.getString("level-requirement.profession.hit"),
                "Could not load 'level-requirement.profession.hit'");
        Validate.notNull(professionNotHit = config.getString("level-requirement.profession.not-hit"),
                "Could not load 'level-requirement.profession.not-hit'");
//        MMOCoreGUI.unrealCorePlugin.sendLogger(mainHit + " " + mainNotHit + " " + professionHit + " ");

//        this.lore.forEach(s -> MMOCoreGUI.unrealCorePlugin.sendLogger(s));
    }


    public List<String> display(Player player, Quest quest) {
        PlayerData playerData = PlayerData.get(player);
//        if (quest.hasParent() && !playerData.getQuestData().checkParentAvailability(quest))
//            return locked.display(inv, itemIndex);

        List<String> lore = new ArrayList<>(this.lore);

        // Replace quest lore
        int loreIndex = lore.indexOf("{lore}");
        if (loreIndex >= 0) {
            lore.remove(loreIndex);
            for (int j = 0; j < quest.getLore().size(); j++)
                lore.add(loreIndex + j, quest.getLore().get(j));
        }

        // Calculate lore for later
        int reqCount = quest.countLevelRestrictions();
        boolean started = playerData.getQuestData().hasCurrent(quest), completed = playerData.getQuestData().hasFinished(quest),
                cooldown = completed && playerData.getQuestData().checkCooldownAvailability(quest);

        lore.removeIf(next -> (next.startsWith("{level_req}") && reqCount < 1)
                || (next.startsWith("{started}") && !started)
                || (next.startsWith("{!started}") && started)
                || (next.startsWith("{completed}") && !completed)
                || (next.startsWith("{completed_cannot_redo}") && !(completed && !quest.isRedoable()))
                || (next.startsWith("{completed_can_redo}") && !(cooldown && quest.isRedoable()))
                || (next.startsWith("{completed_delay}") && !(completed && !cooldown)));

        // Replace level requirements
        loreIndex = lore.indexOf("{level_req}{level_requirements}");
        if (loreIndex >= 0) {
            lore.remove(loreIndex);
            int mainRequired = quest.getLevelRestriction(null);
            if (mainRequired > 0)
                lore.add(loreIndex, (playerData.getLevel() >= mainRequired ? mainHit : mainNotHit).replace("{level}", "" + mainRequired));

            for (Profession profession : quest.getLevelRestrictions()) {
                int required = quest.getLevelRestriction(profession);
                lore.add(loreIndex + (mainRequired > 0 ? 1 : 0),
                        (playerData.getCollectionSkills().getLevel(profession) >= required ? professionHit : professionNotHit)
                                .replace("{level}", "" + required).replace("{profession}", profession.getName()));
            }
        }

        Placeholders holders = getPlaceholders(playerData, quest);
        for (int j = 0; j < lore.size(); j++)
            lore.set(j, ChatColor.GRAY + holders.apply(player, lore.get(j)));



        return lore;
    }


    public Placeholders getPlaceholders(PlayerData data, Quest quest) {


        Placeholders holders = new Placeholders();
        holders.register("name", quest.getName());
        holders.register("total_level_req", quest.getLevelRestrictions().size() + (quest.getLevelRestriction(null) > 0 ? 1 : 0));
        holders.register("current_level_req", (data.getLevel() >= quest.getLevelRestriction(null) ? 1 : 0) + quest.getLevelRestrictions().stream()
                .filter(type -> data.getCollectionSkills().getLevel(type) >= quest.getLevelRestriction(type)).collect(Collectors.toSet()).size());

        if (data.getQuestData().hasCurrent(quest)) {
            holders.register("objective", data.getQuestData().getCurrent().getFormattedLore());
            holders.register("progress",
                    (int) ((double) data.getQuestData().getCurrent().getObjectiveNumber() / quest.getObjectives().size() * 100.));
        }

        if (data.getQuestData().hasFinished(quest)) {
            holders.register("date", dateFormat.format(data.getQuestData().getFinishDate(quest)));
            holders.register("delay", new DelayFormat(2).format(data.getQuestData().getDelayFeft(quest)));
        }

        return holders;
    }

}
