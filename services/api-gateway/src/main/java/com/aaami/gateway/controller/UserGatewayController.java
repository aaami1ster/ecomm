package com.aaami.gateway.controller;

import com.aaami.gateway.client.UserServiceClient;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserGatewayController {
    
    private final UserServiceClient userServiceClient;
    
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserCommand command) {
        UserDto user = userServiceClient.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "role", required = false) UserRole role) {
        List<UserDto> users = userServiceClient.getAllUsers(firstName, lastName, email, role);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable("id") Long id) {
        UserDto user = userServiceClient.getUser(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
        UserDto user = userServiceClient.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserCommand command) {
        UserDto user = userServiceClient.updateUser(id, command);
        return ResponseEntity.ok(user);
    }
}

