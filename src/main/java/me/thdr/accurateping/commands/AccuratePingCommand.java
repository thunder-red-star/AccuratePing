package me.thdr.accurateping.commands;

import me.thdr.accurateping.PingListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class AccuratePingCommand implements CommandExecutor {

    private final PingListener pingListener;
    private final FileConfiguration langConfig;

    public AccuratePingCommand(PingListener pingListener, FileConfiguration langConfig) {
        this.pingListener = pingListener;
        this.langConfig = langConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(langConfig.getString("messages.player-not-found"));
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(langConfig.getString("messages.not-a-player"));
            return true;
        }

        long ping = pingListener.getPing(target);
        String message = target.equals(sender)
                ? langConfig.getString("messages.ping-self")
                : langConfig.getString("messages.ping-other");
        sender.sendMessage(message.replace("%player%", target.getName()).replace("%ping%", String.valueOf(ping)));
        return true;
    }
}