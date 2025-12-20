package com.trackIt.independent_services.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponse {

    private Long companyId;

    @NotNull(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Company type is required")
    private String companyType;

    private boolean isDeleted;
}
