package com.example.All.in.one.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Service
@Slf4j
public class ParameterStoreService {

    private final SsmClient ssmClient;

    public ParameterStoreService(SsmClient ssmClient) {
        this.ssmClient = ssmClient;
    }

    public String getParameterValue(String parameterName) {
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            return response.parameter().value();
        } catch (Exception e) {
            log.error("Error fetching parameter {}: {}", parameterName, e.getMessage());
            throw new RuntimeException("Failed to fetch parameter: " + parameterName, e);
        }
    }
}
