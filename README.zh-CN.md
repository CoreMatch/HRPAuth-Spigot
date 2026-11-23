<div align="center">
  <h1>HRPAuth-Spigot</h1>
  <p><strong>认领玩家身份，不丢 Mojang 历史。</strong></p>
  <p><a href="./README.md">English</a></p>
</div>

本仓库的规范来源是 [CoreMatch/HRPAuth-Velocity](https://github.com/CoreMatch/HRPAuth-Velocity)：这是一个将游戏内身份连接到 HRPAuth 账号的 Velocity 代理插件。

它支持账号认领与绑定，并可将已有 Mojang UUID 转移到所认领的账号。

## 为什么选择 HRPAuth-Spigot

- **保留 Minecraft 身份。** 玩家迁移至 HA 账号时，保留其 Mojang UUID。
- **在游戏内认领或绑定。** 将代理端注册的身份连接到平台账号。
- **使用熟悉命令别名。** 通过插件注册的两个命令别名执行玩家操作。
- **以服务身份授权。** 插件使用 OAuth2 服务凭据调用 HRPAuth。

## 进一步了解

- [规范 Velocity 仓库](https://github.com/CoreMatch/HRPAuth-Velocity) — 维护中的源码
- [HA Wiki](https://wiki.mcnb.dev/) — 生态文档
- [HA-Contract](https://github.com/CoreMatch/HA-Contract) — 生态 API 契约

## 范围

尽管目录名为 Spigot，它并非 Spigot/Bukkit 插件；其命令仅供玩家使用。

## 许可证与来源

[AGPL-3.0](LICENSE)。来源：[CoreMatch/HRPAuth-Velocity](https://github.com/CoreMatch/HRPAuth-Velocity)。
