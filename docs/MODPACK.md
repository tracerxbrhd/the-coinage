# Modpack and datapack guide

## Server configuration

The server config is written to `serverconfig/uapi/the-coinage/server.toml` in each world.

- `copperPerSilver` and `silverPerGold` define the integer denomination hierarchy.
- `capacityPerDenomination` limits each denomination stored in a purse.
- `automaticPurseConversion` is off by default. When off, payments require the requested physical denominations.
- `automaticCoinPickup` is on by default.
- `mobCoinDrops` is implemented but off by default.
- Villager and Wandering Trader additions can be disabled independently.

Changing ratios changes generated exchange offers and all normalized-value calculations. Existing physical counts are not rewritten.

## Trades

Add JSON files below `data/<namespace>/the_coinage/trades/`. A malformed definition is logged and skipped.

```json
{
  "format_version": 1,
  "target": "minecraft:farmer",
  "level": 2,
  "weight": 1,
  "cost": { "type": "currency", "denomination": "copper", "count": 24 },
  "result": { "type": "item", "item": "minecraft:bread", "count": 8 },
  "max_uses": 12,
  "xp": 5,
  "price_multiplier": 0.05
}
```

Targets are villager profession IDs or `wandering_generic` / `wandering_rare`. Both `cost` and `result` support `item` or `currency`; currency values may exceed normal stack limits.

## Mob drops

Add JSON files below `data/<namespace>/the_coinage/mob_drops/`. Define exactly one `entity` or `entity_tag`.

```json
{
  "format_version": 1,
  "entity_tag": "example:bandits",
  "chance": 0.01,
  "denomination": "copper",
  "min": 1,
  "max": 4
}
```

The feature must also be enabled in server config. Default definitions intentionally cover only plausible coin-bearing humanoids.

## Loot and archaeology

The Coinage uses additive NeoForge global loot modifiers; vanilla tables are not replaced. Packs may override the injection tables at:

- `data/the_coinage/loot_table/inject/structure_coins.json`
- `data/the_coinage/loot_table/inject/archaeology_coins.json`

Override individual global modifiers or replace the `data/neoforge/loot_modifiers/global_loot_modifiers.json` list when a pack needs complete control.

## Tags and compatibility

- `#the_coinage:coins` contains all physical coin items.
- `#the_coinage:humanoid_coin_bearers` and `#the_coinage:illager_coin_bearers` are default entity-type tags and can be extended.
- Curios and Accessories are optional. The active equipped purse is preferred over inventory purses.
- Create needs no code integration: there are no recipe overrides, banking blocks or unconditional Create class references.
