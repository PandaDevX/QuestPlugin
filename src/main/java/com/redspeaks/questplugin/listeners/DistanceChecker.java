package com.redspeaks.questplugin.listeners;

import com.redspeaks.questplugin.QuestPlugin;
import com.redspeaks.questplugin.util.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;

public class DistanceChecker implements Runnable {

    @Override
    public void run() {
        QuestPlugin.attachedQuest.forEach((k, v) -> {
            if(v.getType() != QuestType.RUN_DISTANCE) return;
            v.setProgress(k.getStatistic(Statistic.WALK_ONE_CM) / 100);

            if(v.isComplete()) {
                k.sendMessage(ChatColor.GREEN + "Congratulations for completing the quest!");
                v.getCommandsToRun().forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("{player}", k.getName())));
            }
        });
    }
}
