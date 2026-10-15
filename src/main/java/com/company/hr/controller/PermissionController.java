package com.company.hr.controller;

import com.company.hr.dto.Dtos.PermissionDecisionRequest;
import com.company.hr.dto.Dtos.PermissionResponse;
import com.company.hr.dto.Dtos.PermissionSubmitRequest;
import com.company.hr.enums.PermissionStatus;
import com.company.hr.security.AppUserDetails;
import com.company.hr.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse submit(@AuthenticationPrincipal AppUserDetails me,
                                     @Valid @RequestBody PermissionSubmitRequest req) {
        return permissionService.submit(me.getId(), req);
    }

    @GetMapping("/me")
    public List<PermissionResponse> mine(@AuthenticationPrincipal AppUserDetails me) {
        return permissionService.findMine(me.getId());
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public List<PermissionResponse> all(@RequestParam(required = false) PermissionStatus status) {
        return permissionService.findAll(status);
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('HR')")
    public PermissionResponse decide(@PathVariable Long id,
                                     @Valid @RequestBody PermissionDecisionRequest req) {
        return permissionService.decide(id, req);
    }
}
