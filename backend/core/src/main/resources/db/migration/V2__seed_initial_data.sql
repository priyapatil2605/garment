INSERT INTO server_nodes(server_id,server_name,host,http_port,status,role)
VALUES (1,'Server 1','localhost',8080,'UP','PRIMARY'),
       (2,'Server 2','localhost',8081,'UP','REPLICA'),
       (3,'Server 3','localhost',8082,'UP','REPLICA');

INSERT INTO products(sku,name,category,description,price,image_url) VALUES
('DF-JKT-001','Classic Denim Jacket','Jackets','Classic everyday denim jacket',1999.00,NULL),
('DF-JNS-001','Premium Blue Jeans','Jeans','Regular-fit blue jeans',1299.00,NULL),
('DF-SHT-001','Oversized Cotton Shirt','Shirts','Comfortable cotton shirt',899.00,NULL),
('DF-PNT-001','Relaxed Cargo Pants','Pants','Relaxed-fit cargo trousers',1499.00,NULL),
('DF-HOD-001','Everyday Hoodie','Hoodies','Soft casual hoodie',1199.00,NULL),
('DF-DRS-001','Linen Summer Dress','Dresses','Lightweight summer dress',1799.00,NULL);

INSERT INTO inventory(product_id,server_id,quantity)
SELECT p.product_id,s.server_id,25 FROM products p CROSS JOIN server_nodes s;
