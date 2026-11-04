# ADR-0001: Velocity Proxy Plugin Despite the Repository Name

- **Status**: accepted
- **Date**: 2026-11-04

## Context

The repository is named `HRPAuth-Spigot`, which suggests a Spigot/Bukkit
plugin. In reality the code targets the Velocity proxy. Anyone deploying or
extending this needs to know which plugin platform is actually in use.

## Decision

This is a **Velocity proxy plugin**, not a Spigot/Bukkit plugin. It registers
itself with Velocity, exposes the commands `/hrpauth` and `/ha`, and dispatches
subcommands `reg` (register/claim) and `forcebind`.

## Consequences

- The repository name is misleading; the README states explicitly that despite
  the name this is a Velocity plugin.
- There is no Bukkit/Spigot/Paper dependency anywhere; all imports are
  `com.velocitypowered.api.*`.
- Cost: tooling or documentation that assumes a Spigot plugin (e.g. placing the
  jar in a Spigot `plugins/` folder) will not work; it must be installed on the
  Velocity proxy.

## Evidence

- Velocity dependency, no Spigot dependency: `build.gradle.kts:13-14`
  (`compileOnly("com.velocitypowered:velocity-api:4.1.1-SNAPSHOT")`).
- Plugin metadata: `src/main/resources/velocity-plugin.json:5,7`
  ("HRPAuth Velocity Proxy Plugin", main class
  `com.hrp.auth.proxy.HrpAuthProxy`).
- Plugin annotation and Velocity imports:
  `src/main/java/com/hrp/auth/proxy/HrpAuthProxy.java:17`
  (`@Plugin(id = "hrpauth-proxy", name = "HRPAuth-Proxy")`).
- Command registration and dispatch:
  `src/main/java/com/hrp/auth/proxy/HrpAuthProxy.java:78-86` (register
  `/hrpauth` and `/ha`), `:56-65` (dispatch `reg`→RegisterCommand,
  `forcebind`→ForceBindCommand).
- README self-description: `README.md:3-6` ("Despite the repository name, this
  is a Velocity plugin … not a Spigot/Bukkit plugin"); introduced by `e8b8ea6`.
- Commits: `111dcf4` (initialize as Velocity plugin), `7710fd5` (reg command),
  `d43748d` (forcebind), `0394469` (command handling cleanup).
- Not verified: none material; a repo-wide `-S"spigot"`/`-S"bukkit"` history
  search only matched the README commit.
