package com.trackIt.incident_service.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentsRequest {

    private Long incidentId;

    private String comment;

}
