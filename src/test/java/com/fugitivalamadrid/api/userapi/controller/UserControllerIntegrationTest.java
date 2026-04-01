package com.fugitivalamadrid.api.userapi.controller;

import com.fugitivalamadrid.api.userapi.dto.UserRequest;
import com.fugitivalamadrid.api.userapi.model.User;
import com.fugitivalamadrid.api.userapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        userRepository.deleteAll();
    }

    // ── GET /users ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /users returns empty list when no users exist")
    void getAllUsers_returnsEmptyList_whenNoUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /users returns list when users exist")
    void getAllUsers_returnsList_whenUsersExist() throws Exception {
        createUserInDb("alice", "alice@example.com");
        createUserInDb("bob", "bob@example.com");

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("alice")))
                .andExpect(jsonPath("$[1].username", is("bob")));
    }

    // ── GET /users/{id} ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /users/{id} returns user when exists")
    void getUserById_returnsUser_whenExists() throws Exception {
        User saved = createUserInDb("alice", "alice@example.com");

        mockMvc.perform(get("/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    @DisplayName("GET /users/{id} returns 404 when user not found")
    void getUserById_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    // ── POST /users ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /users creates user and returns created user")
    void createUser_returnsCreatedUser() throws Exception {
        UserRequest request = new UserRequest("alice", "alice@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /users returns 400 when username is blank")
    void createUser_returns400_whenUsernameBlank() throws Exception {
        UserRequest request = new UserRequest("", "alice@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users returns 400 when email is invalid")
    void createUser_returns400_whenEmailInvalid() throws Exception {
        UserRequest request = new UserRequest("alice", "not-an-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /users/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /users/{id} returns 204 when user exists and is deleted")
    void deleteUser_returns204_whenExists() throws Exception {
        User saved = createUserInDb("alice", "alice@example.com");

        mockMvc.perform(delete("/users/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /users/{id} returns 404 when user not found")
    void deleteUser_returns404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User createUserInDb(String username, String email) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // -- update ------------
    @Test
    @DisplayName("PUT /users/{id} returns 204 when user is successfully updated")
    void updateUser_returns204_whenSuccessful() throws Exception {
        User saved = createUserInDb("alice", "alice@example.com");
        UserRequest request = new UserRequest("alice-updated", "alice2@gmail.com");

        mockMvc.perform(put("/users/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // Verify the user was actually updated
        mockMvc.perform(get("/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("alice-updated")))
                .andExpect(jsonPath("$.email", is("alice2@gmail.com")));
    }

    @Test
    @DisplayName("PATCH /users/{id} returns 204 when user is successfully updated")
    void updateUserPartial_returns204_whenSuccessful() throws Exception {
        User saved = createUserInDb("alice", "alice@example.com");
        UserRequest request = new UserRequest("alice", "alice-update@example.com");

        mockMvc.perform(patch("/users/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // Verify the user was actually updated
        mockMvc.perform(get("/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("alice-update@example.com")));
    }

    // ── GET /users/search ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /users/search returns matching users when name matches")
    void searchUsers_returnsMatchingUsers() throws Exception {
        createUserInDb("alice", "alice@example.com");
        createUserInDb("bob", "bob@example.com");

        mockMvc.perform(get("/users/search")
                        .param("name", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("alice")));
    }

    @Test
    @DisplayName("GET /users/search returns empty list when no name matches")
    void searchUsers_returnsEmpty_whenNoMatch() throws Exception {
        createUserInDb("alice", "alice@example.com");

        mockMvc.perform(get("/users/search")
                        .param("name", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /users/search returns sorted results when sortBy and direction provided")
    void searchUsers_returnsSortedResults() throws Exception {
        createUserInDb("alice", "alice@example.com");
        createUserInDb("anna", "anna@example.com");

        mockMvc.perform(get("/users/search")
                        .param("name", "a")
                        .param("sortBy", "username")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("alice")))
                .andExpect(jsonPath("$[1].username", is("anna")));
    }
}