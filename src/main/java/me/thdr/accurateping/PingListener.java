package me.thdr.accurateping;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PingListener implements PacketListener {

    private final Map<UUID, Map<Short, Long>> transactionTimes = new HashMap<>();
    private final Map<UUID, Long> playerPings = new HashMap<>();
    private final JavaPlugin plugin;

    private final Map<UUID, Short> currentTransactionIdMap = new HashMap<>();

    public PingListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startSendingPackets() {
        int interval = plugin.getConfig().getInt("update-interval");
        Bukkit.getScheduler().runTaskTimer(plugin, this::sendWindowConfirmationPackets, interval, interval);
    }

    private void sendWindowConfirmationPackets() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ClientVersion protocolVersion = PacketEvents.getAPI().getPlayerManager().getUser(player).getClientVersion();
            Short currentTransactionId = currentTransactionIdMap.computeIfAbsent(player.getUniqueId(), k -> (short) -5910);
            if (protocolVersion.isNewerThanOrEquals(ClientVersion.V_1_17)) {
                WrapperPlayServerPing ping = new WrapperPlayServerPing(currentTransactionId);
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, ping);
            } else {
                WrapperPlayServerWindowConfirmation windowConfirmation = new WrapperPlayServerWindowConfirmation(0, currentTransactionId, false);
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, windowConfirmation);
            }
            Map<Short, Long> playerTransactionTimes = transactionTimes.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
            playerTransactionTimes.put(currentTransactionId, System.currentTimeMillis());
            nextTransactionId(player.getUniqueId());
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation packet = new WrapperPlayClientWindowConfirmation(event);
            short actionId = packet.getActionId();
            Map<Short, Long> playerTransactionTimes = transactionTimes.get(event.getUser().getUUID());
            if (playerTransactionTimes != null) {
                Long transactionTime = playerTransactionTimes.remove(actionId);
                if (transactionTime != null) {
                    long ping = System.currentTimeMillis() - transactionTime;
                    playerPings.put(event.getUser().getUUID(), ping);
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong packet = new WrapperPlayClientPong(event);
            short actionId = (short) (packet.getId() & 0xFFFF);
            Map<Short, Long> playerTransactionTimes = transactionTimes.get(event.getUser().getUUID());
            if (playerTransactionTimes != null) {
                Long transactionTime = playerTransactionTimes.remove(actionId);
                if (transactionTime != null) {
                    long pingTime = System.currentTimeMillis() - transactionTime;
                    playerPings.put(event.getUser().getUUID(), pingTime);
                }
            }
        }
    }

    public void nextTransactionId(UUID playerId) {
        short currentTransactionId = currentTransactionIdMap.get(playerId);
        currentTransactionId--;
        currentTransactionIdMap.put(playerId, currentTransactionId);
    }

    public long getPing(Player player) {
        return playerPings.getOrDefault(player.getUniqueId(), -1L);
    }
}