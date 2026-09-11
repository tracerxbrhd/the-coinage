![The Coinage banner](img/the-coinage-banner.png)

# The Coinage

<p align="center">
  <a href="https://github.com/tracerxbrhd/the-coinage/releases"><img alt="Release" src="https://img.shields.io/github/v/release/tracerxbrhd/the-coinage?include_prereleases&sort=semver&label=release"></a>
  <a href="https://github.com/tracerxbrhd/the-coinage/actions/workflows/build.yml"><img alt="Build" src="https://github.com/tracerxbrhd/the-coinage/actions/workflows/build.yml/badge.svg?branch=master"></a>
  <a href="https://modrinth.com/mod/the-coinage"><img alt="Modrinth" src="https://img.shields.io/badge/Modrinth-Download-00AF5C?logo=modrinth&logoColor=white"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/the-coinage"><img alt="CurseForge" src="https://img.shields.io/badge/CurseForge-Download-F16436?logo=curseforge&logoColor=white"></a>
</p>

**The Coinage adds an ancient physical currency to Minecraft: Copper, Silver and Gold Coins, a specialized Coin Purse, new trading opportunities, world loot, archaeology rewards and a currency system designed for modded gameplay.**

Coins remain real items rather than an invisible account balance. The mod is intended to feel like a natural part of Minecraft while also exposing a stable currency foundation for modpacks, servers and other mods.

## Compatibility

| Minecraft | The Coinage | U-API | Java | Loader |
| --- | --- | --- | --- | --- |
| 1.21.1 | 1.0.x | 2.x | 21 | NeoForge |
| 26.2 | 1.0.x | 3.x | 25 | NeoForge |

The project supports both Minecraft release lines through compatible builds. The default `master` branch currently contains the 1.21.1 source line.

## Features

- physical **Copper, Silver and Gold Coins** that complement rather than replace Emeralds;
- a server-authoritative **Coin Purse** with automatic pickup and dedicated storage;
- villager and Wandering Trader offers, including ratio-aware coin exchange;
- additive structure loot and archaeology rewards;
- optional, disabled-by-default mob drops;
- datapack-defined trades and mob drops;
- a public integration API for rewards, affordability checks and atomic payments;
- English and Russian localization.

Curios and Accessories support is optional. The Coin Purse can integrate with supported accessory slots without making those mods required dependencies.

## For modpacks and developers

The Coinage is designed to work both as a standalone gameplay mod and as a currency layer for larger packs. Most content customization remains data-driven, while the public API allows other mods to query funds, give rewards and process payments safely.

- [API documentation](docs/API.md)
- [Modpack guide](docs/MODPACK.md)

## Installation

Install NeoForge and a compatible U-API build for the same Minecraft version, then place The Coinage JAR in the `mods` directory on both client and server.

## Building from source

The default branch requires Java 21 and U-API 2.x.

```bash
./gradlew build
```

On Windows:

```powershell
gradlew.bat build
```

## License

The Coinage source code is licensed under the [Mozilla Public License 2.0](LICENSE) (`MPL-2.0`). The Underworld Studio name, logos and branding are not licensed by the MPL.
