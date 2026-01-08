package com.aaami.user.handler;

import com.aaami.cqrs.QueryHandler;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import com.aaami.user.mapper.UserMapper;
import com.aaami.user.query.GetAllUsersQuery;
import com.aaami.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllUsersQueryHandler implements QueryHandler<GetAllUsersQuery, List<UserDto>> {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public List<UserDto> handle(GetAllUsersQuery query) {
        Specification<com.aaami.user.domain.User> spec = buildSpecification(query);
        List<com.aaami.user.domain.User> users = userRepository.findAll(spec);
        return users.stream()
                .map(userMapper::toDto)
                .toList();
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

