package com.redspeaks.questplugin.commands;

import com.redspeaks.questplugin.AbstractCommand;
import com.redspeaks.questplugin.QuestPlugin;
import com.redspeaks.questplugin.util.Quest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Claim extends AbstractCommand {

    public Claim() {
        super("claim", "quest.claim", true, 1);
    }

    @Override
    public void run(Player player, String[] args) {
        String id = args[0];

        Optional<Quest> optionalQuest = QuestPlugin.activeQuests.stream().filter(q -> q.getId().equals(id)).findAny();
        if(!optionalQuest.isPresent()) {
            sendMessage(player, "&cNo quest found by the name of " + id);
            return;
        }
        if(isQuestComplete(player, optionalQuest.get())) {
            sendMessage(player, "&cYou already completed that quest id.");
            return;
        }

        QuestPlugin.attachedQuest.put(player, optionalQuest.get());
        sendMessage(player, "&aYou have been assigned to a new quest.", "", "&7Information:",
                "&7Type: &b" + optionalQuest.get().getType().toString().toLowerCase(),
                "&7Target: &b" + optionalQuest.get().getTarget());
    }

    @Override
    protected String getUsage() {
        return "/claim <quest-id>";
    }

    boolean isQuestComplete(Player player, Quest quest) {
        List<Quest> completedQuests = QuestPlugin.completedQuest.getOrDefault(player, new ArrayList<>());
        if(completedQuests.isEmpty()) {
            return false;
        }
        return completedQuests.stream()
                .anyMatch(q -> q.getId().equals(quest.getId()));
    }
}
