package cn.superiormc.ultimatetweak.managers;

import cn.superiormc.ultimatetweak.UltimateTweak;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeModifyManager {

    public static AttributeModifyManager attributeModifyManager;

    private final Map<UUID, Map<String, ActiveModifier>> activeModifiers = new ConcurrentHashMap<>();

    public AttributeModifyManager() {
        attributeModifyManager = this;
    }

    public void start(Player player,
                      String sessionKey,
                      Attribute attribute,
                      double amount,
                      AttributeModifier.Operation operation) {
        start(player, sessionKey, createModifierKey(sessionKey), attribute, amount, operation);
    }

    public void start(Player player,
                      String sessionKey,
                      NamespacedKey modifierKey,
                      Attribute attribute,
                      double amount,
                      AttributeModifier.Operation operation) {
        cancel(player.getUniqueId(), sessionKey);
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance == null) {
            return;
        }

        attributeInstance.removeModifier(modifierKey);
        attributeInstance.addTransientModifier(new AttributeModifier(modifierKey, amount, operation));
        activeModifiers.computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .put(sessionKey, new ActiveModifier(modifierKey, attribute));
    }

    public void cancel(UUID playerId, String sessionKey) {
        Map<String, ActiveModifier> playerModifiers = activeModifiers.get(playerId);
        if (playerModifiers == null) {
            return;
        }

        if (sessionKey == null) {
            for (String key : new ArrayList<>(playerModifiers.keySet())) {
                cancel(playerId, key);
            }
            return;
        }

        ActiveModifier activeModifier = playerModifiers.remove(sessionKey);
        if (activeModifier == null) {
            return;
        }
        if (playerModifiers.isEmpty()) {
            activeModifiers.remove(playerId, playerModifiers);
        }

        Player player = UltimateTweak.instance.getServer().getPlayer(playerId);
        if (player == null) {
            return;
        }
        AttributeInstance attributeInstance = player.getAttribute(activeModifier.attribute());
        if (attributeInstance != null) {
            attributeInstance.removeModifier(activeModifier.modifierKey());
        }
    }

    public void shutdown() {
        for (UUID playerId : new ArrayList<>(activeModifiers.keySet())) {
            cancel(playerId, null);
        }
    }

    private NamespacedKey createModifierKey(String sessionKey) {
        UUID id = UUID.nameUUIDFromBytes(sessionKey.getBytes(StandardCharsets.UTF_8));
        return new NamespacedKey(UltimateTweak.instance, "attribute_" + id.toString().replace("-", ""));
    }

    private record ActiveModifier(NamespacedKey modifierKey, Attribute attribute) {
    }
}
