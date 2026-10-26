package com.dlr.assessment.web;

import com.dlr.assessment.application.PretestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labs/{labCode}/pretest")
public class PretestController {

    private final PretestService service;

    public PretestController(PretestService service) {
        this.service = service;
    }

    @GetMapping
    public PretestService.Pretest pretest(@PathVariable String labCode) {
        return service.get(labCode);
    }

    @PostMapping
    public PretestService.Pretest submit(@PathVariable String labCode, @Valid @RequestBody PretestRequest request) {
        return service.submit(labCode, request.answers());
    }

    @PostMapping("/skip")
    public PretestService.Pretest skip(@PathVariable String labCode) {
        return service.skip(labCode);
    }

    public record PretestRequest(@NotEmpty List<PretestService.Answer> answers) {
    }
}
