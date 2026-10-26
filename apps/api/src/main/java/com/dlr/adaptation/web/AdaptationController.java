package com.dlr.adaptation.web;

import com.dlr.adaptation.application.AdaptationService;
import com.dlr.adaptation.application.CalibrationService;
import com.dlr.adaptation.application.RetentionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/adaptation")
public class AdaptationController {

    private final AdaptationService service;
    private final CalibrationService calibrationService;

    private final RetentionService retentionService;

    public AdaptationController(AdaptationService service, CalibrationService calibrationService,
                                RetentionService retentionService) {
        this.service = service;
        this.calibrationService = calibrationService;
        this.retentionService = retentionService;
    }

    @GetMapping("/calibration")
    public CalibrationService.Calibration calibration() {
        return calibrationService.calibration();
    }

    @GetMapping("/retention")
    public RetentionService.Retention retention() {
        return retentionService.retention();
    }

    @GetMapping("/recommendation")
    public AdaptationService.Recommendation recommendation() {
        return service.current();
    }

    @PostMapping("/recommendations/{id}/decision")
    public AdaptationService.Recommendation decide(@PathVariable UUID id,
                                                    @Valid @RequestBody DecisionRequest request) {
        return service.decide(id, request.decision());
    }

    @GetMapping("/insights")
    public AdaptationService.Insights insights() {
        return service.insights();
    }

    public record DecisionRequest(@NotNull AdaptationService.Decision decision) {}
}
