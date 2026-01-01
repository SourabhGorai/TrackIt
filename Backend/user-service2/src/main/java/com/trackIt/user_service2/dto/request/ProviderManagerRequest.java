package com.trackIt.user_service2.dto.request;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderManagerRequest {

    // 24 hrs clock format, hh:mm
    @NotNull
    private LocalTime shiftStart;

    @NotNull
    private LocalTime shiftEnd;
}