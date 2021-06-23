package github.pitbox46.chunkprofiler.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.chunkprofiler.ChunkProfilerMod;
import github.pitbox46.chunkprofiler.csv.CSVObject;
import github.pitbox46.chunkprofiler.profile.ChunkProfile;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.EnumArgument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class CommandViewProfile implements Command<CommandSource> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final CommandViewProfile CMD = new CommandViewProfile();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands
                .literal("view")
                .requires(cs -> cs.hasPermissionLevel(2))
                .then(Commands.argument("column", EnumArgument.enumArgument(ChunkProfile.Columns.class))
                    .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ChunkProfile.Columns columnType = context.getArgument("column", ChunkProfile.Columns.class);
        if(columnType == ChunkProfile.Columns.BLOCK_POS || columnType == ChunkProfile.Columns.CHUNK_POS || columnType == ChunkProfile.Columns.DIMENSION) {
            columnType = ChunkProfile.Columns.TICK_TIME;
        }
        ServerPlayerEntity player = null;
        try {
            player = context.getSource().asPlayer();
        } catch (CommandSyntaxException ignore) {}

        CSVObject csv;
        if(ChunkProfilerMod.lastProfile != null) {
            csv = CSVObject.read(ChunkProfilerMod.lastProfile);
        } else {
            ITextComponent message = new StringTextComponent("No profile detected!");
            if(player != null) {
                player.sendStatusMessage(message, false);
            }
            LOGGER.info(message);
            return 1;
        }
        Map<String, List<String>> table = csv.getTable();
        String columnName = columnType.columnName;
        //Prevents system from trying to view block pos, dimension, region, or chunk pos because they will already be viewed
        if(columnName.equals(ChunkProfile.Columns.BLOCK_POS.columnName)
                || columnName.equals(ChunkProfile.Columns.CHUNK_POS.columnName)
                || columnName.equals(ChunkProfile.Columns.REGION.columnName)
                || columnName.equals(ChunkProfile.Columns.DIMENSION.columnName)) {
            columnName = ChunkProfile.Columns.TICK_TIME.columnName;
        }

        Comparator<String> comparator = Comparator.comparingDouble(Double::parseDouble);

        for(ChunkProfile.Columns columns: ChunkProfile.Columns.values()) {
            if(columns == ChunkProfile.Columns.DIMENSION
                    || columns == ChunkProfile.Columns.BLOCK_POS
                    || columns == ChunkProfile.Columns.REGION
                    || columns.columnName.equals(columnName)) {
                continue;
            }
            table.remove(columns.columnName);
        }

        List<List<String>> unsortedList = CSVObject.byColumnToByRow(table);
        List<List<String>> sortedList;
        try {
            int columnPos = new ArrayList<>(table.keySet()).indexOf(columnName);
            sortedList = unsortedList
                    .stream()
                    .sorted((L0, L1) -> comparator.compare(L1.get(columnPos), L0.get(columnPos)))
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            LOGGER.debug(e.getMessage());
            e.printStackTrace();
            return 1;
        }

        IFormattableTextComponent delimiter = new StringTextComponent(" | ").mergeStyle(TextFormatting.WHITE);

        IFormattableTextComponent message = new StringTextComponent("");
        try {
            for (int i = 0; i < sortedList.size() && i < 10/* Display max of 10 entries */; i++) {
                BlockPos tpPos = ChunkProfile.parseBlockPos(sortedList.get(i).get(0));
                IFormattableTextComponent dimensionText =
                        new StringTextComponent(sortedList.get(i).get(2))
                                .mergeStyle(TextFormatting.GREEN)
                                .appendSibling(delimiter);

                double quantity = Double.parseDouble(sortedList.get(i).get(3));
                IFormattableTextComponent quantityText =
                        new StringTextComponent(columnName  + " " + Math.round(quantity * 10000) / 10000D)
                                .mergeStyle(TextFormatting.BLUE)
                                .appendSibling(delimiter);

                IFormattableTextComponent regionText =
                        new StringTextComponent(sortedList.get(i).get(1))
                                .mergeStyle(TextFormatting.GREEN)
                                .appendSibling(delimiter);

                IFormattableTextComponent teleportText =
                        new StringTextComponent("[teleport]")
                                .mergeStyle(TextFormatting.WHITE);
                //TP on click
                if(player != null && sortedList.get(i).get(2).equals(player.getEntityWorld().getDimensionKey().getLocation().toString())) {
                    teleportText.setStyle(teleportText.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s " + tpPos.getX() + " " + tpPos.getY() + " " + tpPos.getZ())));
                } else {
                    teleportText.mergeStyle(TextFormatting.STRIKETHROUGH);
                }

                message.appendSibling(dimensionText).appendSibling(quantityText).appendSibling(regionText).appendSibling(teleportText);
                if (i != sortedList.size() - 1) {
                    message.appendString("\n");
                }
            }

            if(player != null) {
                player.sendStatusMessage(message, false);
            }
            LOGGER.info(message.getUnformattedComponentText());
        } catch (Throwable e) {
            LOGGER.debug(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

}