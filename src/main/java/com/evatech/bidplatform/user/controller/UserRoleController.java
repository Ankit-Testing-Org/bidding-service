package com.evatech.bidplatform.user.controller;

import com.evatech.bidplatform.user.dto.RoleType;
import com.evatech.bidplatform.user.service.UserRoleService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping("/add/roles")
    public ResponseEntity<?> addRole(
            @NotNull @RequestParam String email,
            @NotNull @RequestParam RoleType role) {
        userRoleService.addRole(email, role);
        return ResponseEntity.ok("Role assigned");
    }

    @DeleteMapping("/delete/roles")
    public ResponseEntity<?> removeRole(
            @NotNull @RequestParam String email,
            @NotNull @RequestParam RoleType role
    ) {
        userRoleService.removeRole(email, role);
        return ResponseEntity.ok("Role removed");
    }

    @PutMapping("/update/roles")
    public ResponseEntity<?> replaceRoles(
            @NotNull @RequestParam String email,
            @NotNull @RequestBody Set<RoleType> roles
    ) {
        userRoleService.replaceRoles(email, roles);
        return ResponseEntity.ok("Roles updated");
    }

    @GetMapping("/get/roles")
    public ResponseEntity<?> getRoles(
            @NotNull @RequestParam String email
    ) {
        return ResponseEntity.ok(
                userRoleService.getRoles(email)
        );
    }
}
