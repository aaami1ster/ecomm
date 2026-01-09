package com.aaami.user.integration;

import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";
        restTemplate = new RestTemplate();
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        // Given
        CreateUserCommand command = new CreateUserCommand();
        command.setEmail("integration@test.com");
        command.setPassword("Password123");
        command.setFirstName("Integration");
        command.setLastName("Test");
        command.setRole(UserRole.USER);

        // When
        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                baseUrl, command, UserDto.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("integration@test.com", response.getBody().getEmail());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void getUser_ShouldReturnUser_WhenUserExists() {
        // Given - Create a user first
        CreateUserCommand createCommand = new CreateUserCommand();
        createCommand.setEmail("getuser@test.com");
        createCommand.setPassword("Password123");
        createCommand.setFirstName("Get");
        createCommand.setLastName("User");

        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(
                baseUrl, createCommand, UserDto.class);
        Long userId = createResponse.getBody().getId();

        // When
        ResponseEntity<UserDto> response = restTemplate.getForEntity(
                baseUrl + "/" + userId, UserDto.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("getuser@test.com", response.getBody().getEmail());
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedResponse() {
        // Given - Create a user first
        CreateUserCommand command = new CreateUserCommand();
        command.setEmail("list@test.com");
        command.setPassword("Password123");
        command.setFirstName("List");
        command.setLastName("User");
        restTemplate.postForEntity(baseUrl, command, UserDto.class);

        // When
        ResponseEntity<PaginatedResponse> response = restTemplate.getForEntity(
                baseUrl + "?page=0&size=10", PaginatedResponse.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTotalElements() > 0);
    }
}

