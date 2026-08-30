# The Coinage API

The public API lives under `io.github.tracerxbrhd.thecoinage.api`. Treat every other package as internal. Calls that inspect or mutate player funds must run on the logical server thread.

## Dependency

Depend on The Coinage and its required U-API version. At runtime, NeoForge metadata must declare both dependencies. Do not bundle either mod into your JAR.

## Entry point

```java
CoinageApi.find().ifPresent(api -> {
    CurrencyBreakdown balance = api.getAvailableCurrency(player);
});
```

`CoinageApi` is also registered as a global U-API service. The optional result lets integrations remain safely dormant when The Coinage is absent.

## Currency

`Denomination` provides stable `copper`, `silver` and `gold` identifiers. `CurrencyAmount` represents one denomination; `CurrencyBreakdown` is an immutable three-denomination value. Use `CurrencyRules` and `CurrencyMath` for conversion, normalization and safe value calculation instead of hardcoding ratios.

```java
CurrencyBreakdown reward = CurrencyBreakdown.ZERO.with(Denomination.SILVER, 3);
api.give(player, reward);
```

`give` owns the complete purse → inventory → safe world-drop fallback. Integrations should not spawn physical coin stacks unless world placement is intentional.

## Funds and payments

```java
CurrencyBreakdown price = CurrencyBreakdown.ZERO.with(Denomination.COPPER, 175);
if (api.canAfford(player, price)) {
    PaymentResult result = api.pay(player, price);
    if (!result.successful()) {
        // State changed or another explicit failure occurred before commit.
    }
}
```

- `getAvailableCurrency` combines loose inventory coins with the active purse.
- `simulatePayment` performs the same planning without mutation.
- `pay` validates and commits one server-side transaction.
- `findActivePurse` returns a copy, never mutable internal state.

`PaymentResult.failure()` distinguishes invalid requests, missing denominations, insufficient value, overflow and state changes. Exact denominations are required unless the server enables automatic purse conversion.

## Events

`CoinageEvents.CurrencyReceived`, `CurrencySpent` and `PurseChanged` are posted on `NeoForge.EVENT_BUS`. They are server-side, observational, non-cancellable and fire after successful authoritative mutation. Avoid writing handlers that recursively grant or spend currency.

## U-API rewards

The provider ID `the_coinage:currency` accepts reward data such as:

```json
{
  "denomination": "silver",
  "min": 1,
  "max": 3
}
```

It uses the same authoritative reward service as the API and commands.

## Stability

The public `api` package and purse data format are compatibility surfaces. Breaking API or persistent-format changes require a major release and a migration path.
