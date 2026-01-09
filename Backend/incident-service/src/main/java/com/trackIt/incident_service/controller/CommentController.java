package com.trackIt.incident_service.controller;

import com.trackIt.incident_service.dto.ApiResponse;
import com.trackIt.incident_service.dto.request.CommentsRequest;
import com.trackIt.incident_service.dto.response.CommentResponse;
import com.trackIt.incident_service.service.CommentService;
import com.trackIt.incident_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CommentsRequest req)
    {

        log.info("REST received to create comments on Incident ID: {}", req.getIncidentId());

        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);

        CommentResponse resp = commentService.createComment(req, userId);

        return ResponseEntity.ok(ApiResponse.success(
                "Comment created successfully",
                resp
        ));

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAll(
            @RequestHeader("Authorization") String authHeader
    ){

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        log.info("REST received to fetch all the comments");
        List<CommentResponse> resp = commentService.getAll(userId, role);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched %d comments", resp.size()),
                resp
        ));
    }

    @GetMapping("/userId/{id}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getByUserId(
            @PathVariable Long id
    ){

        log.info("REST received to fetch comments for user: {}", id);
        List<CommentResponse> resp = commentService.getByUserId(id);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched %d comments", resp.size()),
                resp
        ));
    }

    @GetMapping("/incidentId/{id}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getByIncidentId(
            @PathVariable Long id
    ){

        log.info("REST received to fetch comments for Incident Id: {}", id);
        List<CommentResponse> resp = commentService.getByIncidentId(id);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched %d comments", resp.size()),
                resp
        ));
    }

}
