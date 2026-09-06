package com.hrp.auth.proxy.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plugin configuration loaded from config.yml in the plugin data directory.
 * On first startup the file is auto-generated with safe defaults; operators
 * must edit the credentials before the plugin can talk to HRPAuth.
 */
public class Config {

    private static final String CONFIG_FILE = "config.yml";

    private HrpAuthConfig hrpAuth = new HrpAuthConfig();
    private PresenceConfig presence = new PresenceConfig();
    private SiteConfig site = new SiteConfig();
    private LogConfig log = new LogConfig();

    public HrpAuthConfig getHrpAuth() { return hrpAuth; }
    public PresenceConfig getPresence() { return presence; }
    public SiteConfig getSite() { return site; }
    public LogConfig getLog() { return log; }

    // ─── Nested config sections ────────────────────────────────────────

    public static class HrpAuthConfig {
        private String url = "http://127.0.0.1:2778";
        private String clientId = "";
        private String clientSecret = "";
        private String serviceToken = "";
        private int timeoutSec = 10;
        private boolean verifyTls = true;

        public String getUrl() { return url; }
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getServiceToken() { return serviceToken; }
        public int getTimeoutSec() { return timeoutSec; }
        public boolean isVerifyTls() { return verifyTls; }
    }

    public static class PresenceConfig {
        private boolean enabled = true;
        private String name = "HRPAuth-Proxy";
        private int ttlSeconds = 0;
        private int securityLevel = 0;

        public boolean isEnabled() { return enabled; }
        public String getName() { return name; }
        public int getTtlSeconds() { return ttlSeconds; }
        public int getSecurityLevel() { return securityLevel; }
    }

    public static class SiteConfig {
        private String name = "HRPAuth-Proxy";
        private String version = "1.0.0";

        public String getName() { return name; }
        public String getVersion() { return version; }
    }

    public static class LogConfig {
        private String level = "info";
        private String format = "text";

        public String getLevel() { return level; }
        public String getFormat() { return format; }
    }

    // ─── Loading ───────────────────────────────────────────────────────

    /**
     * Load configuration from the given data directory.
     * If config.yml does not exist, a default one is written first.
     */
    @SuppressWarnings("unchecked")
    public static Config load(Path dataDirectory) {
        Path configPath = dataDirectory.resolve(CONFIG_FILE);

        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                Yaml yaml = new Yaml();
                Map<String, Object> raw = yaml.loadAs(in, Map.class);
                if (raw == null) {
                    return defaultConfig();
                }
                return fromMap(raw);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load config from " + configPath, e);
            }
        }

        // First startup: write default config, then return built-in defaults
        writeDefaultConfig(configPath);
        return defaultConfig();
    }

    @SuppressWarnings("unchecked")
    private static Config fromMap(Map<String, Object> map) {
        Config cfg = new Config();

        Map<String, Object> hrp = (Map<String, Object>) map.get("hrpauth");
        if (hrp != null) {
            cfg.hrpAuth = new HrpAuthConfig();
            cfg.hrpAuth.url = getString(hrp, "url", cfg.hrpAuth.url);
            cfg.hrpAuth.clientId = getString(hrp, "client-id", cfg.hrpAuth.clientId);
            cfg.hrpAuth.clientSecret = getString(hrp, "client-secret", cfg.hrpAuth.clientSecret);
            cfg.hrpAuth.serviceToken = getString(hrp, "service-token", cfg.hrpAuth.serviceToken);
            cfg.hrpAuth.timeoutSec = getInt(hrp, "timeout-sec", cfg.hrpAuth.timeoutSec);
            cfg.hrpAuth.verifyTls = getBool(hrp, "verify-tls", cfg.hrpAuth.verifyTls);
        }

        Map<String, Object> pres = (Map<String, Object>) map.get("presence");
        if (pres != null) {
            cfg.presence = new PresenceConfig();
            cfg.presence.enabled = getBool(pres, "enabled", cfg.presence.enabled);
            cfg.presence.name = getString(pres, "name", cfg.presence.name);
            cfg.presence.ttlSeconds = getInt(pres, "ttl-seconds", cfg.presence.ttlSeconds);
            cfg.presence.securityLevel = getInt(pres, "security-level", cfg.presence.securityLevel);
        }

        Map<String, Object> siteMap = (Map<String, Object>) map.get("site");
        if (siteMap != null) {
            cfg.site = new SiteConfig();
            cfg.site.name = getString(siteMap, "name", cfg.site.name);
            cfg.site.version = getString(siteMap, "version", cfg.site.version);
        }

        Map<String, Object> logMap = (Map<String, Object>) map.get("log");
        if (logMap != null) {
            cfg.log = new LogConfig();
            cfg.log.level = getString(logMap, "level", cfg.log.level);
            cfg.log.format = getString(logMap, "format", cfg.log.format);
        }

        return cfg;
    }

    private static Config defaultConfig() {
        return new Config();
    }

    // ─── YAML generation ───────────────────────────────────────────────

    private static void writeDefaultConfig(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, buildDefaultYaml(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write default config to " + path, e);
        }
    }

    /**
     * Builds the default YAML string with operator-friendly comments.
     * Uses LinkedHashMap to control field order.
     */
    private static String buildDefaultYaml() {
        StringBuilder sb = new StringBuilder();
        sb.append("# ─── HRPAuth-Proxy Configuration ────────────────────────\n");
        sb.append("# Auto-generated on first startup. Edit and restart the proxy.\n");
        sb.append("#\n");
        sb.append("# !! IMPORTANT: You MUST fill in hrpauth.client-id and\n");
        sb.append("#    hrpauth.client-secret before the plugin can talk to HRPAuth.\n");
        sb.append("#    These values come from your HRPAuth config.yaml:\n");
        sb.append("#      oauth2.super_client_id      -> client-id\n");
        sb.append("#      oauth2.super_client_secret   -> client-secret\n");
        sb.append("#\n");
        sb.append("#    Or create a dedicated OAuth2 client via:\n");
        sb.append("#      grant_type=client_credentials\n");
        sb.append("# ────────────────────────────────────────────────────────\n");
        sb.append("\n");

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);

        root().forEach((key, value) -> {
            sb.append(yaml.dump(Map.of(key, value)));
        });

        return sb.toString();
    }

    private static Map<String, Object> root() {
        Map<String, Object> root = new LinkedHashMap<>();

        // hrpauth
        Map<String, Object> hrpAuth = new LinkedHashMap<>();
        hrpAuth.put("url", "http://127.0.0.1:2778");
        hrpAuth.put("client-id", "");
        hrpAuth.put("client-secret", "");
        hrpAuth.put("service-token", "");
        hrpAuth.put("timeout-sec", 10);
        hrpAuth.put("verify-tls", true);
        root.put("hrpauth", hrpAuth);

        // presence
        Map<String, Object> presence = new LinkedHashMap<>();
        presence.put("enabled", true);
        presence.put("name", "HRPAuth-Proxy");
        presence.put("ttl-seconds", 0);
        presence.put("security-level", 0);
        root.put("presence", presence);

        // site
        Map<String, Object> site = new LinkedHashMap<>();
        site.put("name", "HRPAuth-Proxy");
        site.put("version", "1.0.0");
        root.put("site", site);

        // log
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("level", "info");
        log.put("format", "text");
        root.put("log", log);

        return root;
    }

    // ─── Map helpers ───────────────────────────────────────────────────

    private static String getString(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : def;
    }

    private static int getInt(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private static boolean getBool(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        if (v instanceof Boolean b) return b;
        return def;
    }
}
