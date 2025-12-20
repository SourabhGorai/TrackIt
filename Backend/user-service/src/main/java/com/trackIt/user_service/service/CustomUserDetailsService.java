package com.trackIt.user_service.service;

import com.trackIt.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        log.debug("Loading user by employeeId: {}", employeeId);

        return userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with employeeId: " + employeeId
                ));
    }
}