package com.pokecraft.onechunk.listeners;

import com.pokecraft.onechunk.AreaManager;
import com.pokecraft.onechunk.OneChunkPlugin;
import com.pokecraft.onechunk.PlayerAreaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final OneChunkPlugin plugin;
    private final AreaManager areaManager;

    public PlayerJoinListener(OneChunkPlugin plugin, AreaManager areaManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        boolean isNew = !areaManager.hasArea(event.getPlayer());

        // loadOrCreate cree/charge le monde du joueur si besoin (doit rester sur le thread principal)
        PlayerAreaData data = areaManager.loadOrCreate(event.getPlayer());

        if (isNew) {
            // On laisse 1 seconde pour que le monde/chunk (0,0) finisse de generer avant de teleporter
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                areaManager.ensureStartingResources(data);
                areaManager.teleportToArea(event.getPlayer(), data);
                event.getPlayer().sendMessage(ChatColor.GOLD + "Bienvenue dans OneChunkSurvival !");
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Objectif 1: recupere 3 rondins de bois.");
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Objectif 2: mine 5 blocs de pierre.");
                event.getPlayer().sendMessage(ChatColor.GRAY + "Chaque bloc mine te donne de l'XP pour agrandir ta zone !");
            }, 20L);
        } else {
            // Le joueur revient : on s'assure juste qu'il est dans son monde
            if (!event.getPlayer().getWorld().getName().equals(data.world)) {
                areaManager.teleportToArea(event.getPlayer(), data);
            }
        }
    }
}
