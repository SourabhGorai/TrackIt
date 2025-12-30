package com.trackIt.incidents.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trackIt.incidents.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentResponsePublic {

    private Long incidentId;
    private String title;
    private Long serviceId;
    private String priority;
    private Status status;
    private String reportedBy;
    private String assignedTo;
    private String reportedAt;
    private String resolvedAt;

}
