package com.redspeaks.questplugin;

import com.redspeaks.questplugin.commands.AutoClaim;
import com.redspeaks.questplugin.commands.Claim;
import com.redspeaks.questplugin.listeners.DistanceChecker;
import com.redspeaks.questplugin.listeners.EntityListener;
import com.redspeaks.questplugin.listeners.PlayerListener;
import com.redspeaks.questplugin.util.Quest;
import com.redspeaks.questplugin.util.QuestType;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public final class QuestPlugin extends JavaPlugin {

    public static Map<Player, Quest> attachedQuest = new HashMap<>();
    public static Set<Player> autoclaimers = new HashSet<>();
    public static Map<Player, List<Quest>> completedQuest = new HashMap<>();
    public static List<Quest> activeQuests = new ArrayList<>();
    private static HikariDataSource hikari;
    private static QuestPlugin instance = null;

    @Override
    public void onEnable() {
        instance = this;

        // config
        saveDefaultConfig();

        // setup database
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", getConfig().getString("database.host"));
        hikari.addDataSourceProperty("port", getConfig().getInt("database.port"));
        hikari.addDataSourceProperty("databaseName", getConfig().getString("database.database"));
        hikari.addDataSourceProperty("user", getConfig().getString("database.user"));
        hikari.addDataSourceProperty("password", getConfig().getString("database.pass"));

        // setup table
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
           try(Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS Quests(UUID varchar(36), active(100), completed(00)");
           }catch (SQLException e) {
               e.printStackTrace();
           }
        });

        // listeners
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new DistanceChecker(), 0, 1);
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // sql

        // load quests
        loadQuests();
        loadPlayerQuests();

        // saver
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::savePlayerQuests, 3600, 3600);

        // commands
        registerCommand(new Claim());
        registerCommand(new AutoClaim());
    }

    void sendMessage(Player player, String... message) {
        Arrays.stream(message).forEach(m -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', m)));
    }

    public static QuestPlugin getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        savePlayerQuests();

        attachedQuest.clear();
        autoclaimers.clear();
        completedQuest.clear();
        activeQuests.clear();

        if(hikari != null) {
            hikari.close();
        }
    }

    void loadQuests() {
        activeQuests.clear();
        for(String key : getConfig().getConfigurationSection("Quests").getKeys(false)) {
            QuestType type = QuestType.valueOf(getConfig().getString("Quests." + key + ".type").toUpperCase());
            int target = getConfig().getInt("Quests." + key + ".type");
            List<String> commands = getConfig().getStringList("Quests." + key + ".reward-commands");

            activeQuests.add(new Quest(type, target, commands, key));
        }

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !attachedQuest.containsKey(p))
                    .filter(p -> autoclaimers.contains(p))
                    .forEach(p -> {
                        Optional<Quest> optionalQuest = activeQuests.stream().filter(q -> !isQuestComplete(p, q))
                                .findAny();

                        optionalQuest.ifPresent(q -> {
                            attachedQuest.put(p, q);
                            sendMessage(p, "&aYou have been assigned to a new quest.", "", "&7Information:",
                                    "&7Type: &b" + q.getType().toString().toLowerCase(),
                                    "&7Target: &b" + q.getTarget());
                        });
                    });
        }, 0, 1);
    }

    void savePlayerQuests() {
        Bukkit.getScheduler().runTaskAsynchronously(QuestPlugin.getInstance(), () -> {

            String statement = "INSERT INTO Quests VALUES(?,?,?) ON DUPLICATE KEY UPDATE active=?";
            try(Connection connection = hikari.getConnection(); PreparedStatement ps = connection.prepareStatement(statement)) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    Quest quest = attachedQuest.get(player);
                    ps.setString(1, player.getUniqueId().toString());
                    List<Quest> questsComplete = completedQuest.getOrDefault(player, new ArrayList<>());
                    if(quest != null) {
                        ps.setString(2, quest.toString());
                        ps.setString(4, quest.toString());
                    } else {
                        ps.setString(2, null);
                        ps.setString(2, null);
                    }
                    if(!questsComplete.isEmpty()) {
                        ps.setString(3, questsComplete.stream()
                                .map(Quest::getId).collect(Collectors.joining(",")));
                    } else {
                        ps.setString(3, null);
                    }
                    attachedQuest.remove(player);
                    ps.executeUpdate();
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    public static void saveQuests(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(QuestPlugin.getInstance(), () -> {
            Quest quest = attachedQuest.get(player);
            String statement = "INSERT INTO Quests VALUES(?,?,?) ON DUPLICATE KEY UPDATE active=?";
            try(Connection connection = hikari.getConnection(); PreparedStatement ps = connection.prepareStatement(statement)) {
                ps.setString(1, player.getUniqueId().toString());
                List<Quest> questsComplete = completedQuest.getOrDefault(player, new ArrayList<>());
                if(quest != null) {
                    ps.setString(2, quest.toString());
                    ps.setString(4, quest.toString());
                } else {
                    ps.setString(2, null);
                    ps.setString(2, null);
                }
                if(!questsComplete.isEmpty()) {
                    ps.setString(3, questsComplete.stream()
                            .map(Quest::getId).collect(Collectors.joining(",")));
                } else {
                    ps.setString(3, null);
                }
                ps.executeUpdate();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        });
        attachedQuest.remove(player);
    }

    void loadPlayerQuests() {
        Bukkit.getScheduler().runTaskAsynchronously(QuestPlugin.getInstance(), () -> {
            String statement = "SELECT * FROM Quests WHERE uuid=?";
            try(Connection connection = hikari.getConnection(); PreparedStatement ps = connection.prepareStatement(statement)) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    ps.setString(1, player.getUniqueId().toString());
                    try(ResultSet resultSet = ps.executeQuery()) {
                        if(resultSet.next()) {
                            Quest quest = Quest.fromString(resultSet.getString("active"));
                            if(quest != null) {
                                Optional<Quest> optionalQuest = activeQuests.stream().filter(q -> q.equals(quest)).findAny();
                                optionalQuest.ifPresent(value -> attachedQuest.put(player, value));
                            }
                            String completed = resultSet.getString("completed");
                            if(completed != null) {
                                List<Quest> list = Arrays.asList(completed.split(","))
                                        .stream().map(s -> activeQuests.stream().filter(q -> q.getId().equals(s)).findAny().orElse(null))
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                                if(!list.isEmpty()) {
                                    completedQuest.put(player, list);
                                }
                            }
                        }
                    }
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    boolean isQuestComplete(Player player, Quest quest) {
        List<Quest> completedQuests = completedQuest.getOrDefault(player, new ArrayList<>());
        if(completedQuests.isEmpty()) {
            return false;
        }
        return completedQuests.stream()
                .anyMatch(q -> q.getId().equals(quest.getId()));
    }

    public static void loadQuests(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(QuestPlugin.getInstance(), () -> {
            String statement = "SELECT * FROM Quests WHERE uuid=?";
            try(Connection connection = hikari.getConnection(); PreparedStatement ps = connection.prepareStatement(statement)) {
                ps.setString(1, player.getUniqueId().toString());
                try(ResultSet resultSet = ps.executeQuery()) {
                    if(resultSet.next()) {
                        Quest quest = Quest.fromString(resultSet.getString("active"));
                        if(quest != null) {
                            Optional<Quest> optionalQuest = activeQuests.stream().filter(q -> q.equals(quest)).findAny();
                            optionalQuest.ifPresent(value -> attachedQuest.put(player, value));
                        }
                        String completed = resultSet.getString("completed");
                        if(completed != null) {
                            List<Quest> list = Arrays.asList(completed.split(","))
                                    .stream().map(s -> activeQuests.stream().filter(q -> q.getId().equals(s)).findAny().orElse(null))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            if(!list.isEmpty()) {
                                completedQuest.put(player, list);
                            }
                        }
                    }
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void registerCommand(AbstractCommand command) {
        getCommand(command.getCommand()).setExecutor(command);
    }
}
