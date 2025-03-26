package com.example.All.in.one.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.regions.Region;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    private final SsmClient ssmClient;

    public DataSourceConfig() {
        this.ssmClient = SsmClient.builder()
                .region(Region.US_EAST_2)
                .build();
    }

    private String getParameterValue(String parameterName) {
        try {
            logger.info("Retrieving parameter: {}", parameterName);
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();
            String value = ssmClient.getParameter(request).parameter().value();
            logger.info("Successfully retrieved parameter: {}", parameterName);
            return value;
        } catch (Exception e) {
            logger.error("Failed to retrieve parameter: {}", parameterName, e);
            throw new RuntimeException("Could not retrieve database parameter: " + parameterName, e);
        }
    }
    @Bean
    public DataSource dataSource() {
        try {
            // Retrieve database parameters from SSM
            String endpoint = getParameterValue("/snapshare/db/endpoint");
            String dbName = getParameterValue("/snapshare/db/name");
            String username = getParameterValue("/snapshare/db/username");
            String password = getParameterValue("/snapshare/db/password");

            // Enhanced logging for debugging
            logger.error("Full Connection Details:");
            logger.error("Endpoint: {}", endpoint);
            logger.error("Database Name: {}", dbName);
            logger.error("Username: {}", username);
            logger.error("Password Length: {}", password != null ? password.length() : "null");

            // Construct JDBC URL with more verbose logging
            String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:5432/%s?connectTimeout=10000" +
                            "&tcpKeepAlive=true" +
                            "&socketTimeout=30000" +
                            "&applicationName=s3-image-upload-app",
                    endpoint, dbName
            );

            logger.error("Constructed JDBC URL: {}", jdbcUrl);

            HikariDataSource dataSource = DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();

            // Existing HikariCP configuration...

            // Additional HikariCP configuration for better debugging
            dataSource.setMaximumPoolSize(10);
            dataSource.setMinimumIdle(5);
            dataSource.setConnectionTimeout(30000);
            dataSource.setIdleTimeout(600000);
            dataSource.setMaxLifetime(1800000);
            dataSource.setPoolName("S3ImageUploadHikariPool");

            logger.info("DataSource successfully created");

            return dataSource;
        } catch (Exception e) {
            logger.error("Comprehensive DataSource Creation Error", e);
            throw new RuntimeException("Detailed DataSource Creation Failure", e);
        }
    }

//    @Bean
//    public DataSource dataSource() {
//        try {
//            // Retrieve database parameters from SSM
//            String endpoint = getParameterValue("/s3-image-upload-app/database/endpoint");
//            String dbName = getParameterValue("/s3-image-upload-app/database/name");
//            String username = getParameterValue("/s3-image-upload-app/database/username");
//            String password = getParameterValue("/s3-image-upload-app/database/password");
//
//            System.out.println("Endpoint: " + endpoint);
//            System.out.println("DB Name: " + dbName);
//            System.out.println("Username: " + username);
//            System.out.println("Password: " + password);
//
//            logger.info("Database Endpoint: {}", endpoint);
//            logger.info("Database Name: {}", dbName);
//            logger.info("Database Username: {}", username);
//
//            // Construct JDBC URL with improved configuration
//            String jdbcUrl = String.format(
//                    "jdbc:postgresql://%s:5432/%s?connectTimeout=10000" +
//                            "&tcpKeepAlive=true" +
//                            "&socketTimeout=30000" +
//                            "&applicationName=s3-image-upload-app",
//                    endpoint, dbName
//            );
//
//            logger.info("Constructed JDBC URL: {}", jdbcUrl);
//
//            HikariDataSource dataSource = DataSourceBuilder.create()
//                    .type(HikariDataSource.class)
//                    .driverClassName("org.postgresql.Driver")
//                    .url(jdbcUrl)
//                    .username(username)
//                    .password(password)
//                    .build();
//
//            // Additional HikariCP configuration for better debugging
//            dataSource.setMaximumPoolSize(10);
//            dataSource.setMinimumIdle(5);
//            dataSource.setConnectionTimeout(30000);
//            dataSource.setIdleTimeout(600000);
//            dataSource.setMaxLifetime(1800000);
//            dataSource.setPoolName("S3ImageUploadHikariPool");
//
//            logger.info("DataSource successfully created");
//            return dataSource;
//        } catch (Exception e) {
//            logger.error("Failed to create DataSource", e);
//            throw new RuntimeException("Failed to create DataSource", e);
//        }
//    }
}