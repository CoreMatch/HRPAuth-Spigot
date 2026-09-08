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
 * /ha forcebind &lt;email&gt; &lt;password&gt;
 *
 * Calls POST /admin/force-bind on the HRPAuth backend to transfer the
 * executing player's Mojang UUID from their proxy-registered account (cbh=0)
 * to a manually registered account identified by email + password, then
 * deletes the proxy-registered account.
 */
public class ForceBindCommand implements SimpleCommand {

    private final Config config;
    private final OAuthClient oauthClient;
    private final HttpClient httpClient;

    public ForceBindCommand(Config config, OAuthClient oauthClient) {
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
        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("This command can only be executed by a player."));
            return;
        }

        if (args.length < 2) {
            source.sendMessage(Component.text("Usage: /ha forcebind <email> <password>"));
            return;
        }

        String email = args[0];
        String password = args[1];
        String username = player.getUsername();

        if (config.getHrpAuth().getClientId().isEmpty() || config.getHrpAuth().getClientSecret().isEmpty()) {
            source.sendMessage(Component.text("OAuth2 credentials not configured. Contact an administrator."));
            return;
        }

        if (config.getHrpAuth().getManageToken().isEmpty()) {
            source.sendMessage(Component.text("Manage token not configured. Contact an administrator."));
            return;
        }

        source.sendMessage(Component.text("Force binding account..."));

        CompletableFuture.runAsync(() -> {
            try {
                String serviceToken = oauthClient.getServiceToken();
                String manageToken = config.getHrpAuth().getManageToken();
                String json = """
                        {"username":"%s","email":"%s","password":"%s","manage_token":"%s"}""".formatted(
                        escapeJson(username), escapeJson(email), escapeJson(password), escapeJson(manageToken));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.getHrpAuth().getUrl() + "/admin/force-bind"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + serviceToken)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(config.getHrpAuth().getTimeoutSec()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    player.sendMessage(Component.text("Force bind successful! Your Mojang UUID has been transferred to the target account."));
                } else {
                    String body = response.body();
                    String errorCode = extractError(body);
                    String errorMsg = extractMessage(body);
                    String friendly = switch (errorCode) {
                        case "user_not_found" ->
                            "No proxy-registered account found for your username.";
                        case "target_not_found" ->
                            "Target account not found. Please check your email and password.";
                        case "invalid_credentials" ->
                            "Invalid email or password for the target account.";
                        case "invalid_manage_token" ->
                            "Server configuration error. Contact an administrator.";
                        case "no_mojang_uuid" ->
                            "The proxy-registered account has no Mojang UUID to transfer.";
                        default -> "Force bind failed (" + response.statusCode() + "): " + errorMsg;
                    };
                    player.sendMessage(Component.text(friendly));
                }
            } catch (Exception e) {
                player.sendMessage(Component.text("Force bind request failed: " + e.getMessage()));
            }
        });
    }

    private static String extractError(String json) {
        int idx = json.indexOf("\"error\":\"");
        if (idx < 0) return "";
        int start = idx + 9;
        int end = json.indexOf("\"", start);
        if (end < 0) return json.substring(start);
        return json.substring(start, end);
    }

    private static String extractMessage(String json) {
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
