package com.dlr.profile.web;

import com.dlr.profile.application.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ProfileService.Profile get() {
        return service.get();
    }

    @PutMapping
    public ProfileService.Profile update(@Valid @RequestBody ProfileService.Profile request) {
        return service.update(request);
    }
}
