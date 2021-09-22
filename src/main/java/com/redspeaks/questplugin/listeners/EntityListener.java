package com.redspeaks.questplugin.listeners;

import com.redspeaks.questplugin.QuestPlugin;
import com.redspeaks.questplugin.util.Quest;
import com.redspeaks.questplugin.util.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if(e.getEntity().getKiller() == null) return;

        Quest quest = QuestPlugin.attachedQuest.get(e.getEntity().getKiller());
        if(quest == null) return;

        if(quest.getType() != QuestType.KILL) return;
        quest.addProgress();

        if(quest.isComplete()) {
            e.getEntity().getKiller().sendMessage(ChatColor.GREEN + "Congratulions for completing the quest!");
            quest.getCommandsToRun().forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("{player}", e.getEntity().getKiller().getName())));
        }
    }
}
