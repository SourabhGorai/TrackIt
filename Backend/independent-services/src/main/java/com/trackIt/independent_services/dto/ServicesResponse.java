package com.trackIt.independent_services.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

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
