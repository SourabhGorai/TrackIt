package com.trackIt.user_service2.service;
import com.trackIt.user_service2.client.IndependentServiceClient;
import com.trackIt.user_service2.dto.request.ProviderManagerRequest;
import com.trackIt.user_service2.dto.response.*;
import com.trackIt.user_service2.exception.UserNotFoundException;
import com.trackIt.user_service2.mapper.UserMapper;
import com.trackIt.user_service2.model.ProviderManagers;
import com.trackIt.user_service2.model.Users;
import com.trackIt.user_service2.repository.ProviderManagerRepository;
import com.trackIt.user_service2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final IndependentServiceClient independentServiceClient;
    private final ProviderManagerRepository providerManagerRepository;

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

    public UserResponsePublic getUserByIdPublic(Long userId) {

        log.info("Fetching user by Id: {}, in public view", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
        CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());

        return UserMapper.toResponseWithPublicView(
                user,
                role != null ? role.getRole() : null,
                company != null ? company.getCompanyName() : null
        );

    }

    public UserResponsePublic getUserByEmployeeIdPublic(String employeeId) {

        log.info("Fetching user by employee Id: {}, in public view", employeeId);

        Users user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with employee ID: " + employeeId)
                );

        RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
        CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());

        return UserMapper.toResponseWithPublicView(
                user,
                role != null ? role.getRole() : null,
                company != null ? company.getCompanyName() : null
        );
    }

    public List<UserResponsePublic> getAllUsersByCompanyIdAuto(Long userId) {

        log.info("Fetching active users for company of userId: {}", userId);

        Users requester = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with ID: " + userId));

        Long companyId = requester.getCompanyId();

        List<Users> users = userRepository
                .findByCompanyIdAndIsDeletedFalseAndIsAccountLockedFalse(companyId);

        return users.stream()
                .map(user -> {
                    RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
                    CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());

                    return UserMapper.toResponseWithPublicView(
                            user,
                            role != null ? role.getRole() : null,
                            company != null ? company.getCompanyName() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponsePublic> getAllUsersByCompanyId(Long compId) {

        log.info("Fetching active users for company: {}", compId);

        CompanyResponse company = Optional
                .ofNullable(independentServiceClient.validateCompany(compId))
                .orElseThrow(() ->
                        new ServiceException("Invalid companyId: " + compId)
                );

        log.info("Fetching active users for company: {}", company.getCompanyName());

        List<Users> users = userRepository
                .findByCompanyIdAndIsDeletedFalseAndIsAccountLockedFalse(compId);

        if (users.isEmpty()) {
            return List.of();
        }

        // 🔥 Collect distinct roleIds
        List<Long> roleIds = users.stream()
                .map(Users::getRoleId)
                .distinct()
                .toList();

        // 🔥 ONE remote call instead of N
        List<RoleResponse> roles =
                independentServiceClient.validateRolesByIds(roleIds);

        Map<Long, RoleResponse> roleMap =
                roles.stream().collect(Collectors.toMap(
                        RoleResponse::getRoleId,
                        Function.identity()
                ));

        return users.stream()
                .map(user -> UserMapper.toResponseWithPublicView(
                        user,
                        roleMap.get(user.getRoleId()) != null
                                ? roleMap.get(user.getRoleId()).getRole()
                                : null,
                        company.getCompanyName()
                ))
                .toList();
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

    public String getName(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format("user with Id: %d not found", userId)));

        return user.getName();
    }

    @Transactional
    public ProviderManagerResponse updateShifts(Long userId,
                                                ProviderManagerRequest request) {

        log.info("Updating shift timings for provider manager userId: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with ID: " + userId));

        ProviderManagers providerManager = providerManagerRepository
                .findByUser_Id(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Provider Manager entry not found for user ID: " + userId));

        if (request.getShiftEnd().isBefore(request.getShiftStart())) {
            throw new IllegalArgumentException("Shift end time must be after shift start time");
        }

        providerManager.setShiftStart(request.getShiftStart());
        providerManager.setShiftEnd(request.getShiftEnd());

        ProviderManagers saved = providerManagerRepository.save(providerManager);

        return ProviderManagerResponse.builder()
                .id(saved.getId())
                .employeeId(user.getEmployeeId())
                .employeeName(user.getName())
                .shiftStart(saved.getShiftStart() != null
                        ? saved.getShiftStart().toString()
                        : null)
                .shiftEnd(saved.getShiftEnd() != null
                        ? saved.getShiftEnd().toString()
                        : null)
                .isActive(!user.getIsAccountLocked())
                .onCall(saved.getOnCall())
                .build();
    }


    @Transactional
    public Boolean updateOnCallStatus(Long userId) {

        log.info("Attempting to change On-Call status of Provider manager with Id: {}", userId);

        ProviderManagers pm = providerManagerRepository.findByUser_Id(userId).orElseThrow(
                () -> new UserNotFoundException("Provider Manager details not found")
        );

        try{

            if(pm.getOnCall()){
                pm.deactivateOnCall();
            }else{
                pm.activateOnCall();
            }

            ProviderManagers saved = providerManagerRepository.save(pm);
            return saved.getOnCall();

        }catch(Exception e){
            throw new RuntimeException();
        }

    }

    public ProviderManagerFullResponse getFullResponseForProviderManager(Long userId) {

        log.info("Attempting to fetch user (PROVIDER_MANAGER) full details with ID: {}", userId);

        Optional<Users> userOpt = userRepository.findById(userId);

        if(userOpt.isEmpty()){
            log.info("User does not exists");
            throw new UserNotFoundException(String.format("User does not present with the ID: %s",
                    userId.toString()));
        }

        ProviderManagers pm = providerManagerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with ID: %s is not an PROVIDER_MANAGER",userId)
                ));

        Users user = userOpt.orElseThrow();

        CompanyResponse company = Optional
                .ofNullable(independentServiceClient.validateCompany(user.getCompanyId()))
                .orElseThrow(() ->
                        new ServiceException(
                                "Invalid companyId: " + user.getCompanyId()
                        )
                );

        RoleResponse role = Optional
                .ofNullable(independentServiceClient.validateRole(user.getRoleId()))
                .orElseThrow(() ->
                        new ServiceException(
                                "Invalid roleID: " + user.getRoleId()
                        )
                );

        return UserMapper.toResponseFullPmInfo(user, company, role, pm);

    }

    public List<ProviderManagerFullResponse> getFullResponseForProviderManagerAll(Long compId) {

        CompanyResponse company = Optional
                .ofNullable(independentServiceClient.validateCompany(compId))
                .orElseThrow(() ->
                        new ServiceException("Invalid companyId: " + compId)
                );

        log.info(
                "Attempting to fetch all PROVIDER_MANAGER responses for company: {}",
                company.getCompanyName()
        );

        RoleResponse role = independentServiceClient.validateRoleByName("PROVIDER_MANAGER");

        List<Users> users = userRepository
                .findByCompanyIdAndRoleIdAndIsDeletedFalseAndIsAccountLockedFalse(
                        compId, role.getRoleId()
                );

        if (users.isEmpty()) {
            return List.of();
        }

        // 🔥 ONE DB call instead of N
        List<ProviderManagers> providerManagers =
                providerManagerRepository.findByUser_CompanyId(compId);

        Map<Long, ProviderManagers> pmByUserId =
                providerManagers.stream()
                        .collect(Collectors.toMap(
                                pm -> pm.getUser().getId(),
                                Function.identity()
                        ));

        return users.stream()
                .map(user -> {

                    ProviderManagers pm = pmByUserId.get(user.getId());

                    if (pm == null) {
                        throw new UserNotFoundException(
                                "User with ID " + user.getId() + " is not a PROVIDER_MANAGER"
                        );
                    }

                    return UserMapper.toResponseFullPmInfo(
                            user,
                            company,
                            role,
                            pm
                    );
                })
                .toList();
    }


}