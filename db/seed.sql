INSERT INTO users(name,email,password_hash,role)
VALUES('Test User', 'user@test.com', 'placeholder', 'USER'),
      ('Admin', 'admin@test.com', 'placeholder', 'ADMIN');

INSERT INTO  events (title, venue, starts_at)
VALUES ('Sample Concert', 'City Hall', '2026-12-01 19:00:00');

INSERT INTO SEATS(event_id,seat_label)
SELECT 1, CONCAT(r.row_label,n.NUM)
FROM (SELECT 'A' AS row_label UNION SELECT 'B'UNION SELECT 'C' UNION SELECT 'D'
        UNION SELECT 'E') r
CROSS JOIN (SELECT 1 AS NUM UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION
       SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8
       UNION SELECT 9 UNION SELECT 10) n;