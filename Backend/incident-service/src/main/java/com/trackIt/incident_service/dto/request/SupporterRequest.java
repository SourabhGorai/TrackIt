package com.trackIt.incident_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trackIt.incident_service.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupporterRequest {

    private Long incidentId;
    private Status status;

}
