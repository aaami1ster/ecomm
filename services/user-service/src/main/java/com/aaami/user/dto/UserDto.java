package com.aaami.user.dto;

import com.aaami.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

