# DragonFruit Distributed Store — Member 2 backend (MySQL)

## 1. Create the database in MySQL Workbench
Open MySQL Workbench and connect to your local MySQL server. Run this once:
```sql
CREATE DATABASE dragonfruit_store;
```
Select the new schema from the Schemas panel. Flyway will create all tables and seed demo products when the backend first starts; do not manually run the migration files.

## 2. Configure credentials in PowerShell
From `C:\Users\PRIYA\Desktop\garment\backend\core`:
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/dragonfruit_store?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = Read-Host "Enter your MySQL password"
$env:SERVER_PORT = "8080"
$env:SERVER_ID = "1"
```
Use your actual MySQL username/password. MySQL Workbench is the GUI client; the MySQL Server service must also be running.

## 3. Build and start
```powershell
mvn -U clean package
mvn spring-boot:run
```
Then in another terminal:
```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/products
Invoke-RestMethod http://localhost:8080/api/inventory
Invoke-RestMethod http://localhost:8080/api/orders
Invoke-RestMethod http://localhost:8080/api/servers
Invoke-RestMethod http://localhost:8080/api/experiments
```

## 4. Create an order using PowerShell
```powershell
$body = @{
  customerName = "Demo Customer"
  idempotencyKey = "demo-order-001"
  items = @(@{ productId = 1; quantity = 2 })
} | ConvertTo-Json -Depth 5
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders -ContentType "application/json" -Body $body
```

## 5. Three local backend instances
Each instance uses the same MySQL database in this demo. Set credentials in each terminal before starting.

Server 1:
```powershell
$env:SERVER_PORT="8080"; $env:SERVER_ID="1"; mvn spring-boot:run
```
Server 2:
```powershell
$env:SERVER_PORT="8081"; $env:SERVER_ID="2"; mvn spring-boot:run
```
Server 3:
```powershell
$env:SERVER_PORT="8082"; $env:SERVER_ID="3"; mvn spring-boot:run
```
If opening a new terminal, re-set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` there first. These are three app processes sharing one database, not three independent MySQL replicas.

## 6. APIs
- `GET /api/health`
- `GET /api/products`, `GET /api/products/{id}`, `POST /api/products`
- `GET /api/inventory`, `GET /api/inventory?serverId=2`, `PUT /api/inventory/{productId}?serverId=1`
- `GET /api/orders`, `GET /api/orders/{id}`, `POST /api/orders`
- `GET /api/servers`, `POST /api/servers/{id}/heartbeat`
- `GET /api/experiments`
- Exp 1 `POST /api/experiments/1/rpc-demo?message=hello` (REST demo, not actual Java RMI)
- Exp 2 `POST /api/experiments/2/run?tasks=20`
- Exp 3 `POST /api/experiments/3/clock-sync?clientSendMillis=1000&clientReceiveMillis=1040&serverTimeMillis=1020`
- Exp 4 `POST /api/experiments/4/election?activeServerIds=1,2,3` (simulation)
- Exp 5 `POST /api/experiments/5/failover-demo?failedServerId=1` (simulation)
- Exp 6 `POST /api/experiments/6/consistency-demo?mode=SYNCHRONOUS` (policy simulation)
- Exp 7 `POST /api/experiments/7/load-balance?algorithm=ROUND_ROBIN&servers=1,2,3`
- Exp 8 `POST /api/experiments/8/mapreduce` with JSON `[{"category":"Jeans","quantity":2},{"category":"Jeans","quantity":3}]`
- Exp 9 `POST /api/experiments/9/collective-demo?operation=REDUCE&processes=3&value=10` (simulation, not actual MPI)

## Scope caveat
This is a broad backend starter. Experiments 1, 5, 6, and 9 are explicitly demonstrations/simulations, not production implementations of Java RMI, automatic failover, real replication, or MPI. Follow the lab manuals and integrate the actual algorithms before claiming those experiments are fully implemented.
