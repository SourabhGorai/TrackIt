package com.trackIt.independent_services.mapper;

import com.trackIt.independent_services.dto.CompanyResponse;
import com.trackIt.independent_services.model.Companies;
import com.trackIt.independent_services.model.Priorities;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PriorityMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String sanitizeName(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }

        return priority.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();
    }

    public static List<Priorities> toResponseList(List<Priorities> priorities) {
        if (priorities == null || priorities.isEmpty()){
            return List.of();
        }
        return priorities.stream()
                .map(PriorityMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static Priorities toResponse(Priorities priority) {
        if(priority == null){
            return null;
        }

        return Priorities.builder()
                .priorityId(priority.getPriorityId())
                .priorityLevel(priority.getPriorityLevel())
                .build();
    }

}
