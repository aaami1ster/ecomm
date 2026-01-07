package com.aaami.user.query;

import com.aaami.cqrs.Query;
import com.aaami.user.dto.UserDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserQuery implements Query<UserDto> {
    @NotNull(message = "User ID is required")
    private Long id;
}

