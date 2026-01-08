package com.aaami.order.repository;

import com.aaami.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByIdAndDeletedAtIsNull(Long id);
    List<Order> findByUserIdAndDeletedAtIsNull(Long userId);
    
    // Legacy method for backward compatibility (will be filtered in handlers)
    @Deprecated
    List<Order> findByUserId(Long userId);
}

