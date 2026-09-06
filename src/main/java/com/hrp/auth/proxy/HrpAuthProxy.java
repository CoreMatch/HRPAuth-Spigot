package com.hrp.auth.proxy;

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

    @Inject
    public HrpAuthProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        CommandManager commandManager = server.getCommandManager();

        CommandMeta hrpauthMeta = commandManager.metaBuilder("hrpauth")
                .plugin(this)
                .build();
        commandManager.register(hrpauthMeta, new SimpleCommand() {
            @Override
            public void execute(Invocation invocation) {
                invocation.source().sendMessage(
                        net.kyori.adventure.text.Component.text("Hello World")
                );
            }
        });

        CommandMeta haMeta = commandManager.metaBuilder("ha")
                .plugin(this)
                .build();
        commandManager.register(haMeta, new SimpleCommand() {
            @Override
            public void execute(Invocation invocation) {
                invocation.source().sendMessage(
                        net.kyori.adventure.text.Component.text("Hello World")
                );
            }
        });

        logger.info("HRPAuth-Proxy has been loaded!");
    }
}
