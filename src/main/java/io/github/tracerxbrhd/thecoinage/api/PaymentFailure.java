package io.github.tracerxbrhd.thecoinage.api;

public enum PaymentFailure {
    NONE,
    INVALID_REQUEST,
    INVALID_PURSE_DATA,
    INSUFFICIENT_FUNDS,
    MISSING_DENOMINATION,
    OVERFLOW,
    STATE_CHANGED
}
