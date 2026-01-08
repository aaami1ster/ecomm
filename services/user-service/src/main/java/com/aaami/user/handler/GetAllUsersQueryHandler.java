package com.aaami.user.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.shared.dto.UserDto;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.query.GetAllUsersQuery;
import com.aaami.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllUsersQueryHandler implements QueryHandler<GetAllUsersQuery, PaginatedResponse<UserDto>> {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public PaginatedResponse<UserDto> handle(GetAllUsersQuery query) {
        Specification<com.aaami.user.domain.User> spec = buildSpecification(query);
        
        // Default pagination values
        int page = query.getPage() != null && query.getPage() >= 0 ? query.getPage() : 0;
        int size = query.getSize() != null && query.getSize() > 0 ? query.getSize() : 20;
        
        // Build sorting
        Sort sort = buildSort(query.getSortBy(), query.getSortDirection());
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<com.aaami.user.domain.User> userPage = userRepository.findAll(spec, pageable);
        
        return PaginatedResponse.<UserDto>builder()
                .content(userPage.getContent().stream()
                        .map(userMapper::toDto)
                        .toList())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }
    
    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "id"); // Default sort
        }
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        return Sort.by(direction, sortBy);
    }
    
    private Specification<com.aaami.user.domain.User> buildSpecification(GetAllUsersQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always exclude deleted users
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            if (query.getFirstName() != null && !query.getFirstName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + query.getFirstName().toLowerCase() + "%"
                ));
            }
            
            if (query.getLastName() != null && !query.getLastName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + query.getLastName().toLowerCase() + "%"
                ));
            }
            
            if (query.getEmail() != null && !query.getEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + query.getEmail().toLowerCase() + "%"
                ));
            }
            
            if (query.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), query.getRole()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

