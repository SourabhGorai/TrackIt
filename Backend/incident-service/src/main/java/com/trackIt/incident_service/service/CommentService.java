package com.trackIt.incident_service.service;

import com.trackIt.incident_service.client.UserServiceClient;
import com.trackIt.incident_service.dto.request.CommentsRequest;
import com.trackIt.incident_service.dto.response.CommentResponse;
import com.trackIt.incident_service.dto.response.UserResponsePublic;
import com.trackIt.incident_service.exception.NotFoundException;
import com.trackIt.incident_service.exception.ServiceException;
import com.trackIt.incident_service.mapper.CommentMapper;
import com.trackIt.incident_service.model.Comments;
import com.trackIt.incident_service.model.Incident;
import com.trackIt.incident_service.repository.CommentRepository;
import com.trackIt.incident_service.repository.IncidentRepository;
import com.trackIt.incident_service.repository.IncidentSlaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentSlaRepository incidentSlaRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public CommentResponse createComment(CommentsRequest req, Long userId) {

        log.info("Attempting to create a comment on incident with ID: {}", req.getIncidentId());

        Incident incident = incidentRepository.findById(req.getIncidentId()).orElseThrow(
                () -> new NotFoundException("Incident", req.getIncidentId().toString())
        );

        Comments comment = Comments.builder()
                .incident(incident)
                .userId(userId)
                .comment(req.getComment())
                .build();

        try {
            Comments saved = commentRepository.save(comment);

            UserResponsePublic user = userServiceClient.getUserDetails(userId);

            return CommentMapper.toResponse(saved, user.getName(), user.getEmployeeId());

        } catch (Exception e) {
            log.info("Error in saving comment");
            throw new ServiceException(String.format(
                    "Error creating comment for Incident ID: %s",
                    req.getIncidentId().toString()
            ));
        }
    }


    public List<CommentResponse> getAll(Long userId, String role) {

        log.info("Attempting to fetch all the comments for user: {}", userId);
        List<Comments> resp = new ArrayList<>();

        if(role.equals("ADMIN")){
            resp = commentRepository.findAll();
        }else{
            resp = commentRepository.findAllByUserId(userId);
        }

        UserResponsePublic user = userServiceClient.getUserDetails(userId);

        return CommentMapper.toResponseList(resp, user.getName(), user.getEmployeeId());

    }

    public List<CommentResponse> getByUserId(Long id) {

        log.info("Attempting to fetch all the comments for userId: {}", id);

        List<Comments> resp = commentRepository.findAllByUserId(id);

        UserResponsePublic user = userServiceClient.getUserDetails(id);

        return CommentMapper.toResponseList(resp, user.getName(), user.getEmployeeId());

    }

    public List<CommentResponse> getByIncidentId(Long id) {

        log.info("Attempting to fetch all the comments for Incident Id: {}", id);

        List<Comments> comments =
                commentRepository.findAllByIncident_IncidentId(id);

        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(comment -> {
                    UserResponsePublic user =
                            userServiceClient.getUserDetails(comment.getUserId());

                    return CommentMapper.toResponse(
                            comment,
                            user.getName(),
                            user.getEmployeeId()
                    );
                })
                .toList();
    }

}
