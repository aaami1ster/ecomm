package com.aaami.user.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.UserDto;
import com.aaami.user.exception.UserNotFoundException;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.query.VerifyPasswordQuery;
import com.aaami.user.repository.UserRepository;
import com.aaami.user.service.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Query handler for verifying user password.
 * Returns UserDto if password is correct, throws exception if invalid.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyPasswordQueryHandler implements QueryHandler<VerifyPasswordQuery, UserDto> {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDto handle(VerifyPasswordQuery query) {
        // Find user by email
        var user = userRepository.findByEmailAndDeletedAtIsNull(query.getEmail())
                .orElseThrow(() -> {
                    log.warn("Password verification failed: User not found with email: {}", query.getEmail());
                    return new UserNotFoundException("Invalid email or password");
                });
        
        // Verify password
        if (!passwordEncoder.matches(query.getPassword(), user.getPassword())) {
            log.warn("Password verification failed for user: {}", query.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        log.debug("Password verification successful for user: {}", query.getEmail());
        return userMapper.toDto(user);
    }
}

