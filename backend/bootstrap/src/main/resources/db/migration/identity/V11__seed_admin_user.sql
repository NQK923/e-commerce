-- Seed admin account if not present
INSERT INTO users (id, email, password, auth_provider, provider_user_id, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555555', 'nqk1337@gmail.com', 'QWRtaW5AMTIz', 'LOCAL', NULL, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'nqk1337@gmail.com'
ON CONFLICT DO NOTHING;
