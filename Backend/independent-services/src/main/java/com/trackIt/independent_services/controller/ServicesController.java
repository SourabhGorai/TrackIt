package com.trackIt.independent_services.controller;

import com.trackIt.independent_services.dto.*;
import com.trackIt.independent_services.mapper.ServiceMapper;
import com.trackIt.independent_services.service.ServicesService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/services")
public class ServicesController {

    private final ServicesService servicesService;

    @PostMapping
    public ResponseEntity<ApiResponse<ServicesResponse>> addService(@RequestBody ServicesRequest request){

        request.setServiceName(ServiceMapper.sanitizeName(request.getServiceName()));
        log.info("REST received to add service with name: {}", request.getServiceName());

        ServicesResponse resp = servicesService.addService(request);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Service created: %s", resp.getServiceName()), resp)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicesResponse>>> getAll(){
        log.info("REST received to list all services");

        List<ServicesResponse> resp = servicesService.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Received list of size %d",
                        resp.size()), resp)
        );
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServicesResponse>> getById(
            @PathVariable Long serviceId){

        log.info("REST received to list service with ID: {}",serviceId);

        ServicesResponse resp = servicesService.getById(serviceId);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Received the service with name: %s",
                        resp.getServiceName()) , resp)
        );
    }

    @GetMapping("public")
    public ResponseEntity<ApiResponse<List<ServicesResponsePublic>>> getAllPublic(){
        log.info("REST received to list all services");

        List<ServicesResponsePublic> resp = servicesService.getAllPublic();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Received list of size %d",
                        resp.size()), resp)
        );
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<?> deleteService(@PathVariable Long id){
        log.info("REST received to delete service with ID: {}", id);
        servicesService.deleteService(id);
        return ResponseEntity.ok(
                ApiResponse.success(String.format("Service with ID '{}' deleted successfully", id))
        );
    }

}
