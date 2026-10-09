# DragonFruit Distributed Store — Member 2 Backend

This module owns the core shopping API, PostgreSQL migrations, and backend experiment adapters.

## Prerequisites
- JDK 21
- PostgreSQL 14+
- Maven, or the Maven Wrapper generated for this project

## Database setup
Create the database once:

```sql
CREATE DATABASE dragonfruit_store;
```

Set local environment variables in PowerShell (change the password and port to match your installation):

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/dragonfruit_store"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="YOUR_LOCAL_PASSWORD"
$env:SERVER_PORT="8080"
```

Do not commit passwords.

## Build and run
From this directory:

```powershell
mvn -U clean test
mvn spring-boot:run
```

If the Maven Wrapper exists, prefer:

```powershell
.\mvnw.cmd -U clean test
.\mvnw.cmd spring-boot:run
```

The wrapper must be generated in this directory, where this `pom.xml` lives. Do not run Maven from a nested `backend` directory.

## Endpoints
- `GET /api/health`
- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/inventory?serverId=1`
- `GET /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders`
- `GET /api/experiments`
- `POST /api/experiments/2/run?tasks=20`
- `POST /api/experiments/3/clock-sync?sendEpochMillis=1000&receiveEpochMillis=1040&serverEpochMillis=1020`
- `POST /api/experiments/6/consistency-demo?mode=SYNCHRONOUS`
- `POST /api/experiments/8/mapreduce`

Example order:

```json
{
  "customerName": "Demo Customer",
  "idempotencyKey": "demo-order-001",
  "items": [
    {"productId": 1, "quantity": 2}
  ]
}
```

## Important integration notes
- This is the core backend module, not the completed three-node cluster.
- `/api/experiments/6/consistency-demo` describes the selected policy; real replication and convergence must be supplied by Member 3.
- Experiment 1's actual RMI interface and remote service binding must be aligned with the lab manual and Member 3's cluster interfaces.
- MapReduce endpoint performs an in-process map/reduce demonstration; adapt the execution model if the lab manual requires a specific framework.
- The current order service uses server 1 inventory by default on port 8080 and maps ports 8081/8082 to server IDs. Coordinate this with the cluster routing design before running multiple nodes against shared data.
