package com.trackIt.incident_service.mapper;

import com.trackIt.incident_service.dto.response.CommentResponse;
import com.trackIt.incident_service.model.Comments;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static List<CommentResponse> toResponseList(
            List<Comments> comments,
            String name,
            String employeeId
    ) {

        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(comment -> toResponse(comment, name, employeeId))
                .collect(Collectors.toList());
    }


    public static CommentResponse toResponse(Comments comment, String name, String employeeId) {

        if (comment == null) return null;

        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .incidentId(comment.getIncident().getIncidentId())
                .name(name)
                .employeeId(employeeId)
                .comment(comment.getComment())
                .createdAt(format(comment.getCreatedAt()))
                .build();

    }

    private static String format(LocalDateTime time) {
        return time != null ? time.format(FORMATTER) : null;
    }


}
