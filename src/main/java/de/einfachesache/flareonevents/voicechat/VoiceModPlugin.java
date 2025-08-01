package de.einfachesache.flareonevents.voicechat;

import de.einfachesache.flareonevents.Config;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceModPlugin implements VoicechatPlugin {

    private static VoicechatServerApi serverApi;
    public static final Map<Integer, Group> TEAM_VOICE_GROUP = new ConcurrentHashMap<>();

    public static void registerVoiceChatListener(Plugin plugin) {
        BukkitVoicechatService svc = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);

        if (svc == null) return;

        svc.registerPlugin(new VoiceModPlugin());
    }

    @Override
    public void initialize(VoicechatApi api) {
        serverApi = (VoicechatServerApi) api;
        loadGroups();
    }

    public void loadGroups() {
        Config.getTeams().keySet().forEach(VoiceModPlugin::createGroup);
    }

    public static void createGroup(int teamId) {
        TEAM_VOICE_GROUP.put(teamId, serverApi.groupBuilder()
                .setPersistent(true)
                .setName(String.valueOf(teamId))
                .setType(Group.Type.OPEN)
                .setPassword(UUID.randomUUID().toString())
                .build());
    }

    public static void deleteGroup(int teamId) {
        Group gruppe = TEAM_VOICE_GROUP.remove(teamId);
        if (gruppe != null) {
            serverApi.removeGroup(gruppe.getId());
        }
    }

    public static void addPlayerToTeam(UUID playerUUID, int teamID) {
        VoicechatConnection conn = serverApi.getConnectionOf(playerUUID);
        if (conn != null) {
            conn.setGroup(TEAM_VOICE_GROUP.get(teamID));
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(Component.text("Du bist jetzt in der Voice-Gruppe #" + teamID + ".", NamedTextColor.GREEN));
        }
    }

    public static void removePlayerToTeam(UUID playerUUID, int teamID) {
        VoicechatConnection conn = serverApi.getConnectionOf(playerUUID);
        if (conn != null) {
            conn.setGroup(null);
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(Component.text("Du hast die Voice-Gruppe #" + teamID + " verlassen.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public void registerEvents(EventRegistration reg) {

        reg.registerEvent(CreateGroupEvent.class, createGroupEvent -> {
            if (createGroupEvent.getConnection() == null) return;

            createGroupEvent.cancel();
        });

        reg.registerEvent(PlayerConnectedEvent.class, playerConnectedEvent -> {

            VoicechatConnection con = playerConnectedEvent.getConnection();
            if (con == null) return;

            int teamID = Config.getPlayerTeams().getOrDefault(con.getPlayer().getUuid(), -1);
            if (teamID != -1) {
                addPlayerToTeam(con.getPlayer().getUuid(), teamID);
            }
        });

        reg.registerEvent(LeaveGroupEvent.class, leaveGroupEvent -> {

            VoicechatConnection con = leaveGroupEvent.getConnection();
            if (con == null) return;

            Group team = leaveGroupEvent.getGroup();
            if (team == null) return;

            int teamID = Config.getPlayerTeams().getOrDefault(con.getPlayer().getUuid(), -1);
            if (teamID == -1) return;

            if (Objects.equals(team.getName(), String.valueOf(teamID))) {
                leaveGroupEvent.cancel();
            }
        });
    }

    @Override
    public String getPluginId() {
        return "flareon_events_listener";
    }
}