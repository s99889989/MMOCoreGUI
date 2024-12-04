package com.daxton.mmocoregui.gui.skills;

import com.daxton.mmocoregui.MMOCoreGUI;
import com.daxton.mmocoregui.application.GUIUtil;
import com.daxton.mmocoregui.been.type.MMOCoreGUIType;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.gui.UnrealCoreGUI;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.control.ContainerModule;
import com.daxton.unrealcore.display.content.module.display.ItemModule;
import com.daxton.unrealcore.display.content.module.display.TextModule;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.SkillList;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SkillsGUI extends UnrealCoreGUI {

    private String name = "";
    private List<String> lore = new ArrayList<>();
    private List<String> skillTextList = new ArrayList<>();

    private ClassSkill selectClassSkill;

    public SkillsGUI(String guiName, FileConfiguration fileConfiguration, Player player) {
        super(guiName, fileConfiguration);

        SkillList skillList = InventoryManager.SKILL_LIST;
        FileConfiguration config = skillList.getConfig();
        if (config.contains("items")) {
            this.name = config.getString("items.skill.name");
            this.lore = config.getStringList("items.skill.lore");
        }
        setSkillSlot(player);
        PlayerData playerData = PlayerData.get(player);
        List<ClassSkill> skills = playerData.getProfess().getSkills()
                .stream()
                .filter(skill -> playerData.hasUnlocked(skill))
                .sorted(Comparator.comparingInt(ClassSkill::getUnlockLevel))
                .collect(Collectors.toList());

        ContainerModule skillListContainer = (ContainerModule) getModule("SkillListContainer");
        ContainerModule skillCard2 = (ContainerModule) skillListContainer.getModule("SkillCard").copy();
        TextModule skillText2 = (TextModule) skillCard2.getModule("SkillText");
        skillTextList = skillText2.getText();

        int height = skillCard2.getHeight();
        int startY = skillCard2.getY();
        int maxHeight = startY;
        for(int i = 0; i < skills.size(); i++) {
            ClassSkill classSkill = skills.get(i);

            Placeholders holders = getPlaceholders(playerData, classSkill);
            RegisteredSkill registeredSkill = classSkill.getSkill();

            int y = startY + (height+5) * i;
            ContainerModule skillCardModule = skillCard2.copy();
            skillCardModule.setModuleID("SkillCard" + registeredSkill.getName());
            skillCardModule.setY(y);

            TextModule skillText = (TextModule) skillCardModule.getModule("SkillText");
            List<String> stringList = new ArrayList<>();
            this.skillTextList.forEach(s -> {
                stringList.add(holders.apply(player, s));
            });
            skillText.setText(stringList);

            ButtonModule selectButton = (ButtonModule) skillCardModule.getModule("SelectButton");
            selectButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                    this.selectClassSkill = classSkill;
                    selectSkill(classSkill);
                }
            });

            ItemModule skillItem = (ItemModule) skillCardModule.getModule("SkillItem");
            skillItem.setItem(classSkill.getSkill().getIcon());

            ButtonModule upGradeButton = (ButtonModule) skillCardModule.getModule("UpGradeButton");
            upGradeButton.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
                    upGradeSkill(classSkill);
                }
            });

