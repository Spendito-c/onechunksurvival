package com.pokecraft.onechunk.commands;

import com.pokecraft.onechunk.AreaManager;
import com.pokecraft.onechunk.PlayerAreaData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MyChunkCommand implements CommandExecutor {

    private final AreaManager areaManager;

    public MyChunkCommand(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande est reservee aux joueurs.");
            return true;
        }

        PlayerAreaData data = areaManager.getData(player);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "Tu n'as pas encore de zone. Reconnecte-toi !");
            return true;
        }

        int required = areaManager.xpRequiredForStage(data.stage);
        player.sendMessage(ChatColor.GOLD + "=== Ta zone OneChunkSurvival ===");
        player.sendMessage(ChatColor.YELLOW + "Stade : " + ChatColor.WHITE + data.stage);
        player.sendMessage(ChatColor.YELLOW + "Rayon actuel : " + ChatColor.WHITE + data.radius + " blocs");
        player.sendMessage(ChatColor.YELLOW + "XP : " + ChatColor.WHITE + data.xp + " / " + required);
        player.sendMessage(ChatColor.YELLOW + "Objectif rondins : " + ChatColor.WHITE +
                (data.logsObjectiveDone ? "Termine" : data.logsCollected + "/3"));
        player.sendMessage(ChatColor.YELLOW + "Objectif pierre : " + ChatColor.WHITE +
                (data.stoneObjectiveDone ? "Termine" : data.stoneCollected + "/5"));
        return true;
    }
}
