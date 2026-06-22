package cn.superiormc.ultimatetweak.hooks.protection;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProtectionPlotSquaredHook extends AbstractProtectionHook {

    public ProtectionPlotSquaredHook() {
        super("PlotSquared");
    }

    @Override
    public boolean canUse(Player player, org.bukkit.Location location) {
        BukkitPlayer bukkitPlayer = BukkitUtil.adapt(player);
        Plot plot = bukkitPlayer.getCurrentPlot();
        if (plot != null) {
            return plot.isAdded(player.getUniqueId());
        }
        return true;
    }

    @Override
    public ProtectionRegionResult createRegion(ProtectionRegion region) {
        Location location = BukkitUtil.adaptComplete(region.centerLocation());
        PlotArea plotArea = PlotSquared.get().getPlotAreaManager().getApplicablePlotArea(location);
        if (plotArea == null) {
            return ProtectionRegionResult.failed("Location is not inside a PlotSquared plot area: " + region.id());
        }
        PlotId plotId = plotArea.getPlotManager().getPlotIdAbs(location.getX(), location.getY(), location.getZ());
        if (plotId == null) {
            return ProtectionRegionResult.failed("Location is not inside a claimable PlotSquared plot: " + region.id());
        }
        Plot plot = plotArea.getPlotAbs(plotId);
        if (plot != null && plot.hasOwner()) {
            return plot.isOwner(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    ? ProtectionRegionResult.alreadyExistsResult()
                    : ProtectionRegionResult.failed("PlotSquared plot already has an owner: " + plotId);
        }
        if (plot == null) {
            plot = new Plot(plotArea, plotId);
        }
        return plot.getPlotModificationManager().create(UUID.fromString("00000000-0000-0000-0000-000000000000"), false)
                ? ProtectionRegionResult.created()
                : ProtectionRegionResult.failed("PlotSquared refused to claim plot: " + plotId);
    }

}
