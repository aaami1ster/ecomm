package com.aaami.shared.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * User role enumeration.
 * Valid values: USER, PREMIUM_USER, ADMIN
 */
@JsonDeserialize(using = UserRoleDeserializer.class)
public enum UserRole {
    USER, 
    PREMIUM_USER, 
    ADMIN
}

