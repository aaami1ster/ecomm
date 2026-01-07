package com.aaami.user.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.user.command.CreateUserCommand;
import com.aaami.user.domain.User;
import com.aaami.user.dto.UserDto;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.repository.UserRepository;
import com.aaami.user.service.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateUserCommandHandler implements CommandHandler<CreateUserCommand, UserDto> {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public UserDto handle(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("User with email " + command.getEmail() + " already exists");
        }
        
        User user = User.builder()
                .email(command.getEmail())
                .password(passwordEncoder.encode(command.getPassword()))
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .role(command.getRole() != null ? command.getRole() : User.UserRole.USER)
                .build();
        
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}

