package com.fugitivalamadrid.api.userapi.controller;

import com.fugitivalamadrid.api.userapi.dto.UserPartialRequest;
import com.fugitivalamadrid.api.userapi.dto.UserRequest;
import com.fugitivalamadrid.api.userapi.dto.UserResponse;
import com.fugitivalamadrid.api.userapi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.fugitivalamadrid.api.userapi.ratelimit.RateLimit;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns a list of all users.
     * @return a list of all users
     */
    @RateLimit(maxRequests = 10, windowSizeMillis = 60000)
    @GetMapping
    public List<UserResponse> getAllUsers() {
        log.info("GET /users - fetching all users");
        return userService.getAllUsers();
    }

    /**
     * Returns a user by id.
     * @param id the user id
     * @return the user
     */
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        log.info("GET /users/{} - fetching user by id", id);
        return userService.getUserById(id);
    }

    /**
     * Creates a new user.
     * @param request the user request
     * @return the created user
     */
    @RateLimit(maxRequests = 5, windowSizeMillis = 60000)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        log.info("POST /users - creating user with username: {}", request.getUsername());
        return userService.createUser(request);
    }

    /**
     * Deletes a user by id.
     * @param id the user id
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("DELETE /users/{} - deleting user by id", id);
        userService.deleteUser(id);
    }

    /**
     * Updates a user by id.
     * @param id the user id
     * @param request the user request
     */
    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        log.info("PUT /users/{} - updating user with username: {}", id, request.getUsername());
        userService.updateUser(id, request);
    }

    /**
     * Partially updates a user by id.
     * @param id the user id
     * @param request the user partial request
     */
    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserPartial(@PathVariable Long id, @Valid @RequestBody UserPartialRequest request) {
        log.info("PATCH /users/{} - partially updating user", id);
        userService.updateUserPartial(id, request);
    }

    /**
     * Searches users by username with optional sorting.
     * @param name the username to search for
     * @param sortBy field to sort by (default: username)
     * @param direction sort direction asc or desc (default: asc)
     * @return list of matching users
     */
    @GetMapping("/search")
    public List<UserResponse> searchUsers(
            @RequestParam String name,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return userService.searchUsers(name, sortBy, direction);
    }
}