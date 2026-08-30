package io.github.tracerxbrhd.thecoinage.purse;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import org.junit.jupiter.api.Test;

class PurseContentsTest {
    @Test void storesCountsAboveVanillaStackLimit() {
        PurseContents contents = new PurseContents(1, 364, 128, 7);
        assertEquals(364, contents.get(Denomination.COPPER));
        assertEquals(new CurrencyBreakdown(364, 128, 7), contents.breakdown());
    }

    @Test void codecRoundTripsAndCarriesSchemaVersion() {
        PurseContents original = new PurseContents(1, 364, 128, 7);
        var encoded = PurseContents.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        assertEquals(1, ((JsonObject) encoded).get("format_version").getAsInt());
        assertEquals(original, PurseContents.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test void invalidDataIsDecodedButExplicitlyRejected() {
        JsonObject json = new JsonObject();
        json.addProperty("format_version", 99);
        json.addProperty("copper", -5);
        PurseContents decoded = PurseContents.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(-5, decoded.copper());
        assertEquals(PurseContents.PurseValidation.UNSUPPORTED_FORMAT, decoded.validate(1_000_000));
    }

    @Test void capacityAndNegativeChecksAreDeterministic() {
        assertEquals(PurseContents.PurseValidation.NEGATIVE_COUNT,
            new PurseContents(1, -1, 0, 0).validate(100));
        assertEquals(PurseContents.PurseValidation.ABOVE_CAPACITY,
            new PurseContents(1, 101, 0, 0).validate(100));
        assertEquals(PurseContents.PurseValidation.VALID,
            new PurseContents(1, 100, 100, 100).validate(100));
    }

    @Test void immutableCopySemanticsPreventSharedMutation() {
        PurseContents before = new PurseContents(1, 5, 6, 7);
        PurseContents after = before.with(Denomination.COPPER, 20);
        assertEquals(5, before.copper());
        assertEquals(20, after.copper());
    }
}
