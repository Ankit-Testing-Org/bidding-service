package com.evatech.bidplatform.user.service;

import com.evatech.bidplatform.user.dto.RoleType;

import java.util.Set;

public interface UserRoleService {

    void addRole(
            String email,
            RoleType role
    );

    void removeRole(
            String email,
            RoleType role
    );

    void replaceRoles(
            String email,
            Set<RoleType> roles
    );

    Set<String> getRoles(
            String email
    );
}
