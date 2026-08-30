package io.github.tracerxbrhd.thecoinage.api;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Server-side observational hooks fired after authoritative mutation. */
public final class CoinageEvents {
    private CoinageEvents() {}

    public static final class CurrencyReceived extends Event {
        private final ServerPlayer player;
        private final CurrencyBreakdown amount;
        public CurrencyReceived(ServerPlayer player, CurrencyBreakdown amount) {
            this.player = player;
            this.amount = amount;
        }
        public ServerPlayer player() { return player; }
        public CurrencyBreakdown amount() { return amount; }
    }

    public static final class CurrencySpent extends Event {
        private final ServerPlayer player;
        private final CurrencyBreakdown amount;
        public CurrencySpent(ServerPlayer player, CurrencyBreakdown amount) {
            this.player = player;
            this.amount = amount;
        }
        public ServerPlayer player() { return player; }
        public CurrencyBreakdown amount() { return amount; }
    }

    public static final class PurseChanged extends Event {
        private final ServerPlayer player;
        private final CurrencyBreakdown before;
        private final CurrencyBreakdown after;
        public PurseChanged(ServerPlayer player, CurrencyBreakdown before, CurrencyBreakdown after) {
            this.player = player;
            this.before = before;
            this.after = after;
        }
        public ServerPlayer player() { return player; }
        public CurrencyBreakdown before() { return before; }
        public CurrencyBreakdown after() { return after; }
    }
}
