package github.pitbox46.chunkprofiler.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.chunkprofiler.profile.Timer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandStartProfile implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandStartProfile CMD = new CommandStartProfile();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("start")
                .requires(cs -> cs.hasPermissionLevel(2))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Timer.ENTITY_TIMER.enable();
        Timer.TILE_ENTITY_TIMER.enable();
        ITextComponent message = new StringTextComponent("Starting chunk profile");
        try {
            context.getSource().asPlayer().sendStatusMessage(message, false);
        } catch (CommandSyntaxException ignore) {}
        LOGGER.info(message.getUnformattedComponentText());
        return 0;
    }
}
