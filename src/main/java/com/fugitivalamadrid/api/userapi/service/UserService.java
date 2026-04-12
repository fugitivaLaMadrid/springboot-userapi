package com.fugitivalamadrid.api.userapi.service;

import com.fugitivalamadrid.api.userapi.dto.UserPartialRequest;
import com.fugitivalamadrid.api.userapi.dto.UserRequest;
import com.fugitivalamadrid.api.userapi.dto.UserResponse;
import com.fugitivalamadrid.api.userapi.exception.UserNotFoundException;
import com.fugitivalamadrid.api.userapi.mapper.UserMapper;
import com.fugitivalamadrid.api.userapi.model.User;
import com.fugitivalamadrid.api.userapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("username", "email", "createdAt");

    public UserService(UserRepository userRepository, AuditLogService auditLogService,  UserMapper userMapper) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.userMapper = userMapper;

    }

    /**
     * Returns a list of all users.
     * @return a list of all users
     */
    @Cacheable("users")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users from database");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Returns a user by id.
     * @param id the user id
     * @return the user
     */
    @Cacheable(value="users", key="#id")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException(id);
                });
        return userMapper.toResponse(user);
    }

    /**
     * Creates a new user.
     * @param request the user request
     * @return the created user
     */
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserRequest request) {
        User user = userMapper.toEntity(request);
        user.setCreatedAt(LocalDateTime.now());
        UserResponse response = userMapper.toResponse(userRepository.save(user));
        auditLogService.logCreated(request.getUsername());
        log.info("User created with id: {}", response.getId());
        return response;
    }

    /**
     * Deletes a user by id.
     * @param id the user id
     */
    @CacheEvict(value="users", allEntries=true)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            log.warn("Delete failed - user not found with id: {}", id);
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        auditLogService.logDeleted(id);
        log.info("User deleted with id: {}", id);
    }

    /**
     * Updates a user by id.
     * @param id the user id
     * @param request the user request
     */
    @CacheEvict(value = "users", allEntries = true)
    public void updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed - user not found with id: {}", id);
                    return new UserNotFoundException(id);
                });
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        userRepository.save(user);
        auditLogService.logUpdated(id, request.getUsername());
        log.info("User updated with id: {}", id);
    }

    /**
     * Partially updates a user by id.
     * @param id the user id
     * @param request the user partial request
     */
    @CacheEvict(value="users", allEntries=true)
    public void updateUserPartial(Long id, UserPartialRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Partial update failed - user not found with id: {}", id);
                    return new UserNotFoundException(id);
                });
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        userRepository.save(user);
        auditLogService.logPartialUpdated(id);

        log.info("User partially updated with id: {}", id);
    }

    /**
     * Searches users by username with optional sorting.
     * @param name the username to search for
     * @param sortBy field to sort by (username or email)
     * @param direction asc or desc
     * @return list of matching users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String name, String sortBy, String direction) {
        log.info("Searching users with name: {}, sortBy: {}, direction: {}", name, sortBy, direction);
        validateSortParameters(sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return userRepository.findByUsernameContainingIgnoreCase(name, sort)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    private void validateSortParameters(String sortBy, String direction) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value. Allowed values: username, email, createdAt");
        }
        String normalizedDirection = direction.toLowerCase(Locale.ROOT);
        if (!normalizedDirection.equals("asc") && !normalizedDirection.equals("desc")) {
            throw new IllegalArgumentException("Invalid direction value. Allowed values: asc, desc");
        }
    }
}
