package com.aaami.product.repository;

import com.aaami.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    Optional<Product> findByIdAndDeletedAtIsNull(Long id);
    
    List<Product> findByDeletedAtIsNull();
    
    boolean existsByNameAndDeletedAtIsNull(String name);
    
    Optional<Product> findByNameAndDeletedAtIsNull(String name);
}

