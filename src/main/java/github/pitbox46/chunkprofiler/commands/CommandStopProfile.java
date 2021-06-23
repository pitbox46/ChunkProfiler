package github.pitbox46.chunkprofiler.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.chunkprofiler.ChunkProfilerMod;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandStopProfile implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandStopProfile CMD = new CommandStopProfile();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("stop")
                .requires(cs -> cs.hasPermissionLevel(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ChunkProfilerMod.endProfiling = true;
        ITextComponent message = new StringTextComponent("Stopping chunk profile");
        try {
            context.getSource().asPlayer().sendStatusMessage(message, false);
        } catch (CommandSyntaxException ignore) {}
        LOGGER.info(message.getUnformattedComponentText());
        return 0;
    }
}
