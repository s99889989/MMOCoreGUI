package com.daxton.mmocoregui.been.type;

public enum MMOCoreGUIType {

    //屬性, 好友, 組隊建立, 組隊查看, 公會建立, 公會查看, 任務, 選擇職業, 技能, 航點
    ATTRIBUTES, FRIENDS, PARTY_CREATION, PARTY_VIEW, GUILD_CREATION, GUILD_VIEW, QUESTS, SELECT_CLASS, SKILLS, WAYPOINTS;

    public static MMOCoreGUIType fromString(String string) {
        try {
            return valueOf(string.toUpperCase());
        }catch (IllegalArgumentException e) {
            return ATTRIBUTES;
        }
    }

}
