# ADR-0001: Velocity Proxy Plugin Despite the Repository Name

- **Status**: accepted

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

- Velocity dependency, no Spigot dependency: `build.gradle.kts:L13-L14 fp=7489a6a5 ~ compileOnly("com.velocitypowered:velocity-api:4.`
  (`compileOnly("com.velocitypowered:velocity-api:4.1.1-SNAPSHOT")`).
- Plugin metadata: `src/main/resources/velocity-plugin.json (field: description)`
  ("HRPAuth Velocity Proxy Plugin", main class
  `com.hrp.auth.proxy.HrpAuthProxy`).
- Plugin annotation and Velocity imports:
  `src/main/java/com/hrp/auth/proxy/HrpAuthProxy.java:L17-L17 fp=3ddec6c7 ~ @Plugin(id = "hrpauth-proxy", name = "HRPAuth-Pr`
  (`@Plugin(id = "hrpauth-proxy", name = "HRPAuth-Proxy")`).
- Command registration and dispatch:
  `src/main/java/com/hrp/auth/proxy/HrpAuthProxy.java:L78-L86 fp=eab0ae06 ~ CommandMeta hrpauthMeta = commandManager.metaBui` (register
  `/hrpauth` and `/ha`), `:56-65` (dispatch `reg`→RegisterCommand,
  `forcebind`→ForceBindCommand).
- README self-description: `README.md:L3-L6 fp=933311d3 ~ Velocity proxy plugin that connects an HRPAuth d` ("Despite the repository name, this
  is a Velocity plugin … not a Spigot/Bukkit plugin").
- Not verified: none material.
