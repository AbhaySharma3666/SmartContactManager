package com.scm.helpers;

import com.scm.entities.User;
import com.scm.entities.UserRole;

public class RoleHelper {
    
    public static UserRole createRole(String roleName, User user) {
        return UserRole.builder()
                .role(roleName)
                .user(user)
                .build();
    }
}
