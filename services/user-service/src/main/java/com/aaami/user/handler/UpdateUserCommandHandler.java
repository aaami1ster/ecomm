package com.aaami.user.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.UpdateUserCommand;
import com.aaami.user.domain.User;
import com.aaami.shared.dto.UserDto;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.exception.DuplicateEmailException;
import com.aaami.user.exception.UserNotFoundException;
import com.aaami.user.repository.UserRepository;
import com.aaami.user.service.PasswordEncoder;
import com.aaami.user.service.UserEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateUserCommandHandler implements CommandHandler<UpdateUserCommand, UserDto> {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer eventProducer;
    
    @Override
    @Transactional
    public UserDto handle(UpdateUserCommand command) {
        User user = userRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + command.getId()));
        
        if (command.getEmail() != null && !command.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndDeletedAtIsNull(command.getEmail())) {
                throw new DuplicateEmailException("User with email " + command.getEmail() + " already exists");
            }
            user.setEmail(command.getEmail());
        }
        
        if (command.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(command.getPassword()));
        }
        
        if (command.getFirstName() != null) {
            user.setFirstName(command.getFirstName());
        }
        
        if (command.getLastName() != null) {
            user.setLastName(command.getLastName());
        }
        
        if (command.getRole() != null) {
            user.setRole(command.getRole());
        }
        
        User updatedUser = userRepository.save(user);
        UserDto userDto = userMapper.toDto(updatedUser);
        
        // Publish event after successful update
        eventProducer.publishUserUpdated(userDto);
        
        return userDto;
    }
}

