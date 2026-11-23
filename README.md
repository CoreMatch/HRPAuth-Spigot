<div align="center">
  <h1>HRPAuth-Spigot</h1>
  <p><strong>Claim a player identity without losing its Mojang history.</strong></p>
  <p><a href="./README.zh-CN.md">简体中文</a></p>
</div>

The canonical repository is [CoreMatch/HRPAuth-Velocity](https://github.com/CoreMatch/HRPAuth-Velocity): a Velocity proxy plugin that connects in-game identities to HRPAuth accounts.

It supports account claiming and binding, plus transfer of an existing Mojang UUID to the claimed account.

## Why HRPAuth-Spigot

- **Keep Minecraft identity intact.** Preserve a player's Mojang UUID when moving to an HA account.
- **Claim or bind in game.** Connect a proxy-registered identity to a platform account.
- **Use familiar aliases.** Reach player actions through the plugin's two registered command aliases.
- **Authorize as a service.** Let the plugin call HRPAuth with OAuth2 service credentials.

## Learn more

- [Canonical Velocity repository](https://github.com/CoreMatch/HRPAuth-Velocity) — maintained source
- [HA Wiki](https://wiki.mcnb.dev/) — ecosystem documentation
- [HA-Contract](https://github.com/CoreMatch/HA-Contract) — ecosystem API contracts

## Scope

Despite its folder name, this is not a Spigot/Bukkit plugin; its commands are player-only.

## License & provenance

[AGPL-3.0](LICENSE). Source: [CoreMatch/HRPAuth-Velocity](https://github.com/CoreMatch/HRPAuth-Velocity).
