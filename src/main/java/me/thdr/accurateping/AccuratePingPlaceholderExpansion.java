package me.thdr.accurateping;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AccuratePingPlaceholderExpansion extends PlaceholderExpansion {

    private final AccuratePing plugin;

    public AccuratePingPlaceholderExpansion(AccuratePing plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "accurateping";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ThunderRedStar";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // %accurateping_ping%
        if ("ping".equals(identifier)) {
            return String.valueOf(plugin.getPingListener().getPing(player));
        }

        return null;
    }
}