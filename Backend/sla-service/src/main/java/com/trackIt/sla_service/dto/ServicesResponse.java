package com.trackIt.sla_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServicesResponse {

    private Long serviceId;
    private String serviceName;
    private Long clientCompanyId;
    private Long providerCompanyId;
}
