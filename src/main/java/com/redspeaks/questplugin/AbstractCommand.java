package com.redspeaks.questplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class AbstractCommand implements CommandExecutor {

    private final String command, permission;
    private final boolean requiredPlayer;
    private final int requiredArgs;
    public AbstractCommand(String command, String permission, boolean requiredPlayer) {
        this(command, permission, requiredPlayer, 0);
    }

    public AbstractCommand(String command, String permission, boolean requiredPlayer, int requiredArgs) {
        this.command = command;
        this.permission = permission;
        this.requiredPlayer = requiredPlayer;
        this.requiredArgs =requiredArgs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have no permission to do that!"));
            return true;
        }
        if(requiredPlayer) {
            if(!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou must be a player to do that!"));
                return true;
            }
            if(args.length < requiredArgs) {
                sender.sendMessage(getUsage());
                return true;
            }
            run((Player) sender, args);
            return true;
        }
        if(args.length < requiredArgs) {
            sender.sendMessage(getUsage());
            return true;
        }
        run(sender, args);
        return false;
    }

    protected void run(CommandSender sender, String[] args) {}
    protected void run(Player player, String[] args) {}

    protected void sendMessage(Player player, String... message) {
        Arrays.stream(message).forEach(m -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', m)));
    }

    protected String getUsage() {
        return null;
    }

    public String getCommand() {
        return command;
    }
}
