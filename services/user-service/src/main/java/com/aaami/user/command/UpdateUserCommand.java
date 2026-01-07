package com.aaami.user.command;

import com.aaami.cqrs.Command;
import com.aaami.user.dto.UserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand implements Command<UserDto> {
    @NotNull(message = "User ID is required")
    private Long id;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String password;
    
    private String firstName;
    
    private String lastName;
    
    private com.aaami.user.domain.User.UserRole role;
}

