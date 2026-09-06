package com.hrp.auth.proxy;

import com.hrp.auth.proxy.config.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * Obtains and caches an OAuth2 Service Token via client_credentials grant.
 */
public class OAuthClient {

    private final Config.HrpAuthConfig hrpAuth;
    private final HttpClient httpClient;

    private String cachedToken;
    private Instant expiresAt = Instant.MIN;

    public OAuthClient(Config.HrpAuthConfig hrpAuth) {
        this.hrpAuth = hrpAuth;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(hrpAuth.getTimeoutSec()))
                .build();
    }

    /**
     * Returns a valid service token, fetching a new one if needed.
     */
    public synchronized String getServiceToken() throws Exception {
        if (cachedToken != null && Instant.now().plusSeconds(30).isBefore(expiresAt)) {
            return cachedToken;
        }
        return fetchToken();
    }

    private String fetchToken() throws Exception {
        String body = "grant_type=client_credentials"
                + "&client_id=" + urlEncode(hrpAuth.getClientId())
                + "&client_secret=" + urlEncode(hrpAuth.getClientSecret());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hrpAuth.getUrl() + "/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(hrpAuth.getTimeoutSec()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to obtain service token (" + response.statusCode() + "): " + response.body());
        }

        String json = response.body();
        cachedToken = extractJsonString(json, "access_token");
        long expiresIn = extractJsonLong(json, "expires_in", 3600);
        expiresAt = Instant.now().plusSeconds(expiresIn);

        return cachedToken;
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) throw new RuntimeException("Missing key: " + key);
        int colon = json.indexOf(':', idx + pattern.length());
        int start = json.indexOf('"', colon + 1);
        int end = json.indexOf('"', start + 1);
        return json.substring(start + 1, end);
    }

    private static long extractJsonLong(String json, String key, long def) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return def;
        int colon = json.indexOf(':', idx + pattern.length());
        int end = colon + 1;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        try {
            return Long.parseLong(json.substring(colon + 1, end));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
