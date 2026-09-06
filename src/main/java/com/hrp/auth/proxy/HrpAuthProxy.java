package com.hrp.auth.proxy;

import com.hrp.auth.proxy.config.Config;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import org.slf4j.Logger;

import jakarta.inject.Inject;
import java.nio.file.Path;

@Plugin(id = "hrpauth-proxy", name = "HRPAuth-Proxy", version = "1.0.0")
public class HrpAuthProxy {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Config config;

    @Inject
    public HrpAuthProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        // Load or auto-generate config.yml
        try {
            config = Config.load(dataDirectory);
            logger.info("Configuration loaded from config.yml");
        } catch (Exception e) {
            logger.error("Failed to load configuration, plugin will not start", e);
            return;
        }

        // Validate critical credentials
        if (config.getHrpAuth().getClientId().isEmpty()
                || config.getHrpAuth().getClientSecret().isEmpty()) {
            logger.warn("HRPAuth client-id / client-secret is empty! "
                    + "Please edit plugins/hrpauth-proxy/config.yml and restart.");
        }

        // Register commands
        CommandManager commandManager = server.getCommandManager();
        RegisterCommand registerCommand = new RegisterCommand(config);

        SimpleCommand hrpauthCmd = invocation -> {
            String[] args = invocation.arguments();
            if (args.length > 0 && "reg".equalsIgnoreCase(args[0])) {
                registerCommand.execute(invocation);
            } else {
                invocation.source().sendMessage(
                        net.kyori.adventure.text.Component.text(
                                "HA > " + config.getSite().getName() + " v" + config.getSite().getVersion() + "\n"
                                + "Usage:\n"
                                + "  /ha reg <email> <password> - Claim a proxy-registered account"
                        )
                );
            }
        };

        CommandMeta hrpauthMeta = commandManager.metaBuilder("hrpauth")
                .plugin(this)
                .build();
        commandManager.register(hrpauthMeta, hrpauthCmd);

        CommandMeta haMeta = commandManager.metaBuilder("ha")
                .plugin(this)
                .build();
        commandManager.register(haMeta, hrpauthCmd);

        logger.info("HRPAuth-Proxy has been loaded! (site={})", config.getSite().getName());
    }

    public Config getConfig() {
        return config;
    }
}
