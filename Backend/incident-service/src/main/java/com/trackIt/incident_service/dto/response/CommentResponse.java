package com.trackIt.incident_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponse {

    private Long commentId;
    private Long incidentId;
    private String name;
    private String employeeId;
    private String comment;
    private String createdAt;

}