//            skillButtonModule.setText(registeredSkill.getName());
//            skillButtonModule.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
//                if(mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.On){
//                    selectSkill(classSkill);
//                }
//            });
            skillListContainer.addModule(skillCardModule);
            maxHeight += height + 5;
        }
        skillListContainer.setActualHeight(maxHeight);
        skillListContainer.removeModule("SkillCard");


    }

    public void setSkillSlot(Player player){
        ContainerModule skillBindContainer = (ContainerModule) getModule("SkillBindContainer");
        ContainerModule skillBindCard2 = (ContainerModule) skillBindContainer.getModule("SkillBindCard").copy();
        int width = skillBindCard2.getWidth();
        int startX = skillBindCard2.getX();
        skillBindContainer.removeModule("SkillBindCard");
        PlayerData playerData = PlayerData.get(player);
        for(int i = 0; i < 8; i++) {
            int n = i+1;


            ItemStack itemStack = new ItemStack(Material.AIR);
            SkillSlot skillSlot = playerData.getProfess().getSkillSlot(n);
            if(skillSlot != null){
                ClassSkill boundSkill = playerData.getBoundSkill(n);
                if (boundSkill != null) {
                    itemStack = boundSkill.getSkill().getIcon();
                }
            }else {
                continue;
            }

            int x = startX + (width+startX) * i;
            ContainerModule skillBindCard = skillBindCard2.copy();
            skillBindCard.setModuleID("SkillBindCard" + n);
            skillBindCard.setX(x);
            skillBindContainer.addModule(skillBindCard);

            ItemModule skillItem = (ItemModule) skillBindCard.getModule("SkillItem");
            skillItem.setItem(itemStack);

            ButtonModule skillBindButtonModule = (ButtonModule) skillBindCard.getModule("SkillBindButton");
            skillBindButtonModule.onButtonClick((buttonModule, mouseButtonType, mouseActionType) -> {
                if(mouseActionType == MouseActionType.On){

                    if(mouseButtonType == MouseButtonType.Left){
                        if(this.selectClassSkill != null){
//                            ClassSkill boundSkill = playerData.getBoundSkill(n);
//                            if (boundSkill != null) {
//                                selectBind(n, this.selectClassSkill, false);
//                            }
                            selectBind(n, this.selectClassSkill, false);
                        }
                    }
                    if(mouseButtonType == MouseButtonType.Right){
                        selectBind(n, this.selectClassSkill, true);
                    }
                }
            });


        }
    }

    public void selectBind(int index, ClassSkill selected, boolean remove){
        Player player = getPlayer();
        PlayerData playerData = PlayerData.get(player);

        SkillSlot skillSlot = playerData.getProfess().getSkillSlot(index);
        if(skillSlot == null){return;}
        // 如果有當前的咒語，請解開。
        if (remove) {
            if (!playerData.hasSkillBound(index)) {
                ConfigMessage.fromKey("no-skill-bound").send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }
            if (!playerData.getProfess().getSkillSlot(index).canManuallyBind()) {
                ConfigMessage.fromKey("cant-manually-bind").send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            playerData.unbindSkill(index);
            ItemModule skillItem = (ItemModule) getModule("SkillBindContainer", "SkillBindCard" + index, "SkillItem");
            skillItem.setItem(new ItemStack(Material.AIR));
            addModule(skillItem);
            upDate();
            return;
        }

        if (selected.isPermanent()) {
            ConfigMessage.fromKey("skill-cannot-be-bound").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        if (!playerData.hasUnlockedLevel(selected)) {
            ConfigMessage.fromKey("skill-level-not-met").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        if (!skillSlot.canManuallyBind()) {
            ConfigMessage.fromKey("cant-manually-bind").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        if (!skillSlot.acceptsSkill(selected)) {
            ConfigMessage.fromKey("not-compatible-skill").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
        playerData.bindSkill(index, selected);

        ItemStack itemStack = new ItemStack(Material.AIR);
        ClassSkill boundSkill = playerData.getBoundSkill(index);
        if (boundSkill != null) {
            itemStack = boundSkill.getSkill().getIcon();
        }

        ItemModule skillItem = (ItemModule) getModule("SkillBindContainer", "SkillBindCard" + index, "SkillItem");
        skillItem.setItem(itemStack);
        addModule(skillItem);
        upDate();
    }

    //升級技能
    public void upGradeSkill(ClassSkill classSkill){
        Player player = getPlayer();
        PlayerData playerData = PlayerData.get(getPlayer());

        boolean shift = false;

        int shiftCost = 1;

        if (!playerData.hasUnlockedLevel(classSkill)) {
            ConfigMessage.fromKey("skill-level-not-met").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        if (!classSkill.isUpgradable()) {
            ConfigMessage.fromKey("cannot-upgrade-skill").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        if (playerData.getSkillPoints() < 1) {
            ConfigMessage.fromKey("not-enough-skill-points").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        if (classSkill.hasMaxLevel() && playerData.getSkillLevel(classSkill.getSkill()) >= classSkill.getMaxLevel()) {
            ConfigMessage.fromKey("skill-max-level-hit").send(player);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
        }

        if (shift) {
            if (playerData.getSkillPoints() < shiftCost) {
                ConfigMessage.fromKey("not-enough-skill-points-shift", "shift_points", "" + shiftCost).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                return;
            }

            playerData.giveSkillPoints(-shiftCost);
            playerData.setSkillLevel(classSkill.getSkill(), playerData.getSkillLevel(classSkill.getSkill()) + shiftCost);
        } else {
            playerData.giveSkillPoints(-1);
            playerData.setSkillLevel(classSkill.getSkill(), playerData.getSkillLevel(classSkill.getSkill()) + 1);
        }

        ConfigMessage.fromKey("upgrade-skill", "skill", classSkill.getSkill().getName(), "level",
                "" + playerData.getSkillLevel(classSkill.getSkill())).send(player);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);

        RegisteredSkill registeredSkill = classSkill.getSkill();
        Placeholders holders = getPlaceholders(playerData, classSkill);
        TextModule skillText = (TextModule) getModule("SkillListContainer", "SkillCard" + registeredSkill.getName(), "SkillText");
        List<String> stringList = new ArrayList<>();
        this.skillTextList.forEach(s -> {
            stringList.add(holders.apply(player, s));
        });
        skillText.setText(stringList);
        addModule(skillText);
        upDate();
    }

    public void selectSkill(ClassSkill skill){
        PlayerData playerData = PlayerData.get(getPlayer());
        Placeholders holders = getPlaceholders(playerData, skill);


        List<String> lore = new ArrayList<>(this.lore);

        int index = lore.indexOf("{lore}");
        lore.remove(index);
        List<String> skillLore = skill.calculateLore(playerData);
        for (int j = 0; j < skillLore.size(); j++)
            lore.add(index + j, skillLore.get(j));

        boolean unlocked = skill.getUnlockLevel() <= playerData.getLevel();

        lore.removeIf(next -> (next.startsWith("{unlocked}") && !unlocked) || (next.startsWith("{locked}") && unlocked) || (next.startsWith("{max_level}") && (!skill.hasMaxLevel() || skill.getMaxLevel() > playerData.getSkillLevel(skill.getSkill()))));

        for (int j = 0; j < lore.size(); j++)
            lore.set(j, ChatColor.GRAY + holders.apply(getPlayer(), lore.get(j)));

        TextModule skillDirectionsText = (TextModule) getModule("SkillDirectionsContainer", "SkillDirectionsText");

        skillDirectionsText.setText(lore);

        addModule(skillDirectionsText);
        upDate();
    }

    public Placeholders getPlaceholders(PlayerData player, ClassSkill skill) {
        Placeholders holders = new Placeholders();
        holders.register("skill", skill.getSkill().getName());
        holders.register("unlock", "" + skill.getUnlockLevel());
        holders.register("level", player.getSkillLevel(skill.getSkill()));
        holders.register("max_level", skill.getMaxLevel());
        return holders;
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
