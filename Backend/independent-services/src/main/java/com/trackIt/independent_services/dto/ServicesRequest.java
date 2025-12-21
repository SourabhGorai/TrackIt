package com.trackIt.independent_services.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServicesRequest {

    @NotNull(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Client Company ID is required")
    private Long clientCompanyId;

    @NotNull(message = "Provider Company ID is required")
    private Long providerCompanyId;
}
