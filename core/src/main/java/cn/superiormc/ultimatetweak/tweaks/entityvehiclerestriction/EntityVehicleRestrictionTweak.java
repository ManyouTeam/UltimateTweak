package cn.superiormc.ultimatetweak.tweaks.entityvehiclerestriction;

import cn.superiormc.ultimatetweak.managers.MatchEntityManager;
import cn.superiormc.ultimatetweak.tweaks.AbstractTweak;
import cn.superiormc.ultimatetweak.tweaks.TweakEventType;
import cn.superiormc.ultimatetweak.tweaks.config.EntityVehicleRestrictionConfig;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.EnumSet;
import java.util.Set;

public class EntityVehicleRestrictionTweak extends AbstractTweak<EntityVehicleRestrictionConfig> {

    public EntityVehicleRestrictionTweak(EntityVehicleRestrictionConfig config) {
        super("EntityVehicleRestriction", config);
    }

    @Override
    public Set<TweakEventType> getEventTypes() {
        return EnumSet.of(TweakEventType.VEHICLE_ENTER);
    }

    @Override
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof LivingEntity entity) || !blocks(event)) {
            return;
        }
        if (MatchEntityManager.matchEntityManager.getMatch(getConfig().getMatchEntity(), entity)) {
            event.setCancelled(true);
        }
    }

    private boolean blocks(VehicleEnterEvent event) {
        return (event.getVehicle() instanceof Boat && getConfig().blocksVehicle("boat"))
                || (event.getVehicle() instanceof Minecart && getConfig().blocksVehicle("minecart"));
    }
}
