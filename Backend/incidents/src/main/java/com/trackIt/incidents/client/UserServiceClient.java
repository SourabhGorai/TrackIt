package com.trackIt.incidents.client;

import com.trackIt.incidents.dto.ApiResponse;
import com.trackIt.incidents.dto.response.RoleResponse;
import com.trackIt.incidents.dto.response.UserResponsePublic;
import com.trackIt.incidents.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.users2-service.url}")
    private String users2ServiceUrl;

    public UserResponsePublic getUserDetails(Long userId) {
        try {
            log.info("Fetching user name for userId: {}", userId);

            ApiResponse<UserResponsePublic> response = webClientBuilder.build()
                    .get()
                    .uri(users2ServiceUrl + "/users/public/{userId}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponsePublic>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response for user ID: {}", userId);
            return null;

        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {}", userId, e);
            throw new ExternalServiceException(
                    "Unable to fetch user details from User Service", e
            );
        }
    }

    public UserResponsePublic getUserDetailsByEmployeeId(String employeeId) {
        try {
            log.info("Fetching user name for employee Id: {}", employeeId);

            ApiResponse<UserResponsePublic> response = webClientBuilder.build()
                    .get()
                    .uri(users2ServiceUrl + "/users/public/employeeId/{employeeId}", employeeId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponsePublic>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response for employee ID: {}", employeeId);
            return null;

        } catch (Exception e) {
            log.error("Failed to fetch user details for employeeId: {}", employeeId, e);
            throw new ExternalServiceException(
                    "Unable to fetch user details from User Service", e
            );
        }
    }

}
