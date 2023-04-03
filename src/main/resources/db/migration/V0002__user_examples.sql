INSERT INTO users (username, password, enabled) VALUES
    ('user1', '{bcrypt}$2a$12$4QhdvdlcuufmT3vxxtFKG.EdW5MX46AiatDj.1c8C.B.EAu8e1TF.', TRUE),
    ('user2', '{bcrypt}$2a$12$UwDHm804..w1hVs9emnBq.nK9rrHc6YsqirTdncYYv/JW4TArmBZa', TRUE);

INSERT INTO authorities (user_id, authority) VALUES
    (1, 'ROLE_USER'),
    (2, 'ROLE_USER');
