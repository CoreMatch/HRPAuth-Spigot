<div align="center">
  <h1>Armorial Velocity Proxy</h1>
  <p><strong>Claim a player identity without losing its Mojang history.</strong></p>
  <p><a href="./README.zh-CN.md">简体中文</a></p>
</div>

The canonical repository is [CoreMatch/HRPAuth-Velocity](https://github.com/CoreMatch/HRPAuth-Velocity): an Armorial Velocity proxy plugin that connects in-game identities to Armorial accounts.

It supports account claiming and binding, plus transfer of an existing Mojang UUID to the claimed account.

## Why Armorial Velocity Proxy

- **Keep Minecraft identity intact.** Preserve a player's Mojang UUID when moving to an Armorial account.
- **Claim or bind in game.** Connect a proxy-registered identity to an Armorial platform account.
- **Use familiar aliases.** Reach player actions through `/armorial`, `/hrpauth`, or `/ha`.
- **Authorize as a service.** Let the plugin call Armorial with OAuth2 service credentials.

## Learn more

- [Canonical Velocity repository](https://github.com/CoreMatch/HRPAuth-Velocity) — maintained source
- [Armorial Wiki](https://wiki.mcnb.dev/) — ecosystem documentation
- [Armorial API Contracts](https://github.com/CoreMatch/HA-Contract) — ecosystem API contracts

## Scope

Despite its folder name, this is not a Spigot/Bukkit plugin; its commands are player-only.

## License & provenance

[AGPL-3.0](LICENSE). Source: [CoreMatch/HRPAuth-Velocity](https://github.com/CoreMatch/HRPAuth-Velocity).
