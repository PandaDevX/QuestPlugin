package com.redspeaks.questplugin.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Quest {

    private final QuestType type;
    private final int target;
    private int progress;
    private final List<String> commandsToRun;
    private final String id;
    private boolean isComplete;
    public Quest(QuestType type, int target, List<String> commandsToRun, String id) {
        this(type, target, 0, commandsToRun, id);
    }

    Quest(QuestType type, int target, int progress, List<String> commandsToRun, String id) {
        this.type = type;
        this.progress = progress;
        this.target = target;
        this.commandsToRun = commandsToRun;
        this.id = id;
        this.isComplete = progress >= target;
    }

    public int getProgress() {
        return progress;
    }

    public int getTarget() {
        return target;
    }

    public void addProgress() {
        if((progress + 1) == getTarget()) {
            complete();
            return;
        }
        this.progress += 1;
    }

    public void setProgress(int progress) {
        if(progress >= getTarget()) {
            complete();
            return;
        }
        this.progress = progress;
    }

    public void complete() {
        this.isComplete = true;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return getId() + "," + type.toString() + "," + getTarget() + "," + getProgress() + "," + String.join(",", commandsToRun);
    }

    public QuestType getType() {
        return type;
    }

    public List<String> getCommandsToRun() {
        return commandsToRun;
    }

    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(!(o instanceof Quest)) {
            return false;
        }
        Quest toCompare = (Quest)o;
        return toString().equals(toCompare.toString());
    }

    public static Quest fromString(String quest) {
        if(quest == null) {
            return null;
        }
        String[] data = quest.split(",");

        String id = data[0];
        QuestType type = QuestType.valueOf(data[1]);
        int target = Integer.parseInt(data[2]);
        int progress = Integer.parseInt(data[3]);
        List<String> commandsToRun = new ArrayList<>(Arrays.asList(data).subList(4, data.length));
        return new Quest(type, target, progress, commandsToRun, id);
    }
}
