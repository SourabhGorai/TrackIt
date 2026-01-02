package com.trackIt.user_service2.controller;

import com.trackIt.user_service2.dto.*;
import com.trackIt.user_service2.dto.request.ProviderManagerRequest;
import com.trackIt.user_service2.dto.response.ProviderManagerFullResponse;
import com.trackIt.user_service2.dto.response.ProviderManagerResponse;
import com.trackIt.user_service2.dto.response.UserResponse;
import com.trackIt.user_service2.dto.response.UserResponsePublic;
import com.trackIt.user_service2.service.JwtService;
import com.trackIt.user_service2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Value("${jwt.secret}")
    private String secretKey;
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("REST request to get profile for user: {}", userDetails.getUsername());

        UserResponse response = userService.getUserProfile(userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", response)
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        log.info("REST request to get user by ID: {}", userId);

        UserResponse response = userService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", response)
        );
    }

    @GetMapping("/public/{userId}")
    public ResponseEntity<ApiResponse<UserResponsePublic>> getUserByIdPublic(
            @PathVariable Long userId) {
        log.info("REST request to get user in public view by ID: {}", userId);

        UserResponsePublic response = userService.getUserByIdPublic(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", response)
        );
    }

    @GetMapping("/public/employeeId/{employeeId}")
    public ResponseEntity<ApiResponse<UserResponsePublic>> getUserByEmployeeIdPublic(
            @PathVariable String employeeId) {
        log.info("REST request to get user in public view by Employee ID: {}", employeeId);

        UserResponsePublic response = userService.getUserByEmployeeIdPublic(employeeId);

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", response)
        );
    }

    @GetMapping("/public/company/auto")
    public ResponseEntity<ApiResponse<List<UserResponsePublic>>> getUserByCompanyIDAutomatically(
            @RequestHeader("Authorization") String authHeader
    ) {
        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

        log.info("REST received to fetch all the employees of the same company as the user");

        List<UserResponsePublic> response = userService.getAllUsersByCompanyIdAuto(userId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched list of size: %d", response.size()),
                response
        ));
    }

    @GetMapping("/public/company/{compId}")
    public ResponseEntity<ApiResponse<List<UserResponsePublic>>> getUserByCompanyID(
            @PathVariable Long compId
    ) {

        log.info("REST received to fetch all the employees of the same company for Id: {}", compId);

        List<UserResponsePublic> response = userService.getAllUsersByCompanyId(compId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched list of size: %d", response.size()),
                response
        ));

    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("REST request to get all users");

        System.out.println("Entering to userService.getAllUsers()");
        List<UserResponse> response = userService.getAllUsers();
        System.out.println("Return from userService.getAllUsers()");

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("Retrieved %d users", response.size()),
                        response
                )
        );
    }

    @PutMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> lockAccount(@PathVariable Long userId) {
        log.info("REST request to lock account for user ID: {}", userId);

        userService.lockAccount(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account locked successfully")
        );
    }

    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> unlockAccount(@PathVariable Long userId) {
        log.info("REST request to unlock account for user ID: {}", userId);

        userService.unlockAccount(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Account unlocked successfully")
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> softDeleteUser(@PathVariable Long userId) {
        log.info("REST request to soft delete user ID: {}", userId);

        userService.softDeleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully")
        );
    }

    @PutMapping("/{userId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> restoreUser(@PathVariable Long userId) {
        log.info("REST request to restore user ID: {}", userId);

        userService.restoreUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User restored successfully")
        );
    }

    @GetMapping("/getName/{userId}")
    public String getName(@PathVariable Long userId) {
        log.info("REST request to get name with user ID: {}", userId);
        return userService.getName(userId);
    }

    @PutMapping("/pm")
    public ResponseEntity<ApiResponse<ProviderManagerResponse>> updateShifts(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ProviderManagerRequest request
    ) {
        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

        log.info("REST request to update shifts - UserId: {}, Username: {}, Role: {}",
                userId, username, role);

        ProviderManagerResponse response = userService.updateShifts(userId, request);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Shifts updated successfully for user with ID: %s", userId),
                response
        ));
    }

    @PutMapping("/onCall")
    public ResponseEntity<ApiResponse<?>> changeOnCallStatus(
            @RequestHeader("Authorization") String authHeader
    ) {

        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);

        log.info("REST request to update On-Call status for user ID: {}", userId);

        Boolean status = userService.updateOnCallStatus(userId);

        String resp = status == true ? "Active" : "Non-Active";

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully changed On-Call status to: %s", resp)
        ));

    }

    // Need to create a method to fetch the complete details of all the PROVIDER_MANAGER.
    @GetMapping("/details/pmById/{userId}")
    public ResponseEntity<ApiResponse<ProviderManagerFullResponse>> getProviderManagerById(
            @PathVariable Long userId
    ){

        log.info("REST received to display the provider_manager contents");

        ProviderManagerFullResponse response = userService.getFullResponseForProviderManager(userId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully retrieved full info about the user: %s",
                        userId.toString()),
                response
        ));

    }

    @GetMapping("/details/pmByCompanyId/{compId}")
    public ResponseEntity<ApiResponse<List<ProviderManagerFullResponse>>> getAllFullPm(
            @PathVariable Long compId
    ){
        log.info("REST received to display all the provider_manager contents");

        List<ProviderManagerFullResponse> response = userService
                .getFullResponseForProviderManagerAll(compId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Retrieved list with size: %d",
                        response.size()),
                response
        ));
    }
    
}
