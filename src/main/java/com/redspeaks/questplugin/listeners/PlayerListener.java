package com.redspeaks.questplugin.listeners;

import com.redspeaks.questplugin.QuestPlugin;
import com.redspeaks.questplugin.util.Quest;
import com.redspeaks.questplugin.util.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        QuestPlugin.saveQuests(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        QuestPlugin.loadQuests(e.getPlayer());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Quest quest = QuestPlugin.attachedQuest.get(e.getPlayer());
        if(quest == null) return;

        if(quest.getType() != QuestType.PLACE_BLOCKS) return;
        quest.addProgress();

        if(quest.isComplete()) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "Congratulations on completing the quest!");
            quest.getCommandsToRun().forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("{player}", e.getPlayer().getName())));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Quest quest = QuestPlugin.attachedQuest.get(e.getPlayer());
        if(quest == null) return;

        if(quest.getType() != QuestType.BREAK_BLOCKS) return;
        quest.addProgress();
        if(quest.isComplete()) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "Congratulations on completing the quest!");
            quest.getCommandsToRun().forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("{player}", e.getPlayer().getName())));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Quest quest = QuestPlugin.attachedQuest.get(e.getPlayer());
        if(quest == null) return;

        if(quest.getType() != QuestType.COMMANDS_RAN) return;
        quest.addProgress();
        if(quest.isComplete()) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "Congratulations on completing the quest!");
            quest.getCommandsToRun().forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("{player}", e.getPlayer().getName())));
        }
    }
}
