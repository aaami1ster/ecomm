package com.aaami.user.controller;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.shared.command.DeleteUserCommand;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.query.GetAllUsersQuery;
import com.aaami.user.query.GetUserByEmailQuery;
import com.aaami.user.query.GetUserQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserCommand command) {
        UserDto user = commandBus.dispatch(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @GetMapping
    public ResponseEntity<PaginatedResponse<UserDto>> getAllUsers(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "role", required = false) UserRole role,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection) {
        GetAllUsersQuery query = GetAllUsersQuery.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(role)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        PaginatedResponse<UserDto> response = queryBus.dispatch(query);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable("id") Long id) {
        GetUserQuery query = new GetUserQuery(id);
        UserDto user = queryBus.dispatch(query);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
        GetUserByEmailQuery query = new GetUserByEmailQuery(email);
        UserDto user = queryBus.dispatch(query);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserCommand command) {
        command.setId(id);
        UserDto user = commandBus.dispatch(command);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        DeleteUserCommand command = new DeleteUserCommand(id);
        commandBus.dispatch(command);
        return ResponseEntity.noContent().build();
    }
}

