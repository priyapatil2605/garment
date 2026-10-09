CREATE TABLE products (
  product_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(150) NOT NULL,
  category VARCHAR(80) NOT NULL,
  description TEXT,
  price DECIMAL(12,2) NOT NULL,
  image_url TEXT,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_product_price CHECK (price >= 0)
);

CREATE TABLE server_nodes (
  server_id INT PRIMARY KEY,
  server_name VARCHAR(100) NOT NULL,
  host VARCHAR(255) NOT NULL,
  http_port INT NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
  role VARCHAR(20) NOT NULL DEFAULT 'REPLICA',
  last_heartbeat TIMESTAMP NULL,
  CONSTRAINT chk_server_id CHECK (server_id BETWEEN 1 AND 3)
);

CREATE TABLE inventory (
  product_id BIGINT NOT NULL,
  server_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  reserved_quantity INT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id, server_id),
  CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(product_id),
  CONSTRAINT fk_inventory_server FOREIGN KEY (server_id) REFERENCES server_nodes(server_id),
  CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0),
  CONSTRAINT chk_inventory_reserved CHECK (reserved_quantity >= 0 AND reserved_quantity <= quantity)
);

CREATE TABLE orders (
  order_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(150) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  processed_by_server INT NULL,
  idempotency_key VARCHAR(100) UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_server FOREIGN KEY (processed_by_server) REFERENCES server_nodes(server_id),
  CONSTRAINT chk_order_total CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
  order_item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(150) NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  line_total DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
  CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
  CONSTRAINT chk_order_item_prices CHECK (unit_price >= 0 AND line_total >= 0)
);

CREATE TABLE replication_log (
  replication_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  source_server_id INT NULL,
  target_server_id INT NULL,
  state_version BIGINT NOT NULL,
  consistency_mode VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_replication_source FOREIGN KEY (source_server_id) REFERENCES server_nodes(server_id),
  CONSTRAINT fk_replication_target FOREIGN KEY (target_server_id) REFERENCES server_nodes(server_id)
);

CREATE TABLE experiment_runs (
  run_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  experiment_number INT NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  input_data JSON NULL,
  result_data JSON NULL,
  started_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  CONSTRAINT chk_experiment_number CHECK (experiment_number BETWEEN 1 AND 9)
);

CREATE TABLE system_events (
  event_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  server_id INT NULL,
  event_type VARCHAR(80) NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_system_events_server FOREIGN KEY (server_id) REFERENCES server_nodes(server_id)
);

CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_replication_created_at ON replication_log(created_at);
CREATE INDEX idx_experiment_runs_number ON experiment_runs(experiment_number);
