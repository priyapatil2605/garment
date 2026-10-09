package com.dragonfruit.core.experiments;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {
    private final JdbcTemplate jdbc;
    @Value("${app.server-id:1}") private int serverId;
    private final ExecutorService pool = Executors.newFixedThreadPool(8);
    public ExperimentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public List<Map<String,Object>> list() {
        return List.of(
            Map.of("id",1,"name","RPC and RMI","status","REST-based request-response demo"),
            Map.of("id",2,"name","Multithreading","status","Implemented demo"),
            Map.of("id",3,"name","Clock Synchronization","status","Implemented demo"),
            Map.of("id",4,"name","Election Algorithm","status","Coordinator election simulation"),
            Map.of("id",5,"name","Primary-backup Fault Tolerance","status","Failover simulation"),
            Map.of("id",6,"name","Data Consistency and Replication","status","Policy simulation"),
            Map.of("id",7,"name","Load Balancing Algorithm","status","Implemented demo"),
            Map.of("id",8,"name","MapReduce Job","status","Implemented demo"),
            Map.of("id",9,"name","MPI Collective Communication","status","Collective communication simulation")
        );
    }

    @PostMapping("/1/rpc-demo")
    public Map<String,Object> rpc(@RequestParam(defaultValue="hello") String message) {
        return Map.of("experiment",1,"request",message,"response","Server "+serverId+" received: "+message,
            "note","REST demonstration, not Java RMI wire-protocol implementation.");
    }

    @PostMapping("/2/run")
    public Map<String,Object> multithreading(@RequestParam(defaultValue="20") int tasks) {
        if(tasks<1||tasks>500) throw new IllegalArgumentException("tasks must be between 1 and 500");
        long start=System.nanoTime();
        List<CompletableFuture<Map<String,Object>>> futures=new ArrayList<>();
        for(int i=1;i<=tasks;i++){ final int id=i;
            futures.add(CompletableFuture.supplyAsync(()->Map.of("taskId",id,"thread",Thread.currentThread().getName(),"completedAt",Instant.now().toString()),pool));
        }
        List<Map<String,Object>> results=futures.stream().map(CompletableFuture::join).toList();
        return Map.of("experiment",2,"tasksRequested",tasks,"tasksCompleted",results.size(),
            "elapsedMillis",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start),"results",results);
    }

    @PostMapping("/3/clock-sync")
    public Map<String,Object> clock(@RequestParam long clientSendMillis,@RequestParam long clientReceiveMillis,@RequestParam long serverTimeMillis) {
        if(clientReceiveMillis<clientSendMillis) throw new IllegalArgumentException("clientReceiveMillis must be >= clientSendMillis");
        long rtt=clientReceiveMillis-clientSendMillis, estimate=serverTimeMillis+rtt/2;
        return Map.of("experiment",3,"algorithm","Cristian-style single-sample estimate","roundTripMillis",rtt,"estimatedServerTimeMillis",estimate,"estimatedServerTime",Instant.ofEpochMilli(estimate).toString());
    }

    @PostMapping("/4/election")
    public Map<String,Object> election(@RequestParam(defaultValue="1,2,3") String activeServerIds) {
        Set<Integer> active=new TreeSet<>();
        for(String part:activeServerIds.split(",")){ try {int id=Integer.parseInt(part.trim()); if(id>=1&&id<=3)active.add(id);}catch(NumberFormatException ex){throw new IllegalArgumentException("Use IDs such as 1,2,3");}}
        if(active.isEmpty())throw new IllegalArgumentException("At least one active server ID (1-3) is required");
        int leader=((TreeSet<Integer>)active).last();
        jdbc.update("UPDATE server_nodes SET role='REPLICA'");
        jdbc.update("UPDATE server_nodes SET role='PRIMARY',status='UP',last_heartbeat=CURRENT_TIMESTAMP WHERE server_id=?",leader);
        return Map.of("experiment",4,"algorithm","Highest-ID coordinator election demo","activeServers",active,"electedLeader",leader,"note","Simulation, not a distributed failure detector.");
    }

    @PostMapping("/5/failover-demo")
    public Map<String,Object> failover(@RequestParam(defaultValue="1") int failedServerId) {
        if(failedServerId<1||failedServerId>3)throw new IllegalArgumentException("failedServerId must be 1-3");
        List<Integer> candidates=jdbc.query("SELECT server_id FROM server_nodes WHERE server_id<>? ORDER BY server_id DESC",(rs,n)->rs.getInt(1),failedServerId);
        int promoted=candidates.get(0);
        jdbc.update("UPDATE server_nodes SET status=CASE WHEN server_id=? THEN 'DOWN' ELSE 'UP' END",failedServerId);
        jdbc.update("UPDATE server_nodes SET role='REPLICA'");
        jdbc.update("UPDATE server_nodes SET role='PRIMARY' WHERE server_id=?",promoted);
        return Map.of("experiment",5,"failedServer",failedServerId,"promotedServer",promoted,"note","Simulation; no process is killed.");
    }

    @PostMapping("/6/consistency-demo")
    public Map<String,Object> consistency(@RequestParam(defaultValue="SYNCHRONOUS") String mode) {
        String m=mode.toUpperCase(Locale.ROOT);
        if(!Set.of("SYNCHRONOUS","ASYNCHRONOUS").contains(m))throw new IllegalArgumentException("mode must be SYNCHRONOUS or ASYNCHRONOUS");
        return Map.of("experiment",6,"mode",m,"acknowledgement",m.equals("SYNCHRONOUS")?"after replica acknowledgement":"after local write","note","Policy simulation only; no replica write is performed.");
    }

    @PostMapping("/7/load-balance")
    public Map<String,Object> loadBalance(@RequestParam(defaultValue="ROUND_ROBIN") String algorithm,@RequestParam(defaultValue="1,2,3") String servers) {
        List<Integer> ids=new ArrayList<>();
        for(String p:servers.split(",")){try{int id=Integer.parseInt(p.trim());if(id>=1&&id<=3)ids.add(id);}catch(NumberFormatException ex){throw new IllegalArgumentException("servers must be IDs");}}
        if(ids.isEmpty())throw new IllegalArgumentException("At least one valid server required");
        String a=algorithm.toUpperCase(Locale.ROOT); int chosen;
        if(a.equals("RANDOM"))chosen=ids.get(ThreadLocalRandom.current().nextInt(ids.size()));
        else if(a.equals("LEAST_ID"))chosen=ids.stream().min(Integer::compareTo).orElseThrow();
        else if(a.equals("ROUND_ROBIN"))chosen=ids.get((int)(System.currentTimeMillis()/1000%ids.size()));
        else throw new IllegalArgumentException("algorithm must be ROUND_ROBIN, RANDOM, or LEAST_ID");
        return Map.of("experiment",7,"algorithm",a,"eligibleServers",ids,"selectedServer",chosen);
    }

    @PostMapping("/8/mapreduce")
    public Map<String,Object> mapReduce(@RequestBody List<Map<String,Object>> rows) {
        if(rows==null||rows.size()>10000)throw new IllegalArgumentException("Provide at most 10000 rows");
        Map<String,Long> result=new TreeMap<>();
        for(Map<String,Object> row:rows){Object c=row.get("category"),q=row.get("quantity");if(c==null||!(q instanceof Number))throw new IllegalArgumentException("Each row needs category and numeric quantity");result.merge(c.toString(),((Number)q).longValue(),Long::sum);}
        return Map.of("experiment",8,"inputRows",rows.size(),"reduceResult",result);
    }

    @PostMapping("/9/collective-demo")
    public Map<String,Object> collective(@RequestParam(defaultValue="BROADCAST") String operation,@RequestParam(defaultValue="3") int processes,@RequestParam(defaultValue="10") int value) {
        String op=operation.toUpperCase(Locale.ROOT);
        if(!Set.of("BROADCAST","SCATTER","GATHER","REDUCE","ALLREDUCE","BARRIER").contains(op))throw new IllegalArgumentException("Unsupported collective operation");
        if(processes<1||processes>64)throw new IllegalArgumentException("processes must be 1-64");
        List<Integer> ranks=new ArrayList<>();for(int i=0;i<processes;i++)ranks.add(i);
        return Map.of("experiment",9,"operation",op,"processCount",processes,"simulatedRanks",ranks,"inputValue",value,"reductionSum",(long)value*processes,"note","Simulation, not actual MPI runtime.");
    }
}
