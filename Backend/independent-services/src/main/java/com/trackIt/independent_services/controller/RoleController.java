package com.trackIt.independent_services.controller;

import com.trackIt.independent_services.dto.ApiResponse;
import com.trackIt.independent_services.dto.RolesResponse;
import com.trackIt.independent_services.model.Roles;
import com.trackIt.independent_services.service.RoleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/{role}")
    public ResponseEntity<ApiResponse<Roles>> addRoles(@PathVariable String role){
        log.info("REST request received to create role: {}", role);

        Roles response = roleService.addRole(role);

        return ResponseEntity.ok(
                ApiResponse.success("New role created successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RolesResponse>>> getAll(){
        log.info("REST request received to get all the roles");
        List<RolesResponse> list = roleService.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("Retrieved %d roles", list.size()), list
                )
        );
    }

    @GetMapping("/validate/{roleId}")
    public ResponseEntity<?> validateRole(@PathVariable Long roleId){
        log.info("REST request received to get all the roles");
        RolesResponse resp = roleService.validateRole(roleId);
        System.out.println(resp);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{role}")
    public ResponseEntity<ApiResponse<?>> deleteRole(@PathVariable String role){
        log.info("REST received to delete role: {}", role);
        roleService.deleteRole(role);
        log.info("Deleted role: {}", role);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Deleted role: %s", role))
        );

    }



}
