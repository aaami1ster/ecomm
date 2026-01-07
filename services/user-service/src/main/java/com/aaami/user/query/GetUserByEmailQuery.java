package com.aaami.user.query;

import com.aaami.cqrs.Query;
import com.aaami.user.dto.UserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserByEmailQuery implements Query<UserDto> {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}

