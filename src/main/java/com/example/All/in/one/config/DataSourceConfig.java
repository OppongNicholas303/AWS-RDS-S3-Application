package com.example.All.in.one.config;

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

    private final SsmClient ssmClient;

    public DataSourceConfig() {
        this.ssmClient = SsmClient.builder()
                .region(Region.US_EAST_2)  // Match your AWS region
                .build();
    }

    private String getParameterValue(String parameterName) {
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();
            return ssmClient.getParameter(request).parameter().value();
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve database parameter: " + parameterName, e);
        }
    }

    @Bean
    public DataSource dataSource() {
        try {
            // Retrieve database parameters from SSM
            String endpoint = getParameterValue("/s3-image-upload-app/database/endpoint");
            String dbName = getParameterValue("/s3-image-upload-app/database/name");
            String username = getParameterValue("/s3-image-upload-app/database/username");
            String password = getParameterValue("/s3-image-upload-app/database/password");

            // Construct JDBC URL with improved configuration
            String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:5432/%s?connectTimeout=10000" +
                            "&ssl=true" +
                            "&sslmode=require" +
                            "&tcpKeepAlive=true" +
                            "&socketTimeout=30000" +
                            "&applicationName=s3-image-upload-app" +
                            "&loggerLevel=TRACE", // Add detailed logging
                    endpoint, dbName
            );

            return DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create DataSource", e);
        }
    }
}