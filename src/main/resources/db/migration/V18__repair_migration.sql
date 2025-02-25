-- Simple repair migration
UPDATE Users SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE Sessions SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE Reviews SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;