package io.github.tracerxbrhd.thecoinage.api;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;

/** Explicit result for both simulated and committed server-side payments. */
public record PaymentResult(boolean successful, boolean simulated, PaymentFailure failure,
                            CurrencyBreakdown requested, CurrencyBreakdown withdrawn) {
    public static PaymentResult success(boolean simulated, CurrencyBreakdown requested) {
        return new PaymentResult(true, simulated, PaymentFailure.NONE, requested, requested);
    }

    public static PaymentResult failure(boolean simulated, PaymentFailure failure, CurrencyBreakdown requested) {
        return new PaymentResult(false, simulated, failure, requested, CurrencyBreakdown.ZERO);
    }
}
