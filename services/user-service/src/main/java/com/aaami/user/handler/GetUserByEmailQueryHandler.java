package com.aaami.user.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.UserDto;
import com.aaami.user.exception.UserNotFoundException;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.query.GetUserByEmailQuery;
import com.aaami.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserByEmailQueryHandler implements QueryHandler<GetUserByEmailQuery, UserDto> {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public UserDto handle(GetUserByEmailQuery query) {
        return userRepository.findByEmailAndDeletedAtIsNull(query.getEmail())
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + query.getEmail()));
    }
}

