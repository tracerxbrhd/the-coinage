package io.github.tracerxbrhd.thecoinage.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.tracerxbrhd.thecoinage.api.CoinageApi;
import io.github.tracerxbrhd.thecoinage.api.PaymentResult;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.purse.ActivePurseResolver;
import io.github.tracerxbrhd.thecoinage.purse.PurseStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CoinageCommands {
    private CoinageCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("coinage")
            .then(Commands.literal("balance")
                .executes(context -> balance(context.getSource(), context.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player()).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(context -> balance(context.getSource(), EntityArgument.getPlayer(context, "player")))))
            .then(Commands.literal("purse").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> purse(context.getSource(), EntityArgument.getPlayer(context, "player")))))
            .then(Commands.literal("give").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .then(Commands.argument("denomination", StringArgumentType.word())
                            .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                java.util.Arrays.stream(Denomination.values()).map(Denomination::serializedName), builder))
                            .executes(context -> give(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                LongArgumentType.getLong(context, "amount"),
                                StringArgumentType.getString(context, "denomination")))))))
            .then(Commands.literal("take").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .then(Commands.argument("denomination", StringArgumentType.word())
                            .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                java.util.Arrays.stream(Denomination.values()).map(Denomination::serializedName), builder))
                            .executes(context -> take(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                LongArgumentType.getLong(context, "amount"),
                                StringArgumentType.getString(context, "denomination")))))))
            .then(Commands.literal("reload").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(context -> {
                var source = context.getSource();
                ReloadCommand.reloadPacks(source.getServer().getPackRepository().getSelectedIds(), source);
                return 1;
            })));
    }

    private static int balance(CommandSourceStack source, ServerPlayer player) {
        CurrencyBreakdown value = CoinageApi.find().orElseThrow().getAvailableCurrency(player);
        source.sendSuccess(() -> Component.translatable("command.the_coinage.balance", player.getDisplayName(),
            value.gold(), value.silver(), value.copper()), false);
        return 1;
    }

    private static int purse(CommandSourceStack source, ServerPlayer player) {
        var handle = ActivePurseResolver.find(player);
        if (handle.isEmpty()) {
            source.sendFailure(Component.translatable("command.the_coinage.purse.missing", player.getDisplayName()));
            return 0;
        }
        CurrencyBreakdown value = PurseStorage.read(handle.get().get()).breakdown();
        source.sendSuccess(() -> Component.translatable("command.the_coinage.purse", player.getDisplayName(),
            value.gold(), value.silver(), value.copper()), false);
        return 1;
    }

    private static int give(CommandSourceStack source, ServerPlayer player, long count, String name) {
        Denomination denomination = denomination(source, name);
        if (denomination == null) return 0;
        CoinageApi.find().orElseThrow().give(player, CurrencyBreakdown.ZERO.with(denomination, count));
        source.sendSuccess(() -> Component.translatable("command.the_coinage.give", player.getDisplayName(), count,
            Component.translatable(denomination.translationKey())), true);
        return 1;
    }

    private static int take(CommandSourceStack source, ServerPlayer player, long count, String name) {
        Denomination denomination = denomination(source, name);
        if (denomination == null) return 0;
        PaymentResult result = CoinageApi.find().orElseThrow().pay(player,
            CurrencyBreakdown.ZERO.with(denomination, count));
        if (!result.successful()) {
            source.sendFailure(Component.translatable("command.the_coinage.take.failed", player.getDisplayName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.the_coinage.take", player.getDisplayName(), count,
            Component.translatable(denomination.translationKey())), true);
        return 1;
    }

    private static Denomination denomination(CommandSourceStack source, String name) {
        Denomination value = Denomination.byName(name).orElse(null);
        if (value == null) source.sendFailure(Component.translatable("command.the_coinage.denomination.invalid", name));
        return value;
    }
}
