package com.upsilon.TCMP;

import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.DataSourceBuilder;
import javax.sql.DataSource;

public class FlywayRepairUtil {
    
    public static void main(String[] args) {
        // Create the datasource with the same configuration as in application.properties
        DataSource dataSource = DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/TCMP?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true")
            .username("root")
            .password("ducthinh123")
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build();

        // Create Flyway instance
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .outOfOrder(true)
            .cleanDisabled(false)
            .validateOnMigrate(false)
            .placeholders(java.util.Collections.singletonMap("schema", "tcmp"))
            .mixed(true)
            .locations("classpath:db/migration")
            .load();

        // Repair the schema history table
        flyway.repair();
        
        System.out.println("Flyway repair completed successfully.");
    }
}