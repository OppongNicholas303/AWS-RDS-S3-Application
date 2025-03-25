package com.example.All.in.one.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.IOException;

@Service
public class SecretsManagerService {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;

    public SecretsManagerService(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
        this.objectMapper = new ObjectMapper();
    }

    public String getSecret(String secretName, String field) {
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
            String secret = valueResponse.secretString();

            JsonNode jsonNode = objectMapper.readTree(secret);
            return jsonNode.get(field).asText();
        } catch (IOException e) {
            throw new RuntimeException("Error parsing secret JSON", e);
        }
    }
}