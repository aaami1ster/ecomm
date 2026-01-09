package com.aaami.user.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.CreateUserCommand;
import com.aaami.user.domain.User;
import com.aaami.shared.dto.UserDto;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.exception.DuplicateEmailException;
import com.aaami.user.repository.UserRepository;
import com.aaami.user.service.PasswordEncoder;
import com.aaami.user.service.UserEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateUserCommandHandler implements CommandHandler<CreateUserCommand, UserDto> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer eventProducer;
    
    @Override
    @Transactional
    public UserDto handle(CreateUserCommand command) {
        log.debug("Received create request for {}", command.getFirstName());
        if (userRepository.existsByEmailAndDeletedAtIsNull(command.getEmail())) {
            throw new DuplicateEmailException("User with email " + command.getEmail() + " already exists");
        }
        
        User user = User.builder()
                .email(command.getEmail())
                .password(passwordEncoder.encode(command.getPassword()))
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .role(command.getRole() != null ? command.getRole() : com.aaami.shared.dto.UserRole.USER)
                .build();
        
        User savedUser = userRepository.save(user);
        UserDto userDto = userMapper.toDto(savedUser);
        
        // Publish event after successful save
        eventProducer.publishUserCreated(userDto);
        
        return userDto;
    }
}

