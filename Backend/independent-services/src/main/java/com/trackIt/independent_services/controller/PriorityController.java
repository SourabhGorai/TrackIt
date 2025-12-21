package com.trackIt.independent_services.controller;

import com.trackIt.independent_services.dto.ApiResponse;
import com.trackIt.independent_services.mapper.PriorityMapper;
import com.trackIt.independent_services.model.Priorities;
import com.trackIt.independent_services.service.PriorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/priority")
@RequiredArgsConstructor
@Slf4j
public class PriorityController {

    private final PriorityService priorityService;

    @PostMapping("/{priority}")
    public ResponseEntity<ApiResponse<Priorities>> addPriority(@PathVariable String priority) {

        String sanitizedPriority = PriorityMapper.sanitizeName(priority);
        log.info("Request received to add priority {} in database", sanitizedPriority);

        return ResponseEntity.ok(
                ApiResponse.success("New Priority Level added successfully",
                        priorityService.addNew(sanitizedPriority))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Priorities>>> getAll(){

        log.info("Request received to list all existing priorities");

        List<Priorities> list = priorityService.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Fetched %d priorities", list.size()), list)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePriority(@PathVariable Long id){
        log.info("REST received to delete role: {}", id);
        priorityService.deletePriority(id);
        log.info("Deleted role: {}", id);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Deleted role: %s", id))
        );

    }

}
