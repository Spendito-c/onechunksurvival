package com.pokecraft.onechunk.listeners;

import com.pokecraft.onechunk.AreaManager;
import com.pokecraft.onechunk.OneChunkPlugin;
import com.pokecraft.onechunk.PlayerAreaData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BorderListener implements Listener {

    private final OneChunkPlugin plugin;
    private final AreaManager areaManager;
    private final Map<UUID, Long> lastWarning = new HashMap<>();

    public BorderListener(OneChunkPlugin plugin, AreaManager areaManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        PlayerAreaData data = areaManager.getData(player);
        if (data == null) return;

        if (areaManager.isOutside(data, event.getTo())) {
            event.setCancelled(true);
            Location safe = event.getFrom();
            player.teleport(safe);

            long now = System.currentTimeMillis();
            long last = lastWarning.getOrDefault(player.getUniqueId(), 0L);
            if (now - last > 3000) {
                lastWarning.put(player.getUniqueId(), now);
                String msg = plugin.getConfig().getString("messages.border-blocked",
                        "&cTu ne peux pas sortir de ta zone pour l'instant !");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        }
    }
}
