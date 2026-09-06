package com.hrp.auth.proxy;

import com.hrp.auth.proxy.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * /ha reg &lt;email&gt; &lt;password&gt;
 *
 * Calls POST /admin/claim-user on the HRPAuth backend to claim the executing
 * player's proxy-registered account (cbh 0→1) with a real email + password.
 * The player's Mojang UUID is auto-detected from the proxy session.
 */
public class RegisterCommand implements SimpleCommand {

    private final Config config;
    private final OAuthClient oauthClient;
    private final HttpClient httpClient;

    public RegisterCommand(Config config, OAuthClient oauthClient) {
        this.config = config;
        this.oauthClient = oauthClient;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getHrpAuth().getTimeoutSec()))
                .build();
    }

    @Override
    public void execute(Invocation invocation) {
        execute(invocation.source(), invocation.arguments());
    }

    public void execute(CommandSource source, String[] args) {
        // Only players can execute this command
        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("This command can only be executed by a player."));
            return;
        }

        if (args.length < 2) {
            source.sendMessage(Component.text("Usage: /ha reg <email> <password>"));
            return;
        }

        String email = args[0];
        String password = args[1];
        String mojangUuid = player.getUniqueId().toString().replace("-", "");

        if (config.getHrpAuth().getClientId().isEmpty() || config.getHrpAuth().getClientSecret().isEmpty()) {
            source.sendMessage(Component.text("OAuth2 credentials not configured. Contact an administrator."));
            return;
        }

        source.sendMessage(Component.text("Claiming account..."));

        // Async HTTP request to avoid blocking the proxy
        CompletableFuture.runAsync(() -> {
            try {
                String serviceToken = oauthClient.getServiceToken();
                String json = """
                        {"mojang_uuid":"%s","email":"%s","password":"%s"}""".formatted(
                        escapeJson(mojangUuid), escapeJson(email), escapeJson(password));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.getHrpAuth().getUrl() + "/admin/claim-user"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + serviceToken)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(config.getHrpAuth().getTimeoutSec()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    player.sendMessage(Component.text("Account claimed successfully! You can now log in via WebUI."));
                } else {
                    String body = response.body();
                    String errorMsg = extractMessage(body);
                    player.sendMessage(Component.text("Claim failed (" + response.statusCode() + "): " + errorMsg));
                }
            } catch (Exception e) {
                player.sendMessage(Component.text("Claim request failed: " + e.getMessage()));
            }
        });
    }

    private static String extractMessage(String json) {
        // Simple extraction: look for "message":"..." pattern
        int idx = json.indexOf("\"message\":\"");
        if (idx < 0) return json;
        int start = idx + 11;
        int end = json.indexOf("\"", start);
        if (end < 0) return json.substring(start);
        return json.substring(start, end);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
