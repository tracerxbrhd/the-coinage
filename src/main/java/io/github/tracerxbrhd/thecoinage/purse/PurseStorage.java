package io.github.tracerxbrhd.thecoinage.purse;

import io.github.tracerxbrhd.thecoinage.api.currency.CurrencyBreakdown;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import io.github.tracerxbrhd.thecoinage.registry.CoinageDataComponents;
import net.minecraft.world.item.ItemStack;

/** Single mutation boundary for persistent purse contents. */
public final class PurseStorage {
    private PurseStorage() {}

    public static PurseContents read(ItemStack purse) {
        return purse.getOrDefault(CoinageDataComponents.PURSE_CONTENTS.get(), PurseContents.EMPTY);
    }

    public static DepositResult deposit(ItemStack purse, Denomination denomination, long requested, long capacity) {
        if (requested < 0 || capacity < 1) return new DepositResult(0, Math.max(0, requested), Status.INVALID_REQUEST);
        PurseContents current = read(purse);
        if (current.validate(capacity) != PurseContents.PurseValidation.VALID) {
            return new DepositResult(0, requested, Status.INVALID_DATA);
        }
        long existing = current.get(denomination);
        long accepted = Math.min(requested, capacity - existing);
        if (accepted > 0) purse.set(CoinageDataComponents.PURSE_CONTENTS.get(),
            current.with(denomination, Math.addExact(existing, accepted)));
        return new DepositResult(accepted, requested - accepted, accepted == requested ? Status.SUCCESS : Status.PARTIAL);
    }

    public static ExtractionResult extract(ItemStack purse, Denomination denomination, long requested, long capacity) {
        if (requested < 0 || capacity < 1) return new ExtractionResult(0, Status.INVALID_REQUEST);
        PurseContents current = read(purse);
        if (current.validate(capacity) != PurseContents.PurseValidation.VALID) {
            return new ExtractionResult(0, Status.INVALID_DATA);
        }
        long extracted = Math.min(requested, current.get(denomination));
        if (extracted > 0) purse.set(CoinageDataComponents.PURSE_CONTENTS.get(),
            current.with(denomination, current.get(denomination) - extracted));
        return new ExtractionResult(extracted, extracted == requested ? Status.SUCCESS : Status.PARTIAL);
    }

    public static boolean replace(ItemStack purse, CurrencyBreakdown balance, long capacity) {
        if (balance.copper() > capacity || balance.silver() > capacity || balance.gold() > capacity) return false;
        PurseContents current = read(purse);
        if (current.validate(capacity) != PurseContents.PurseValidation.VALID) return false;
        purse.set(CoinageDataComponents.PURSE_CONTENTS.get(), new PurseContents(PurseContents.CURRENT_FORMAT,
            balance.copper(), balance.silver(), balance.gold()));
        return true;
    }

    public enum Status { SUCCESS, PARTIAL, INVALID_DATA, INVALID_REQUEST }
    public record DepositResult(long accepted, long remainder, Status status) {}
    public record ExtractionResult(long extracted, Status status) {}
}
