package github.pitbox46.chunkprofiler.mixin;

import github.pitbox46.chunkprofiler.profile.CrashProfiler;
import github.pitbox46.chunkprofiler.profile.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.function.Consumer;

import static github.pitbox46.chunkprofiler.ChunkProfilerMod.PROFILER;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract long getGameTime();

    @Inject(at = @At(value = "INVOKE", target = "net/minecraftforge/server/timings/TimeTracker.trackStart(Ljava/lang/Object;)V"), method = "tickBlockEntities", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onTickBlockEntityStart(CallbackInfo ci, IProfiler iprofiler, Iterator iterator, TileEntity tileentity, BlockPos blockpos) {
        Timer.TILE_ENTITY_TIMER.startTime(tileentity, this.getGameTime());
        PROFILER.start(tileentity);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraftforge/server/timings/TimeTracker.trackEnd(Ljava/lang/Object;)V"), method = "tickBlockEntities", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onTickBlockEntityEnd(CallbackInfo ci, IProfiler iprofiler, Iterator iterator, TileEntity tileentity, BlockPos blockpos) {
        Timer.TILE_ENTITY_TIMER.endTime(tileentity);
        PROFILER.stop(tileentity);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraftforge/server/timings/TimeTracker.trackStart(Ljava/lang/Object;)V"), method = "guardEntityTick")
    public void onTickEntityStart(Consumer<Entity> consumerEntity, Entity entityIn, CallbackInfo ci) {
        Timer.ENTITY_TIMER.startTime(entityIn, this.getGameTime());
        PROFILER.start(entityIn);
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraftforge/server/timings/TimeTracker.trackEnd(Ljava/lang/Object;)V"), method = "guardEntityTick")
    public void onTickEntityEnd(Consumer<Entity> consumerEntity, Entity entityIn, CallbackInfo ci) {
        Timer.ENTITY_TIMER.endTime(entityIn);
        PROFILER.stop(entityIn);
    }
}
