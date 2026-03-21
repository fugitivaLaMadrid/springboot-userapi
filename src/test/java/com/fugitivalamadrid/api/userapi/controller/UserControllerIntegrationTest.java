package com.fugitivalamadrid.api.userapi.controller;

import com.fugitivalamadrid.api.userapi.dto.UserRequest;
import com.fugitivalamadrid.api.userapi.model.User;
import com.fugitivalamadrid.api.userapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
    void getAllUsers_returnsEmptyList_whenNoUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
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
    void getUserById_returnsUser_whenExists() throws Exception {
        User saved = createUserInDb("alice", "alice@example.com");

        mockMvc.perform(get("/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    void getUserById_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    // ── POST /users ───────────────────────────────────────────────────────────

    @Test
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
    void createUser_returns400_whenUsernameBlank() throws Exception {
        UserRequest request = new UserRequest("", "alice@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returns400_whenEmailInvalid() throws Exception {
        UserRequest request = new UserRequest("alice", "not-an-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /users/{id} ────────────────────────────────────────────────────

    @Test
    void deleteUser_returns204_whenExists() throws Exception {
        User saved = createUserInDb("alice", "alice@example.com");

        mockMvc.perform(delete("/users/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
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
}
