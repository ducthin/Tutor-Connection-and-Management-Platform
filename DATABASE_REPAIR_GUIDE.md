# Database Repair Guide

## Current Issue

The application is failing to start with the error: `Schema 'tcmp' contains a failed migration to version 35`. However, our project only has migration files up to V24.

## Solution Steps

### 1. Temporary Solution (Current Implementation)

We've applied a temporary solution to allow the application to start:

- Disabled Flyway migrations (`spring.flyway.enabled=false`)
- Enabled Hibernate's automatic schema update (`spring.jpa.hibernate.ddl-auto=update`)

This allows the application to run while we prepare a proper database repair.

### 2. Permanent Solution

To properly fix the database, follow these steps once the application is running:

#### Option A: Using MySQL Directly

1. Connect to your MySQL database:
   ```
   mysql -u root -p
   ```

2. Select the database:
   ```
   USE TCMP;
   ```

3. Execute the repair commands:
   ```sql
   -- Delete the entry for the failed migration (V35)
   DELETE FROM flyway_schema_history WHERE version = '35';

   -- Optional: Remove any entries higher than your last valid version (V24)
   DELETE FROM flyway_schema_history WHERE CAST(version AS UNSIGNED) > 24;

   -- Optional: Mark the latest migration (V24) as the most recent successful one
   UPDATE flyway_schema_history 
   SET success = TRUE
   WHERE version = '24';
   ```

#### Option B: Using the SQL file

1. Use a database administration tool like MySQL Workbench or DBeaver
2. Connect to your TCMP database
3. Run the SQL commands from `src/main/resources/db/repair_flyway_schema.sql`

### 3. Re-enabling Flyway

After repairing the database:

1. Edit `src/main/resources/application.properties`
2. Uncomment and re-enable the Flyway configuration:
   ```properties
   spring.flyway.enabled=true
   spring.flyway.baseline-on-migrate=true
   spring.flyway.locations=classpath:db/migration
   ...
   ```
3. Change Hibernate back to non-destructive mode:
   ```properties
   spring.jpa.hibernate.ddl-auto=none
   ```

## Prevention for the Future

1. Always keep migration files in sync with the database
2. Before deleting any migration files, ensure they're properly handled in the database
3. Consider using `spring.flyway.repair-on-migrate=true` (already in your configuration)
4. For development environments, consider using `spring.flyway.clean-on-validation-error=true` but be careful as this will delete data

## If Problems Persist

Another option is to completely reset the database:

```sql
DROP DATABASE TCMP;
CREATE DATABASE TCMP;
```

Then restart the application with Flyway enabled to recreate everything from scratch.