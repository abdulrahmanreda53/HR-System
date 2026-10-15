package com.company.hr.controller;

import com.company.hr.dto.Dtos.SettingResponse;
import com.company.hr.dto.Dtos.SettingUpdateRequest;
import com.company.hr.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HR')")
public class SettingController {

    private final SystemSettingService settingService;

    @GetMapping
    public List<SettingResponse> list() {
        return settingService.findAll().stream().map(SettingResponse::from).toList();
    }

    @PutMapping("/{key}")
    public SettingResponse update(@PathVariable String key, @Valid @RequestBody SettingUpdateRequest req) {
        return SettingResponse.from(settingService.update(key, req.value()));
    }
}
