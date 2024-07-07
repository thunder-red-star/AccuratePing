package me.thdr.accurateping;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.sun.tools.jdi.Packet;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

import me.thdr.accurateping.commands.AccuratePingCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class AccuratePing extends JavaPlugin {
    private FileConfiguration langConfig = null;
    private File langFile = null;
    private PingListener pingListener;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Load config
        saveDefaultConfig();

        // Get config value "enable-skript", if true then load Skript
        if (getConfig().getBoolean("enable-skript")) {
            if (Skript.isAcceptRegistrations()) {
                SkriptAddon skriptAddon = Skript.registerAddon(this);
                try {
                    skriptAddon.loadClasses("me.thdr.accurateping.skript");
                    getLogger().info("Loaded Skript classes");
                } catch (IOException e) {
                    getLogger().severe("Some weird stuff happened while loading Skript classes, see stacktrace below");
                    throw new RuntimeException(e);
                }
                skriptAddon.setLanguageFileDirectory("lang/skript");
            }
        }

        // Load lang.yml
        this.saveResource("lang/lang.yml", false);
        langFile = new File(getDataFolder(), "lang/lang.yml");
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Create a new instance of PingListener, this will store all the player pings
        pingListener = new PingListener(this);

        // Register the ping listener with packetevents
        PacketEvents.getAPI().getEventManager().registerListener(pingListener, PacketListenerPriority.HIGHEST);
        PacketEvents.getAPI().init();

        pingListener.startSendingPackets();

        // Register the ping command
        Objects.requireNonNull(this.getCommand("accurateping")).setExecutor(new AccuratePingCommand(pingListener, langConfig));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AccuratePingPlaceholderExpansion(this).register();
        }

        getLogger().info("AccuratePing has been enabled");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public PingListener getPingListener() {
        return pingListener;
    }
}