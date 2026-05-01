MERGE INTO books (isbn, name, author, introduction) KEY (isbn) VALUES
('9789576589185', 'Clean Code', 'Robert C. Martin', 'A handbook of agile software craftsmanship.'),
('9789863126592', 'Designing Data-Intensive Applications', 'Martin Kleppmann', 'Concepts for building reliable and scalable systems.');

MERGE INTO inventory (inventory_id, isbn, store_time, status) KEY (inventory_id) VALUES
(1, '9789576589185', CURRENT_TIMESTAMP, 'AVAILABLE'),
(2, '9789863126592', CURRENT_TIMESTAMP, 'AVAILABLE');
