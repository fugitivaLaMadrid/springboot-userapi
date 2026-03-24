package com.fugitivalamadrid.api.userapi.controller;

import com.fugitivalamadrid.api.userapi.dto.UserRequest;
import com.fugitivalamadrid.api.userapi.dto.UserResponse;
import com.fugitivalamadrid.api.userapi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
}