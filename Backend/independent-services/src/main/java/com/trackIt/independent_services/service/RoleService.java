package com.trackIt.independent_services.service;

import com.trackIt.independent_services.dto.RolesResponse;
import com.trackIt.independent_services.exception.AlreadyExistsException;
import com.trackIt.independent_services.exception.ServiceException;
import com.trackIt.independent_services.mapper.RoleMapper;
import com.trackIt.independent_services.model.Roles;
import com.trackIt.independent_services.repository.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    @CacheEvict(value = "roles", allEntries = true)
    @Transactional
    public Roles addRole(String role){

        log.info("Creating role: {}", role);

        String sanitizedRole = RoleMapper.sanitizeRole(role);

        if(roleRepository.existsByRole(sanitizedRole)){
            log.warn("Attempt to create role that already exists: {}", sanitizedRole);
            throw new AlreadyExistsException("Role",sanitizedRole);
        }

        Roles roles = Roles.builder()
                .role(sanitizedRole)
                .createdAt(LocalDateTime.now())
                .build();

        try{
            Roles savedRole = roleRepository.save(roles);
            log.info("Successfully created role: {}", savedRole.getRole());
            return savedRole;
        }catch(Exception e){
            log.error("Failed to create role: {}", sanitizedRole, e);
            throw new ServiceException("Failed to create role: " + sanitizedRole, e);
        }

    }

    @Transactional(readOnly = true)
    public List<RolesResponse> getList(List<Long> ids) {

        log.info("Attempting to create role list for ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Roles> roles = roleRepository.findAllById(ids);

        if (roles.isEmpty()) {
            return List.of();
        }

        return RoleMapper.toResponseList(roles);
    }

    @Cacheable(value = "roles")
    @Transactional(readOnly = true)
    public List<RolesResponse> getAll() {
        log.info("Attempt to fetch all roles");
        List<Roles> roles = roleRepository.findAll();
        log.info("Found {} roles", roles.size());
        return RoleMapper.toResponseList(roles);
    }

    @CacheEvict(value = "roles", allEntries = true)
    @Transactional
    public void deleteRole(String role) {

        String sanitizedName = RoleMapper.sanitizeRole(role);
        log.info("Attempt to delete role: {}", sanitizedName);

        try{
            roleRepository.deleteByRole(sanitizedName);
            log.info("Deleted role: {}", sanitizedName);
        }catch(Exception e){
            log.info("Failed to delete role: {}", sanitizedName);
            throw new ServiceException("Failed to delete role: "+sanitizedName, e);
        }
    }

    public RolesResponse validateRole(Long roleId) {
        Roles role = roleRepository.findById(roleId).orElseThrow();
        return RoleMapper.toResponse(role);
    }

    public RolesResponse getRoleId(String name) {
        log.info("Attempting to get the role Id");
        String sanitizedName = RoleMapper.sanitizeRole(name);

        try {

            Roles role = roleRepository.findByRole(sanitizedName)
                    .orElseThrow(() ->
                            new ServiceException("Role not found: " + sanitizedName)
                    );

            return RoleMapper.toResponse(role);

        } catch(Exception e){
            log.info("Failed to validate role: {}", sanitizedName);
            throw new ServiceException("Failed to delete role: "+sanitizedName, e);
        }
    }
}
