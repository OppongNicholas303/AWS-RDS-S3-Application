//package com.example.All.in.one.config;
//
//import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
//import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
//import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DataSourceConfig {
//    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);
//
//    private final AWSSimpleSystemsManagement ssmClient;
//
//    @Value("${aws.ssm.db-endpoint-param}")
//    private String dbEndpointParam;
//
//    @Value("${aws.ssm.db-name-param}")
//    private String dbNameParam;
//
//    @Value("${aws.ssm.db-username-param}")
//    private String dbUsernameParam;
//
//    @Value("${aws.ssm.db-password-param}")
//    private String dbPasswordParam;
//
//    @Value("${spring.datasource.timeout:30000}")  // Default 30 seconds timeout
//    private int connectionTimeout;
//
//    public DataSourceConfig() {
//        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
//                .build();
//    }
//
//    private String getParameterValue(String parameterName) {
//        try {
//            GetParameterRequest request = new GetParameterRequest()
//                    .withName(parameterName)
//                    .withWithDecryption(true);
//
//            String value = ssmClient.getParameter(request).getParameter().getValue();
//            logger.info("Successfully retrieved parameter: {}", parameterName);
//            return value;
//        } catch (Exception e) {
//            logger.error("Error retrieving parameter {}: {}", parameterName, e.getMessage());
//            throw new RuntimeException("Could not retrieve database parameter: " + parameterName, e);
//        }
//    }
//
//    @Bean
//    @Primary
//    public DataSource dataSource() {
//        try {
//            String endpoint = getParameterValue(dbEndpointParam);
//            String dbName = getParameterValue(dbNameParam);
//            String username = getParameterValue(dbUsernameParam);
//            String password = getParameterValue(dbPasswordParam);
//
//            logger.info("Configuring DataSource with endpoint: {}, database: {}", endpoint, dbName);
//
////            String jdbcUrl = String.format("jdbc:postgresql://%s:5432/%s?connectTimeout=%d",
////                    endpoint, dbName, connectionTimeout);
//
//            String jdbcUrl = String.format("jdbc:postgresql://%s:5432/%s?connectTimeout=%d&ssl=true",
//                    endpoint, dbName, connectionTimeout);
//
//            return DataSourceBuilder.create()
//                    .url(jdbcUrl)
//                    .username(username)
//                    .password(password)
//                    .driverClassName("org.postgresql.Driver")
//                    .build();
//        } catch (Exception e) {
//            logger.error("Failed to create DataSource", e);
//            throw new RuntimeException("Could not create DataSource", e);
//        }
//    }
//}

package com.example.All.in.one.config;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    private final AWSSimpleSystemsManagement ssmClient;

    @Value("${aws.ssm.db-endpoint-param:/s3-image-upload-app/database/endpoint}")
    private String dbEndpointParam;

    @Value("${aws.ssm.db-name-param:/s3-image-upload-app/database/name}")
    private String dbNameParam;

    @Value("${aws.ssm.db-username-param:/s3-image-upload-app/database/username}")
    private String dbUsernameParam;

    @Value("${aws.ssm.db-password-param:/s3-image-upload-app/database/password}")
    private String dbPasswordParam;

    @Value("${spring.datasource.timeout:30000}")
    private int connectionTimeout;

    public DataSourceConfig() {
        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
                .build();
    }

    private String getParameterValue(String parameterName) {
        try {
            GetParameterRequest request = new GetParameterRequest()
                    .withName(parameterName)
                    .withWithDecryption(true);

            String value = ssmClient.getParameter(request).getParameter().getValue();
            logger.info("Successfully retrieved parameter: {}", parameterName);
            return value;
        } catch (Exception e) {
            logger.error("Error retrieving parameter {}: {}", parameterName, e.getMessage(), e);
            throw new RuntimeException("Could not retrieve database parameter: " + parameterName, e);
        }
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            // Retrieve database parameters from SSM Parameter Store
            String endpoint = getParameterValue(dbEndpointParam);
            String dbName = getParameterValue(dbNameParam);
            String username = getParameterValue(dbUsernameParam);
            String password = getParameterValue(dbPasswordParam);

            logger.info("Configuring DataSource with endpoint: {}, database: {}", endpoint, dbName);

            String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:5432/%s?connectTimeout=%d" +
                            "&ssl=true" +
                            "&sslmode=require" +
                            "&tcpKeepAlive=true" +
                            "&socketTimeout=30" +
                            "&applicationName=s3-image-upload-app",
                    endpoint, dbName, connectionTimeout
            );

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        } catch (Exception e) {
            logger.error("Failed to create DataSource", e);
            throw new RuntimeException("Could not create DataSource", e);
        }
    }
}
