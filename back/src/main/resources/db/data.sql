/*-- ==============================================
-- INSERT DEFAULT ROLES
-- ==============================================
INSERT INTO role (id, name, created_date, created_by)
VALUES (1, 'ADMIN', NOW(), 'system');

INSERT INTO role (id, name, created_date, created_by)
VALUES (2, 'USER', NOW(), 'system');

-- ==============================================
-- INSERT ADMIN USER
-- ==============================================
-- Password = admin123 (hashed with BCrypt)
INSERT INTO user (id, firstname, lastname, email, password, enabled, created_date, created_by)
VALUES (1,
        'System',
        'Administrator',
        'admin@admin.com',
           -- Généré avec BCryptPasswordEncoder.encode("admin123")
        '$2a$12$9xgDkEzqT0Ipqe2W8ix4JeQFLwl7nppOGvwepONu25hOM622k5XSy',
        true,
        NOW(),
        'system'
       );

-- ==============================================
-- LINK ADMIN USER WITH ADMIN ROLE
-- ==============================================
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);
*/