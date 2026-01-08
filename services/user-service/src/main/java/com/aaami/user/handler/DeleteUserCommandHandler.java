package com.aaami.user.handler;

import com.aaami.cqrs.CommandHandler;
import com.aaami.shared.command.DeleteUserCommand;
import com.aaami.user.domain.User;
import com.aaami.user.exception.UserNotFoundException;
import com.aaami.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DeleteUserCommandHandler implements CommandHandler<DeleteUserCommand, Void> {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public Void handle(DeleteUserCommand command) {
        User user = userRepository.findByIdAndDeletedAtIsNull(command.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + command.getId()));
        
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        return null;
    }
}

