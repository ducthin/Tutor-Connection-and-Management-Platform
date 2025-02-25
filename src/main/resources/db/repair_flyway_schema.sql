-- Delete the entry for the failed migration (V35)
DELETE FROM flyway_schema_history WHERE version = '35';

-- Optional: Remove any entries higher than your last valid version (V24)
DELETE FROM flyway_schema_history WHERE CAST(version AS UNSIGNED) > 24;

-- Optional: Mark the latest migration (V24) as the most recent successful one
UPDATE flyway_schema_history 
SET success = TRUE
WHERE version = '24';