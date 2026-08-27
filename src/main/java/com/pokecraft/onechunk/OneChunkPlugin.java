package com.pokecraft.onechunk;

import com.pokecraft.onechunk.commands.MyChunkCommand;
import com.pokecraft.onechunk.listeners.BlockBreakListener;
import com.pokecraft.onechunk.listeners.BorderListener;
import com.pokecraft.onechunk.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneChunkPlugin extends JavaPlugin {

    private AreaManager areaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.areaManager = new AreaManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, areaManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(areaManager), this);
        getServer().getPluginManager().registerEvents(new BorderListener(this, areaManager), this);

        MyChunkCommand myChunkCmd = new MyChunkCommand(areaManager);
        if (getCommand("mychunk") != null) {
            getCommand("mychunk").setExecutor(myChunkCmd);
        }

        getLogger().info("OneChunkSurvival active !");
    }

    @Override
    public void onDisable() {
        if (areaManager != null) {
            areaManager.saveAll();
        }
    }

    public AreaManager getAreaManager() {
        return areaManager;
    }
}
