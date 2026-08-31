package io.github.tracerxbrhd.thecoinage.api.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CurrencyExchangeTableTest {
    @Test void configuredRatiosDriveEveryExchangeDirection() {
        var exchanges = CurrencyExchangeTable.forRules(new CurrencyRules(7, 11));
        assertEquals(4, exchanges.size());
        assertEquals(new CurrencyAmount(Denomination.COPPER, 7), exchanges.get(0).cost());
        assertEquals(new CurrencyAmount(Denomination.SILVER, 1), exchanges.get(0).reward());
        assertEquals(new CurrencyAmount(Denomination.COPPER, 7), exchanges.get(1).reward());
        assertEquals(new CurrencyAmount(Denomination.SILVER, 11), exchanges.get(2).cost());
        assertEquals(new CurrencyAmount(Denomination.GOLD, 1), exchanges.get(2).reward());
        assertEquals(new CurrencyAmount(Denomination.SILVER, 11), exchanges.get(3).reward());
        assertFalse(exchanges.get(0).rare());
        assertTrue(exchanges.get(3).rare());
    }
}
