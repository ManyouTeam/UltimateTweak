package cn.superiormc.ultimatetweak.utils;

import cn.superiormc.ultimatetweak.UltimateTweak;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerUtil {

    private BukkitTask bukkitTask;

    private ScheduledTask scheduledTask;

    public SchedulerUtil(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public SchedulerUtil(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public void cancel() {
        if (UltimateTweak.isFolia) {
            scheduledTask.cancel();
        } else {
            bukkitTask.cancel();
        }
    }

    // 在主线程上运行任务
    public static void runSync(Runnable task) {
        if (UltimateTweak.isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(UltimateTweak.instance, task);
        } else {
            Bukkit.getScheduler().runTask(UltimateTweak.instance, task);
        }
    }

    public static void runSync(Entity entity, Runnable task) {
        if (UltimateTweak.isFolia) {
            entity.getScheduler().run(UltimateTweak.instance, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(UltimateTweak.instance, task);
        }
    }

    public static void runSync(Location location, Runnable task) {
        if (UltimateTweak.isFolia) {
            Bukkit.getRegionScheduler().run(UltimateTweak.instance, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(UltimateTweak.instance, task);
        }
    }

    // 在异步线程上运行任务
    public static void runTaskAsynchronously(Runnable task) {
        if (UltimateTweak.isFolia) {
            Bukkit.getAsyncScheduler().runNow(UltimateTweak.instance, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(UltimateTweak.instance, task);
        }
    }

    // 延迟执行任务
    public static SchedulerUtil runTaskLater(Runnable task, long delayTicks) {
        if (UltimateTweak.isFolia) {
            return new SchedulerUtil(Bukkit.getGlobalRegionScheduler().runDelayed(UltimateTweak.instance,
                    scheduledTask -> task.run(), delayTicks));
        } else {
            return new SchedulerUtil(Bukkit.getScheduler().runTaskLater(UltimateTweak.instance, task, delayTicks));
        }
    }

    public static SchedulerUtil runTaskLater(Entity entity, Runnable task, long delayTicks) {
        if (UltimateTweak.isFolia) {
            return new SchedulerUtil(entity.getScheduler().runDelayed(UltimateTweak.instance,
                    scheduledTask -> task.run(), null, delayTicks));
        } else {
            return new SchedulerUtil(Bukkit.getScheduler().runTaskLater(UltimateTweak.instance, task, delayTicks));
        }
    }

    // 定时循环任务
    public static SchedulerUtil runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        if (UltimateTweak.isFolia) {
            return new SchedulerUtil(Bukkit.getGlobalRegionScheduler().runAtFixedRate(UltimateTweak.instance,
                    scheduledTask -> task.run(), delayTicks, periodTicks));
        } else {
            return new SchedulerUtil(Bukkit.getScheduler().runTaskTimer(UltimateTweak.instance, task, delayTicks, periodTicks));
        }
    }

    public static SchedulerUtil runTaskTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        if (UltimateTweak.isFolia) {
            return new SchedulerUtil(entity.getScheduler().runAtFixedRate(UltimateTweak.instance,
                    scheduledTask -> task.run(), null, delayTicks, periodTicks));
        } else {
            return new SchedulerUtil(Bukkit.getScheduler().runTaskTimer(
                    UltimateTweak.instance, task, delayTicks, periodTicks));
        }
    }

    // 延迟执行任务
    public static SchedulerUtil runTaskLaterAsynchronously(Runnable task, long delayTicks) {
        if (UltimateTweak.isFolia) {
            return new SchedulerUtil(Bukkit.getGlobalRegionScheduler().runDelayed(UltimateTweak.instance,
                    scheduledTask -> task.run(), delayTicks));
        } else {
            return new SchedulerUtil(Bukkit.getScheduler().runTaskLaterAsynchronously(UltimateTweak.instance, task, delayTicks));
        }
    }

}
