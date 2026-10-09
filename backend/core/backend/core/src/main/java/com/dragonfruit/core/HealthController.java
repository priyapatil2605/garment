
package com.dragonfruit.core;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "application", "DragonFruit Distributed Store",
                "status", "UP",
                "serverPort", serverPort,
                "timestamp", Instant.now().toString());
    }
}
