package com.trackIt.incident_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromiseResponse {

    private Integer response_time_mins;
    private Integer resolution_time_mins;;

}
