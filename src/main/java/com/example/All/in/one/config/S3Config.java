package com.example.All.in.one.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .build();  // Use the default credentials provider chain
    }

    @Bean
    public SsmClient ssmClient() {
        return SsmClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
//
//    @Bean
//    public S3Client s3Client() {
//        return S3Client.builder()
//                .region(Region.of(awsRegion))
//                .credentialsProvider(InstanceProfileCredentialsProvider.create())
//                .build();
//    }

//    @Bean
//    public S3Client s3Client() {
//        return S3Client.builder()
//                .region(Region.of(awsRegion))
//                .credentialsProvider(ProfileCredentialsProvider.create()) // Uses ~/.aws/credentials
//                .build();
//    }
}