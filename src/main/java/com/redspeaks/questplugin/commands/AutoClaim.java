package com.redspeaks.questplugin.commands;

import com.redspeaks.questplugin.AbstractCommand;
import com.redspeaks.questplugin.QuestPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AutoClaim extends AbstractCommand {

    public AutoClaim() {
        super("autoclaim", "quest.autoclaim", true);
    }

    @Override
    protected void run(Player player, String[] args) {
        if(QuestPlugin.autoclaimers.contains(player)) {
            QuestPlugin.autoclaimers.remove(player);
            player.sendMessage(ChatColor.RED + "You disabled autoclaiming quest.");
            return;
        }
        QuestPlugin.autoclaimers.add(player);
        player.sendMessage(ChatColor.GREEN + "You enabled autoclaiming quest.");
    }
}
