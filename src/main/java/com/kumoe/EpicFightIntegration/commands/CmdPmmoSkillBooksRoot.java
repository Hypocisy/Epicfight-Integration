package com.kumoe.EpicFightIntegration.commands;

import com.kumoe.EpicFightIntegration.config.writers.PackGenerator;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CmdPmmoSkillBooksRoot {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("efimod")
                .then(Commands.literal("genSkillData")
                        .requires(ctx -> ctx.hasPermission(2))
                        .executes(ctx -> PackGenerator.generatePack(ctx.getSource().getServer())))
        );
    }
}