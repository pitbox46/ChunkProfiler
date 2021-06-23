package github.pitbox46.chunkprofiler.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
                Commands.literal("chunkprofiler")
                        .then(CommandStartProfile.register(dispatcher))
                        .then(CommandStopProfile.register(dispatcher))
                        .then(CommandViewProfile.register(dispatcher))
        );

        dispatcher.register(Commands.literal("chunkprofiler").redirect(cmdTut));
    }
}
