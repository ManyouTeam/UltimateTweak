package cn.superiormc.ultimatetweak.tweaks.config;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class EntityVehicleRestrictionConfig extends AbstractTweakConfig {

    private Set<String> vehicles = Collections.emptySet();

    public EntityVehicleRestrictionConfig(File file) {
        super("EntityVehicleRestriction", file);
        reload();
    }

    @Override
    public void reload() {
        super.reload();
        Set<String> loadedVehicles = new LinkedHashSet<>();
        for (String vehicle : getConfig().getStringList("vehicles")) {
            loadedVehicles.add(vehicle.toLowerCase(Locale.ENGLISH));
        }
        vehicles = Collections.unmodifiableSet(loadedVehicles);
    }

    public ConfigurationSection getMatchEntity() {
        return getConfig().getConfigurationSection("match-entity");
    }

    public boolean blocksVehicle(String vehicle) {
        return vehicles.contains(vehicle);
    }
}
