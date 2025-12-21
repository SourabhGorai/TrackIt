package com.trackIt.user_service2.service;
import com.trackIt.user_service2.client.IndependentServiceClient;
import com.trackIt.user_service2.dto.CompanyResponse;
import com.trackIt.user_service2.dto.RoleResponse;
import com.trackIt.user_service2.dto.UserResponse;
import com.trackIt.user_service2.exception.UserNotFoundException;
import com.trackIt.user_service2.mapper.UserMapper;
import com.trackIt.user_service2.model.Users;
import com.trackIt.user_service2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final IndependentServiceClient independentServiceClient;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Fetch and set role name for authorization
        try {
            RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
            if (role != null) {
                user.setRoleName(role.getRole());
            }
        } catch (Exception e) {
            log.error("Failed to fetch role for user: {}", email, e);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        log.info("Fetching profile for user: {}", email);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
        CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());

        return UserMapper.toResponseWithDetails(
                user,
                role != null ? role.getRole() : null,
                company != null ? company.getCompanyName() : null
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
        CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());

        return UserMapper.toResponseWithDetails(
                user,
                role != null ? role.getRole() : null,
                company != null ? company.getCompanyName() : null
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll().stream()
                .filter(user -> !user.getIsDeleted())
                .map(user -> {
                    RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
                    CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());
                    return UserMapper.toResponseWithDetails(
                            user,
                            role != null ? role.getRole() : null,
                            company != null ? company.getCompanyName() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void lockAccount(Long userId) {
        log.info("Locking account for user ID: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsAccountLocked(true);
        userRepository.save(user);

        log.info("Account locked successfully for user ID: {}", userId);
    }

    @Transactional
    public void unlockAccount(Long userId) {
        log.info("Unlocking account for user ID: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsAccountLocked(false);
        userRepository.save(user);

        log.info("Account unlocked successfully for user ID: {}", userId);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        log.info("Soft deleting user ID: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsDeleted(true);
        userRepository.save(user);

        log.info("User soft deleted successfully with ID: {}", userId);
    }

    @Transactional
    public void restoreUser(Long userId) {
        log.info("Restoring user ID: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setIsDeleted(false);
        userRepository.save(user);

        log.info("User restored successfully with ID: {}", userId);
    }
}