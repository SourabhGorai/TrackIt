package com.trackIt.incident_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trackIt.incident_service.model.Status;
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
public class ReporterRequest {

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Description is required")
    private String description;

    @NotNull(message = "Service Id is required")
    private Long serviceId;

    @NotNull(message = "Priority is required")
    private Long priorityId;

    @NotNull(message = "Status is required")
    private Status status;

}
