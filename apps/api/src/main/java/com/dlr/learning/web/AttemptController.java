package com.dlr.learning.web;

import com.dlr.learning.application.AttemptService;
import com.dlr.learning.domain.Attempt;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/labs/{labCode}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public Attempt start(@PathVariable String labCode) {
        return attemptService.start(labCode);
    }

    @GetMapping("/attempts/{id}")
    public Attempt get(@PathVariable UUID id) {
        return attemptService.get(id);
    }

    @PostMapping("/attempts/{id}/continue")
    public Attempt continueBelowThreshold(@PathVariable UUID id) {
        return attemptService.continueBelowThreshold(id);
    }
}
