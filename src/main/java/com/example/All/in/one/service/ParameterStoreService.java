package com.example.All.in.one.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Service
public class ParameterStoreService {

    private final SsmClient ssmClient;

    public ParameterStoreService(SsmClient ssmClient) {
        this.ssmClient = ssmClient;
    }

    public String getParameter(String parameterName) {
        try {
            GetParameterRequest parameterRequest = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();

            GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
            return parameterResponse.parameter().value();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving parameter from AWS Parameter Store", e);
        }
    }

    // Optional: Method to get specific keys if parameters are stored as JSON
    public String getParameterField(String parameterName, String field) {
        try {
            String jsonString = getParameter(parameterName);
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(jsonString)
                    .get(field)
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing parameter JSON", e);
        }
    }
}