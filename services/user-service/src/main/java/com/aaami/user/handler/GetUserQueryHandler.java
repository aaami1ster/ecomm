package com.aaami.user.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.UserDto;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.query.GetUserQuery;
import com.aaami.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserQueryHandler implements QueryHandler<GetUserQuery, UserDto> {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public UserDto handle(GetUserQuery query) {
        return userRepository.findById(query.getId())
                .map(userMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + query.getId()));
    }
}

