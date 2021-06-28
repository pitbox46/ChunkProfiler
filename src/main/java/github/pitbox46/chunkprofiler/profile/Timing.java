package github.pitbox46.chunkprofiler.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Timing {
    private static final Logger LOGGER = LogManager.getLogger();

    private boolean timing;
    private long tick;
    private long startTime;
    private final Map<Long,Long> times = new HashMap<>();

    public void startTiming(long tick) {
        this.tick = tick;
        timing = true;
        startTime = System.nanoTime();
    }

    public void endTiming() {
        if(timing) {
            times.put(tick, System.nanoTime() - startTime);
            timing = false;
        }
    }

    public long getLastTime() {
        List<Long> list = new ArrayList<>(times.values());
        return list.get(list.size() - 1);
    }

    public Map<Long,Long> getTimes() {
        return new HashMap<>(times);
    }
}
