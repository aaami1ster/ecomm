package com.aaami.user.repository;

import com.aaami.shared.dto.UserRole;
import com.aaami.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    
    // Legacy methods for backward compatibility (will be filtered in handlers)
    @Deprecated
    Optional<User> findByEmail(String email);
    @Deprecated
    boolean existsByEmail(String email);
}

