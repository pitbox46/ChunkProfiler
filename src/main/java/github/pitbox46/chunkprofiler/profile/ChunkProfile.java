package github.pitbox46.chunkprofiler.profile;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChunkProfile {
    private static final Logger LOGGER = LogManager.getLogger("Chunk Profiler");

    public final ChunkPos chunkPos;
    public final RegistryKey<World> dimension;
    public int entityCount;
    public int tileEntityCount;
    private final Map<Long,Long> timeToCompleteTick = new HashMap<>();

    public ChunkProfile(ChunkPos chunkPos, RegistryKey<World> dimension, int entityCount, int tileEntityCount) {
        this.chunkPos = chunkPos;
        this.dimension = dimension;
        this.entityCount = entityCount;
        this.tileEntityCount = tileEntityCount;
    }

    public ChunkProfile(ChunkPos chunkPos, RegistryKey<World> dimension) {
        this(chunkPos, dimension, 0, 0);
    }

    public void appendEntity(Map<Long,Long> times) {
        entityCount++;
        for(Map.Entry<Long,Long> entry: times.entrySet()) {
            if(!timeToCompleteTick.containsKey(entry.getKey())) {
                timeToCompleteTick.putIfAbsent(entry.getKey(), entry.getValue());
            } else {
                timeToCompleteTick.put(entry.getKey(), timeToCompleteTick.get(entry.getKey()) + entry.getValue());
            }
        }
    }

    public void appendTileEntity(Map<Long,Long> times) {
        tileEntityCount++;
        for(Map.Entry<Long,Long> entry: times.entrySet()) {
            if(!timeToCompleteTick.containsKey(entry.getKey())) {
                timeToCompleteTick.putIfAbsent(entry.getKey(), entry.getValue());
            } else {
                timeToCompleteTick.put(entry.getKey(), timeToCompleteTick.get(entry.getKey()) + entry.getValue());
            }
        }
    }

    public double getAvgTime() {
        long sum = 0;
        for(Map.Entry<Long,Long> entry: timeToCompleteTick.entrySet()) {
            sum += entry.getValue();
        }
        return sum / (double) new ArrayList<>(timeToCompleteTick.keySet()).size();
    }

    public static BlockPos parseBlockPos(String blockPos) {
        String temp = blockPos.replace("(", "").replace(")","")
                .replace("x=","").replace("z=","");
        String[] split = temp.split(",");
        return new BlockPos(Integer.parseInt(split[0]), 64, Integer.parseInt(split[1]));
    }

    public enum Columns {
        CHUNK_POS("Chunk Pos"),
        BLOCK_POS("Block Pos"),
        REGION("Region"),
        DIMENSION("Dimension"),
        TICK_TIME("Tick Time(ms)"),
        ENTITY_COUNT("Entity Count"),
        TILE_ENTITY_COUNT("Tile Entity Count");

        public final String columnName;
        Columns(String columnName) {
            this.columnName = columnName;
        }
    }
}
