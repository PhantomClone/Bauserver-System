package me.phantomclone.minewars.buildserversystem.skincache;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.apache.commons.io.IOUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record SkinCacheImpl(Map<String, UUID> playerNameUuidCache, Map<UUID, String> playerUuidSkinValueString)
        implements SkinCache, Listener {

    public SkinCacheImpl(JavaPlugin javaPlugin) {
        this(new HashMap<>(), new HashMap<>());
        javaPlugin.getServer().getPluginManager().registerEvents(this, javaPlugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        playerNameUuidCache().put(playerJoinEvent.getPlayer().getName(), playerJoinEvent.getPlayer().getUniqueId());
        playerJoinEvent.getPlayer().getPlayerProfile().getProperties().stream()
                .filter(profileProperty -> "textures".equalsIgnoreCase(profileProperty.getName()))
                .map(ProfileProperty::getValue).findFirst()
                .ifPresent(value -> playerUuidSkinValueString().put(playerJoinEvent.getPlayer().getUniqueId(), value));
    }

    @Override
    public UUID playerUuidOfPlayerName(String playerName, boolean fromMojang) {
        if (!fromMojang && playerNameUuidCache().containsKey(playerName))
            return playerNameUuidCache().get(playerName);
        try {
            final String json = IOUtils.toString(new URL(
                    String.format("https://api.mojang.com/users/profiles/minecraft/%s", playerName)
            ), StandardCharsets.UTF_8);
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
            final String uuidString = (String) jsonObject.get("id");
            final UUID uuid = UUID.fromString(uuidString);
            playerNameUuidCache().put(playerName, uuid);
            return uuid;
        } catch (IOException | ParseException e) {
            return null;
        }
    }

    @Override
    public String playerNameOfPlayerUuid(UUID playerUuid, boolean fromSessionServer) {
        Optional<String> optionalPlayerName;
        if (!fromSessionServer &&
                (optionalPlayerName = playerNameUuidCache().entrySet().stream()
                        .filter(entry -> entry.getValue().equals(playerUuid)).map(Map.Entry::getKey).findFirst())
                        .isPresent()) {
            return optionalPlayerName.get();
        }
        try {
            final String json = IOUtils.toString(new URL(
                            String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", playerUuid.toString())),
                    StandardCharsets.UTF_8);
            final JSONObject jsonObject = ((JSONObject) new JSONParser().parse(json));
            final String playerName = (String) jsonObject.get("name");
            playerNameUuidCache.put(playerName, playerUuid);
            return playerName;
        } catch (IOException | ParseException e) {
            return null;
        }
    }

    @Override
    public String skinValueOfPlayerUuid(UUID playerUuid, boolean fromSessionServer) {
        if (!fromSessionServer && playerUuidSkinValueString().containsKey(playerUuid))
            return playerUuidSkinValueString().get(playerUuid);
        try {
            final String json = IOUtils.toString(new URL(
                        String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", playerUuid.toString())),
                StandardCharsets.UTF_8);
            final JSONArray array = (JSONArray) ((JSONObject) new JSONParser().parse(json)).get("properties");
            final JSONObject jsonObject = (JSONObject) array.get(0);
            final String skinValueString = (String) jsonObject.get("value");
            playerUuidSkinValueString().put(playerUuid, skinValueString);
            return skinValueString;
        } catch (IOException | ParseException e) {
            return null;
        }
    }
}
