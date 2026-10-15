package com.company.hr.controller;

import com.company.hr.dto.Dtos.*;
import com.company.hr.security.AppUserDetails;
import com.company.hr.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppUserDetails me) {
        return userService.getById(me.getId());
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyPassword(@AuthenticationPrincipal AppUserDetails me,
                                 @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(me.getId(), req);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest req) {
        return userService.create(req);
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public List<UserResponse> list() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public UserResponse get(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public UserResponse update(@AuthenticationPrincipal AppUserDetails me, @PathVariable Long id,
                               @Valid @RequestBody UserUpdateRequest req) {
        return userService.update(id, req, me.getId());
    }
}
