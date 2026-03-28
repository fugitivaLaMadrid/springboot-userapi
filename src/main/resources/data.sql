-- Sample users for development
INSERT INTO users (username, email, created_at)
VALUES
    ('alice', 'alice@example.com', NOW()),
    ('bob', 'bob@example.com', NOW()),
    ('carol', 'carol@example.com', NOW());