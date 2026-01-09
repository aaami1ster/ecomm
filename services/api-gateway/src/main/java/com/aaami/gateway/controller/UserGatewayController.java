package com.aaami.gateway.controller;

import com.aaami.gateway.client.UserServiceClient;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints. Registration is open to all. Other operations require authentication with appropriate roles.")
public class UserGatewayController {
    
    private final UserServiceClient userServiceClient;
    
    @Operation(
            summary = "Create a new user",
            description = "Registers a new user. Available to all authenticated users (USER, PREMIUM_USER, ADMIN)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with this email already exists",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserCommand command) {
        UserDto user = userServiceClient.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @Operation(
            summary = "Get all users",
            description = "Retrieves a paginated list of users with optional filtering. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<UserDto>> getAllUsers(
            @Parameter(description = "Filter by first name", example = "John")
            @RequestParam(value = "firstName", required = false) String firstName,
            @Parameter(description = "Filter by last name", example = "Doe")
            @RequestParam(value = "lastName", required = false) String lastName,
            @Parameter(description = "Filter by email", example = "user@example.com")
            @RequestParam(value = "email", required = false) String email,
            @Parameter(description = "Filter by role", example = "USER", schema = @Schema(implementation = UserRole.class))
            @RequestParam(value = "role", required = false) UserRole role,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Sort field (e.g., firstName, email, createdAt)", example = "firstName")
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection) {
        PaginatedResponse<UserDto> response = userServiceClient.getAllUsers(firstName, lastName, email, role, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user by their ID. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        UserDto user = userServiceClient.getUser(id);
        return ResponseEntity.ok(user);
    }
    
    @Operation(
            summary = "Get user by email",
            description = "Retrieves a user by their email address. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            )
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(
            @Parameter(description = "User email address", example = "user@example.com", required = true)
            @PathVariable("email") String email) {
        UserDto user = userServiceClient.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @Operation(
            summary = "Update a user",
            description = "Updates an existing user. Available to all authenticated users (USER, PREMIUM_USER, ADMIN).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @RequestBody UpdateUserCommand command) {
        command.setId(id);
        UserDto user = userServiceClient.updateUser(id, command);
        return ResponseEntity.ok(user);
    }
    
    @Operation(
            summary = "Delete a user",
            description = "Deletes a user by ID. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        userServiceClient.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
