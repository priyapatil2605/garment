package com.dragonfruit.core.api;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @Value("${server.port:8080}") private int port;
    @Value("${app.server-id:1}") private int serverId;

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("application", "DragonFruit Distributed Store",
            "status", "UP", "serverId", serverId, "port", port,
            "timestamp", Instant.now().toString());
    }
}
