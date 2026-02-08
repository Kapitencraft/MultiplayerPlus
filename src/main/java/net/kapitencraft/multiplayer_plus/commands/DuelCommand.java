package net.kapitencraft.multiplayer_plus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kapitencraft.multiplayer_plus.duel.Duel;
import net.kapitencraft.multiplayer_plus.duel.DuelHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DuelCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("duel")
                .then(Commands.literal("challenge")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(commandContext -> makeDuel(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player")))
                        )
                ).then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(commandContext -> inviteToDuel(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "player")))
                        )
                ).then(Commands.literal("end")
                        .executes(DuelCommand::closeDuel)
                )
        );
    }

    private static int makeDuel(CommandSourceStack stack, ServerPlayer player) {
        DuelHandler handler = DuelHandler.getInstance();
        ServerPlayer serverPlayer = stack.getPlayer();
        if (handler.hasDuel(serverPlayer)) {
            stack.sendFailure(Component.translatable("duel_command.fail.already_in_duel"));
        } else {
            if (handler.hasDuel(player)) {
                stack.sendFailure(Component.translatable("duel_command.fail.other_in_duel"));
            } else {
                Duel duel = handler.computeOrAdd(serverPlayer);
                duel.addToEnemyTeam(player, serverPlayer);
                stack.sendSuccess(() -> Component.translatable("duel_command.success.make"), true);
                return 2;
            }
        }
        return 0;
    }

    private static int inviteToDuel(CommandSourceStack stack, ServerPlayer player) {
        DuelHandler handler = DuelHandler.getInstance();
        ServerPlayer serverPlayer = stack.getPlayer();
        if (handler.hasDuel(player)) {
            stack.sendFailure(Component.translatable("duel_command.fail.other_in_duel"));
        } else {
            Duel duel = handler.computeOrAdd(serverPlayer);
            duel.addToTeam(player, serverPlayer);
            stack.sendSuccess(() -> Component.translatable("duel_command.success.join"), true);
            return 2;
        }
        return 0;
    }

    private static int closeDuel(CommandContext<CommandSourceStack> context) {
        DuelHandler handler = DuelHandler.getInstance();
        CommandSourceStack stack = context.getSource();
        ServerPlayer serverPlayer = stack.getPlayer();
        if (!handler.hasDuel(serverPlayer)) {
            stack.sendFailure(Component.translatable("duel_command.fail.no_duel"));
        } else {
            Duel duel = handler.getDuel(serverPlayer);
            if (duel.isOwner(serverPlayer)) {
                if (duel.close()) {
                    stack.sendSuccess(() -> Component.translatable("duel_command.success.close"), true);
                    return duel.getMemberCount();
                } else {
                    stack.sendFailure(Component.translatable("duel_command.fail.already_closed"));
                    return 0;
                }
            }
        }
        return 0;
    }
}
