package com.dragonfruit.core.api;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servers")
public class ServerController {
    private final JdbcTemplate jdbc;
    public ServerController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public List<Map<String, Object>> list() {
        return jdbc.queryForList("SELECT server_id AS serverId, server_name AS serverName, host, http_port AS httpPort, status, role, last_heartbeat AS lastHeartbeat FROM server_nodes ORDER BY server_id");
    }

    @PostMapping("/{id}/heartbeat")
    public Map<String, Object> heartbeat(@PathVariable int id) {
        int changed = jdbc.update("UPDATE server_nodes SET status='UP', last_heartbeat=CURRENT_TIMESTAMP WHERE server_id=?", id);
        if (changed == 0) throw new IllegalArgumentException("Unknown server ID");
        return jdbc.queryForMap("SELECT server_id AS serverId, status, last_heartbeat AS lastHeartbeat FROM server_nodes WHERE server_id=?", id);
    }
}
