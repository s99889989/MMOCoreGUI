package com.daxton.mmocoregui;

import com.daxton.mmocoregui.command.CommandMain;
import com.daxton.mmocoregui.command.TabMain;
import com.daxton.mmocoregui.controller.GUIController;
import com.daxton.mmocoregui.listener.PlayerListener;
import com.daxton.unrealcore.UnrealCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MMOCoreGUI extends JavaPlugin {

    public static UnrealCorePlugin unrealCorePlugin;

    @Override
    public void onEnable() {

        unrealCorePlugin = new UnrealCorePlugin(this);

        Objects.requireNonNull(Bukkit.getPluginCommand("mmocoregui")).setExecutor(new CommandMain());
        Objects.requireNonNull(Bukkit.getPluginCommand("mmocoregui")).setTabCompleter(new TabMain());

        GUIController.load();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), unrealCorePlugin.getJavaPlugin());

    }

    @Override
    public void onDisable() {



    }
}
