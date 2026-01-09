package com.trackIt.independent_services.controller;

import com.trackIt.independent_services.dto.*;
import com.trackIt.independent_services.mapper.ServiceMapper;
import com.trackIt.independent_services.service.ServicesService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServicesController {

    private final ServicesService servicesService;

    @PostMapping
    public ResponseEntity<ApiResponse<ServicesResponse>> addService(@RequestBody ServicesRequest request) {

        request.setServiceName(ServiceMapper.sanitizeName(request.getServiceName()));
        log.info("REST received to add service with name: {}", request.getServiceName());

        ServicesResponse resp = servicesService.addService(request);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Service created: %s", resp.getServiceName()), resp)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicesResponse>>> getAll() {
        log.info("REST received to list all services");

        List<ServicesResponse> resp = servicesService.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Received list of size %d",
                        resp.size()), resp)
        );
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServicesResponse>> getById(
            @PathVariable Long serviceId) {

        log.info("REST received to list service with ID: {}", serviceId);

        ServicesResponse resp = servicesService.getById(serviceId);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Received the service with name: %s",
                        resp.getServiceName()), resp)
        );
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<ServicesResponsePublic>>> getAllPublic() {
        log.info("REST received to list all services");

        List<ServicesResponsePublic> resp = servicesService.getAllPublic();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Received list of size %d",
                        resp.size()), resp)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteService(@PathVariable Long id) {
        log.info("REST received to delete service with ID: {}", id);
        servicesService.deleteService(id);
        return ResponseEntity.ok(
                ApiResponse.success(String.format("Service with ID '%d' deleted successfully", id))
        );
    }

    @GetMapping("/clientCompany/{id}")
    public ResponseEntity<ApiResponse<List<ServicesResponse>>> getByClientCompany(
            @PathVariable Long id) {
        log.info("REST received to list all the services related by " +
                "Client Company with ID: {}", id);
        List<ServicesResponse> list = servicesService.getListWRTClient(id);
        return ResponseEntity.ok(
                ApiResponse.success(String.format("%d services fetched", list.size()), list)
        );
    }

    @GetMapping("/providerCompany/{id}")
    public ResponseEntity<ApiResponse<List<ServicesResponse>>> getByProviderCompany(
            @PathVariable Long id) {
        log.info("REST received to list all the services provider Company with ID: {}", id);
        List<ServicesResponse> list = servicesService.getListWRTProvider(id);
        return ResponseEntity.ok(
                ApiResponse.success(String.format("%d services fetched", list.size()), list)
        );
    }

    @GetMapping("/serviceName/{name}")
    public ResponseEntity<ApiResponse<List<ServicesResponse>>> getByServiceName(@PathVariable String name) {
        String sanitizeName = ServiceMapper.sanitizeName(name);
        log.info("REST received to list all the services with name: {}", sanitizeName);
        List<ServicesResponse> list = servicesService.getAllByName(sanitizeName);
        return ResponseEntity.ok(
                ApiResponse.success(String.format("%d services fetched", list.size()), list)
        );
    }

    @GetMapping("/validate/{id}")
    public ResponseEntity<Boolean> validateService(@PathVariable Long id) {
        log.info("REST received to validate service with id: {}", id);
        boolean validation = servicesService.validateService(id);
        return ResponseEntity.ok(validation);
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServicesResponse>> updateService(
            @PathVariable Long serviceId,
            @RequestBody ServicesRequest request
    ) {
        request.setServiceName(ServiceMapper.sanitizeName(request.getServiceName()));

        log.info("REST received to update service with ID: {}", serviceId);

        ServicesResponse resp = servicesService.updateService(serviceId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Service updated successfully", resp)
        );
    }

    @GetMapping("/serviceList/{compId}")
    public ResponseEntity<ApiResponse<List<Long>>> getServiceIdList (@PathVariable Long compId) {

        log.info("REST received to get serviceId list with company ID: {}", compId);

        List<Long> list = servicesService.getAllServiceIdForCompany(compId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched list with size: %d ", list.size()),
                list
        ));

    }

}
