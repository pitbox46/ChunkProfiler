package github.pitbox46.chunkprofiler.profile;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Timer<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Timer<Entity> ENTITY_TIMER = new Timer<>();
    public static final Timer<TileEntity> TILE_ENTITY_TIMER = new Timer<>();

    private final Map<T, Timing> timings = new HashMap<>();
    private boolean enabled;

    public Timer() {}

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public void startTime(T object, long tick) {
        if(enabled) {
            timings.putIfAbsent(object, new Timing());
            timings.get(object).startTiming(tick);
        }
    }

    public void endTime(T object) {
        if(enabled) {
            timings.get(object).endTiming();
        }
    }

    public void reset() {
        this.timings.clear();
        this.enabled = false;
    }

    public HashMap<T,Timing> getTimings() {
        return new HashMap<>(timings);
    }
}
