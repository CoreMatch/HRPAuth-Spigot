# HRPAuth-Spigot

Velocity proxy plugin that connects an HRPAuth deployment to Minecraft players:
registers/claims game accounts against HRPAuth and exposes admin commands on the
proxy. Despite the repository name, this is a **Velocity** plugin
(`velocity-plugin.json`, plugin id `hrpauth-proxy`), not a Spigot/Bukkit plugin.

## Build & Run

Requires Java 25.

```bash
./build.sh   # runs ./gradlew build
```

Output: `build/libs/HRPAuth-Proxy-1.0-SNAPSHOT.jar`. Drop the jar into your
Velocity server's `plugins/` directory and restart the proxy.

## Dependencies & Configuration

- Upstream dependency: an HRPAuth core instance (OAuth2 + business API).
- On first startup the plugin generates
  `plugins/hrpauth-proxy/config.yml` with defaults. You must fill in
  `hrpauth.client-id` / `hrpauth.client-secret` (from HRPAuth's
  `oauth2.super_client_id` / `oauth2.super_client_secret`, or a dedicated
  `client_credentials` client) and `hrpauth.url`, then restart.
- Optional sections: `presence` (service registration on HRPAuth) and `site`
  (display name/version for the `/ha` command banner).

Commands: `/ha reg <email> <password>` (claim a proxy-registered account) and
`/ha forcebind <email> <password>` (transfer the Mojang UUID to the target
account).

## Documentation

- HRPAuth contracts and error codes: [HA-Contract](https://github.com/CoreMatch/HA-Contract)
  (see `docs/api/openapi/hrpauth-business.yaml`)
- Force-bind background (roadmap item 2.1): [HA-Contract — agents.md](https://github.com/CoreMatch/HA-Contract/blob/main/agents.md)
