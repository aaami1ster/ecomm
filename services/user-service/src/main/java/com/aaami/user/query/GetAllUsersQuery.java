package com.aaami.user.query;

import com.aaami.cqrs.Query;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllUsersQuery implements Query<PaginatedResponse<UserDto>> {
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection; // "asc" or "desc"
}

