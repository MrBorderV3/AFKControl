package me.border.afkcontrol.util;

import me.border.utilities.scheduler.TaskBuilder;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Class used to calculate TPS of a server at any given second.
 * This class can only have 1 instance active at any given time, not more is needed since they will produce the same results.
 *
 * WIP - More features to be added later like getting the TPS (X) time ago.
 * TODO - MOVE TO SPIGOTUTILITIES
 */
public class TPSCounter  {

    private static TPSCounter instance;

    /**
     * Return the current TPSCounter instance or create one and assign it to {@link #instance} if it hasn't been created yet.
     *
     * @return The instance
     */
    public static TPSCounter create(){
        if (instance == null){
            instance = new TPSCounter();
        }

        return instance;
    }

    /**
     * Indicates whether this counter is operating or not
     */
    private volatile boolean stopped = true;

    private int ticksPassed;
    private double tps;

    private TPSCounter(){
    }

    /**
     * Start this TPSCounter
     */
    public void start(){
        stopped = false;
        me.border.spigotutilities.task.TaskBuilder.builder()
                .after(0)
                .every(1)
                .runnable(new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (stopped) {
                            cancel();
                            return;
                        }

                        ticksPassed += 1;
                    }
                })
                .run();

        TaskBuilder.builder()
                .async()
                .after(0, TimeUnit.MILLISECONDS)
                .every(1, TimeUnit.SECONDS)
                .task(new TimerTask() {
                    @Override
                    public void run() {
                        if (stopped) {
                            cancel();
                            return;
                        }

                        tps = ticksPassed;
                        ticksPassed = 0;
                    }
                })
                .build();
    }

    /**
     * Stop this TPSCounter
     */
    public void stop(){
        stopped = true;
        ticksPassed = 0;
        tps = 0;
    }

    /**
     * Get the latest recorded TPS
     *
     * @return the TPS.
     */
    public double getTPS(){
        return tps;
    }

    public static TPSCounter getInstance(){
        return instance;
    }
}
