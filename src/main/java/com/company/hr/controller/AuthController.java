package com.company.hr.controller;

import com.company.hr.dto.Dtos.LoginRequest;
import com.company.hr.dto.Dtos.LoginResponse;
import com.company.hr.dto.Dtos.UserResponse;
import com.company.hr.security.AppUserDetails;
import com.company.hr.security.JwtService;
import com.company.hr.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password()));
        AppUserDetails principal = (AppUserDetails) auth.getPrincipal();
        UserResponse user = userService.getById(principal.getId());
        return new LoginResponse(jwtService.generateToken(principal), "Bearer", user);
    }
}
