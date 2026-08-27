package com.pokecraft.onechunk.listeners;

import com.pokecraft.onechunk.AreaManager;
import com.pokecraft.onechunk.PlayerAreaData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final AreaManager areaManager;

    public BlockBreakListener(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerAreaData data = areaManager.getData(player);
        if (data == null) return;

        Material type = event.getBlock().getType();

        if (!data.logsObjectiveDone && AreaManager.isLog(type)) {
            data.logsCollected++;
            if (data.logsCollected >= 3) {
                data.logsObjectiveDone = true;
                player.sendMessage(ChatColor.GREEN + "Objectif rempli : 3 rondins recuperes !");
            }
        }

        if (!data.stoneObjectiveDone && AreaManager.isStone(type)) {
            data.stoneCollected++;
            if (data.stoneCollected >= 5) {
                data.stoneObjectiveDone = true;
                player.sendMessage(ChatColor.GREEN + "Objectif rempli : pierre minee !");
            }
        }

        int xp = areaManager.getXpPerBlock(type);
        if (xp > 0) {
            areaManager.addXp(player, data, xp);
        } else {
            areaManager.save(data);
        }
    }
}
