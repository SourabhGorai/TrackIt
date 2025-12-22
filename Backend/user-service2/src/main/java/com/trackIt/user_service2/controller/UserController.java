package com.trackIt.user_service2.controller;

import com.trackIt.user_service2.dto.ApiResponse;
import com.trackIt.user_service2.dto.UserResponse;
import com.trackIt.user_service2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

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
}
