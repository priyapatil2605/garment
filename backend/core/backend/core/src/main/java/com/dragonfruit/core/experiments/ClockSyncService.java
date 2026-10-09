package com.dragonfruit.core.experiments;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ClockSyncService {
    public Map<String, Object> estimate(long clientSendEpochMillis,
                                        long clientReceiveEpochMillis,
                                        long serverEpochMillis) {
        if (clientReceiveEpochMillis < clientSendEpochMillis) {
            throw new IllegalArgumentException("Receive time must be >= send time");
        }
        long rtt = clientReceiveEpochMillis - clientSendEpochMillis;
        long estimatedServerAtReceive = serverEpochMillis + (rtt / 2);
        return Map.of(
            "algorithm", "Cristian's algorithm (single-sample demonstration)",
            "clientSendEpochMillis", clientSendEpochMillis,
            "clientReceiveEpochMillis", clientReceiveEpochMillis,
            "serverEpochMillis", serverEpochMillis,
            "roundTripTimeMillis", rtt,
            "estimatedServerTimeEpochMillis", estimatedServerAtReceive,
            "estimatedServerTime", Instant.ofEpochMilli(estimatedServerAtReceive).toString()
        );
    }
}
