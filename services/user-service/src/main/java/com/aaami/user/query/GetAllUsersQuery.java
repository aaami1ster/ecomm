package com.aaami.user.query;

import com.aaami.cqrs.Query;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllUsersQuery implements Query<List<UserDto>> {
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}

