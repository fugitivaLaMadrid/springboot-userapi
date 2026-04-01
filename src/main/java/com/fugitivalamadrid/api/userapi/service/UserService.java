package com.fugitivalamadrid.api.userapi.service;

import com.fugitivalamadrid.api.userapi.dto.UserPartialRequest;
import com.fugitivalamadrid.api.userapi.dto.UserRequest;
import com.fugitivalamadrid.api.userapi.dto.UserResponse;
import com.fugitivalamadrid.api.userapi.exception.UserNotFoundException;
import com.fugitivalamadrid.api.userapi.model.User;
import com.fugitivalamadrid.api.userapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a list of all users.
     * @return a list of all users
     */
    @Cacheable("users")
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users from database");
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns a user by id.
     * @param id the user id
     * @return the user
     */
    @Cacheable(value="users", key="#id")
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException(id);
                });
        return toResponse(user);
    }

    /**
     * Creates a new user.
     * @param request the user request
     * @return the created user
     */
    @CacheEvict(value="users", allEntries=true)
    public UserResponse createUser(UserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(userRepository.save(user));
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
        log.info("User deleted with id: {}", id);
    }

    /**
     * Converts a User to a UserResponse.
     * @param user the user
     * @return the user response
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Updates a user by id.
     * @param id the user id
     * @param request the user request
     */
    @CacheEvict(value="users", allEntries=true)
    public void updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update user data failed - user not found with id: {}", id);
                    return new UserNotFoundException(id);
                });
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        userRepository.save(user);
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
                    log.warn("Update data failed - user not found with id: {}", id);
                    return new UserNotFoundException(id);
                });

        // Only update fields that are not null
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
        log.info("User partially updated with id: {}", id);
    }

    /**
     * Searches users by username with optional sorting.
     * @param name the username to search for
     * @param sortBy field to sort by (username or email)
     * @param direction asc or desc
     * @return list of matching users
     */
    public List<UserResponse> searchUsers(String name, String sortBy, String direction) {
        log.info("Searching users with name: {}, sortBy: {}, direction: {}", name, sortBy, direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return userRepository.findAll(sort)
                .stream()
                .filter(user -> user.getUsername().toLowerCase().contains(name.toLowerCase()))
                .map(this::toResponse)
                .toList();
    }
}