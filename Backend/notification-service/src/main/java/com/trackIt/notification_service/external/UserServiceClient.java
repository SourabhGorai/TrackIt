package com.trackIt.notification_service.external;

import com.trackIt.notification_service.dto.ApiResponse;
import com.trackIt.notification_service.dto.ProviderManagerFullResponse;
import com.trackIt.notification_service.dto.UserResponsePublic;
import com.trackIt.notification_service.exception.ExternalServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.user-service.url}")
    private String userServiceUrl;

    @Value("${app.user-service.internal-token}")
    private String internalToken;

    private WebClient client() {
        return webClientBuilder.build();
    }

    private WebClient.RequestHeadersSpec<?> withInternalAuth(
            WebClient.RequestHeadersSpec<?> request
    ) {
        return request.header("X-Internal-Service", "notification-service")
                .header("X-Internal-Token", internalToken);
    }

    public UserResponsePublic getUserDetails(Long userId) {

        try {
            log.info("Fetching user details for userId={}", userId);

            ApiResponse<UserResponsePublic> response =
                    withInternalAuth(
                            client().get()
                                    .uri(userServiceUrl + "/users/public/{id}", userId)
                    )
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponsePublic>>() {})
                            .block(Duration.ofSeconds(5));

            return response != null ? response.getData() : null;

        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Failed to fetch user details", e
            );
        }
    }

    public List<ProviderManagerFullResponse> getProviderManagersByCompanyId(Long compId) {

        try {
            log.info("Fetching provider managers for companyId={}", compId);

            ApiResponse<List<ProviderManagerFullResponse>> response =
                    withInternalAuth(
                            client().get()
                                    .uri(userServiceUrl + "/users/details/pmByCompanyId/{id}", compId)
                    )
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<
                                    ApiResponse<List<ProviderManagerFullResponse>>>() {})
                            .block(Duration.ofSeconds(5));

            return response != null ? response.getData() : List.of();

        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Failed to fetch provider managers", e
            );
        }
    }

    public UserResponsePublic getUserDetailsByEmployeeId(String employeeId) {

        try {
            log.info("Fetching user name for employee Id: {}", employeeId);

            ApiResponse<UserResponsePublic> response = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/users/public/employeeId/{employeeId}", employeeId)
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
