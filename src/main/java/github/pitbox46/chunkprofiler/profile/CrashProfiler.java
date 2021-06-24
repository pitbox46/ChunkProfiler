package github.pitbox46.chunkprofiler.profile;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static github.pitbox46.chunkprofiler.ChunkProfilerMod.gameDir;

public class CrashProfiler implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger();

    public volatile Object currentObject;
    public volatile boolean running = true;
    private volatile boolean active = false;
    private volatile long start;

    public synchronized void start(Object object) {
        this.currentObject = object;
        this.active = true;
        start = System.nanoTime();
    }

    public synchronized void stop() {
        if(active) {
            this.active = false;
            currentObject = null;
        }
    }

    @Override
    public void run() {
        while(true) {
            synchronized (this) {
                while(!running) {}
                try {
                    wait(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (active && System.nanoTime() - start > 10 * 1000000000L) {
                    LOGGER.error("{} took too long to tick", currentObject.toString());
                    File crashFile = new File(gameDir, "crash-reports" + File.separatorChar + "tick-timeout-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder
                            .append("Crash due to tick timeout")
                            .append("\n\n")
                            .append("Details:") 
                            .append("\n");
                    if(currentObject instanceof TileEntity) {
                        TileEntity object = (TileEntity) currentObject;
                        ChunkPos chunkPos = new ChunkPos(object.getPos());
                        stringBuilder
                                .append("Position: ").append(object.getPos())
                                .append("\n")
                                .append("Chunk: ").append(chunkPos)
                                .append("\n")
                                .append("Region File: ").append("r.").append(chunkPos.getRegionCoordX()).append(".").append(chunkPos.getRegionCoordZ())
                                .append("\n")
                                .append("Type: ").append(object.getType());
                    }
                    else if(currentObject instanceof Entity) {
                        Entity object = (Entity) currentObject;
                        ChunkPos chunkPos = new ChunkPos(object.getPosition());
                        stringBuilder
                                .append("Position: ").append(object.getPositionVec())
                                .append("\n")
                                .append("Chunk: ").append(chunkPos)
                                .append("\n")
                                .append("Region File: ").append("r.").append(chunkPos.getRegionCoordX()).append(".").append(chunkPos.getRegionCoordZ())
                                .append("\n")
                                .append("Type: ").append(object.getType())
                                .append("\n")
                                .append("UUID: ").append(object.getUniqueID());
                    }
                    try (FileWriter writer = new FileWriter(crashFile)) {
                        writer.write(stringBuilder.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.stop();
                }
            }
        }
    }
}
