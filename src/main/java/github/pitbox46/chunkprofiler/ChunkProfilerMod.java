package github.pitbox46.chunkprofiler;

import github.pitbox46.chunkprofiler.commands.ModCommands;
import github.pitbox46.chunkprofiler.csv.CSVObject;
import github.pitbox46.chunkprofiler.profile.ChunkProfile;
import github.pitbox46.chunkprofiler.profile.CrashProfiler;
import github.pitbox46.chunkprofiler.profile.Timer;
import github.pitbox46.chunkprofiler.profile.Timing;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

@Mod("chunkprofiler")
public class ChunkProfilerMod {
    private static final Logger LOGGER = LogManager.getLogger();

    public static volatile CrashProfiler PROFILER = new CrashProfiler();
    private static final Thread PROFILER_THREAD = new Thread(PROFILER, "Profiler thread");

    public static File gameDir;
    public static File lastProfile;
    public static boolean endProfiling = false;

    public ChunkProfilerMod() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        if(!PROFILER_THREAD.isAlive()) {
            PROFILER_THREAD.start();
        } else {
            PROFILER.running = true;
        }
        gameDir = event.getServer().getDataDirectory();
    }

    @SubscribeEvent
    public void onServerClosed(FMLServerStoppedEvent event) {
        PROFILER.running = false;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onTickEnd(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.END && endProfiling) {
            Map<ChunkPos, ChunkProfile> chunkProfiles = new HashMap<>();
            for(Entry<Entity,Timing> entry: Timer.ENTITY_TIMER.getTimings().entrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getKey().getPosition());
                chunkProfiles.putIfAbsent(chunkPos, new ChunkProfile(chunkPos, entry.getKey().getEntityWorld().getDimensionKey()));
                chunkProfiles.get(chunkPos).appendEntity(entry.getValue().getTimes());
            }
            for(Entry<TileEntity,Timing> entry: Timer.TILE_ENTITY_TIMER.getTimings().entrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getKey().getPos());
                chunkProfiles.putIfAbsent(chunkPos, new ChunkProfile(chunkPos, entry.getKey().getWorld().getDimensionKey()));
                chunkProfiles.get(chunkPos).appendTileEntity(entry.getValue().getTimes());
            }
            Map<String,List<String>> table = new LinkedHashMap<>();
            for(ChunkProfile.Columns column: ChunkProfile.Columns.values()) {
                table.put(column.columnName, new ArrayList<>());
            }
            for(ChunkProfile profile: chunkProfiles.values()) {
                table.get(ChunkProfile.Columns.CHUNK_POS.columnName).add(profile.chunkPos.toString());
                table.get(ChunkProfile.Columns.BLOCK_POS.columnName).add("(x=" + profile.chunkPos.getXStart() + ",z=" + profile.chunkPos.getZStart() + ")");
                table.get(ChunkProfile.Columns.REGION.columnName).add("r." + profile.chunkPos.getRegionCoordX() + "." + profile.chunkPos.getRegionCoordZ());
                table.get(ChunkProfile.Columns.DIMENSION.columnName).add(profile.dimension.getLocation().toString());
                table.get(ChunkProfile.Columns.ENTITY_COUNT.columnName).add(Integer.toString(profile.entityCount));
                table.get(ChunkProfile.Columns.TILE_ENTITY_COUNT.columnName).add(Integer.toString(profile.tileEntityCount));
                table.get(ChunkProfile.Columns.TICK_TIME.columnName).add(Double.toString(profile.getAvgTime() / 1000000D));
            }
            lastProfile = new File(gameDir, "logs" + File.separatorChar + "chunkprofile-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".csv");
            CSVObject.write(lastProfile, new CSVObject(table));
            endProfiling = false;
            Timer.ENTITY_TIMER.reset();
            Timer.TILE_ENTITY_TIMER.reset();
        }
    }
}
