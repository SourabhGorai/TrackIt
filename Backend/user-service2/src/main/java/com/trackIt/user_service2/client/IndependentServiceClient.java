package com.trackIt.user_service2.client;

import com.trackIt.user_service2.dto.ApiResponse;
import com.trackIt.user_service2.dto.response.CompanyResponse;
import com.trackIt.user_service2.dto.response.RoleResponse;
import com.trackIt.user_service2.exception.ExternalServiceException;
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
public class IndependentServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final HttpServletRequest request;

    @Value("${app.independent-service.url}")
    private String independentServiceUrl;

    /* =========================
       INTERNAL UTILITY
       ========================= */

    private String getAuthHeader() {
        return request.getHeader("Authorization");
    }

    /* =========================
       ROLE VALIDATION
       ========================= */

    public RoleResponse validateRole(Long roleId) {
        try {
            log.info("Validating role with ID: {}", roleId);

            ApiResponse<RoleResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/roles/validate/{roleId}", roleId)
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<RoleResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response == null || !Boolean.TRUE.equals(response.getSuccess())
                    || response.getData() == null) {

                log.error("Invalid role ID: {}", roleId);
                throw new ExternalServiceException("Invalid role ID: " + roleId);
            }

            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate role with ID: {}", roleId, e);
            throw new ExternalServiceException(
                    "Unable to validate role. Please try again later", e
            );
        }
    }

    public RoleResponse validateRoleByName(String name) {
        try {
            log.info("Validating role with name: {}", name);

            ApiResponse<RoleResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/roles/validateByName/{name}", name)
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<RoleResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response == null || !Boolean.TRUE.equals(response.getSuccess())
                    || response.getData() == null) {

                log.error("Invalid role name: {}", name);
                throw new ExternalServiceException("Invalid role name: " + name);
            }

            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate role with name: {}", name, e);
            throw new ExternalServiceException(
                    "Unable to validate role by name. Please try again later", e
            );
        }
    }

    public List<RoleResponse> validateRolesByIds(List<Long> ids) {
        try {
            log.info("Validating roles by IDs: {}", ids);

            ApiResponse<List<RoleResponse>> response = webClientBuilder.build()
                    .post()
                    .uri(independentServiceUrl + "/roles/idList")
                    .header("Authorization", getAuthHeader())
                    .bodyValue(ids)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<RoleResponse>>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response == null || !Boolean.TRUE.equals(response.getSuccess())
                    || response.getData() == null) {

                log.warn("Empty role list returned for IDs: {}", ids);
                return List.of();
            }

            return response.getData();

        } catch (Exception e) {
            log.error("Failed to validate roles with IDs: {}", ids, e);
            throw new ExternalServiceException(
                    "Unable to validate roles. Please try again later", e
            );
        }
    }

    /* =========================
       COMPANY VALIDATION
       ========================= */

    public CompanyResponse validateCompany(Long companyId) {
        try {
            log.info("Validating company with ID: {}", companyId);

            ApiResponse<CompanyResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/companies/validate/{companyId}", companyId)
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<CompanyResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response == null || !Boolean.TRUE.equals(response.getSuccess())
                    || response.getData() == null) {

                log.error("Invalid company ID: {}", companyId);
                throw new ExternalServiceException("Invalid company ID: " + companyId);
            }

            return response.getData();

        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate company with ID: {}", companyId, e);
            throw new ExternalServiceException(
                    "Unable to validate company. Please try again later", e
            );
        }
    }
}
