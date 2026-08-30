package com.dlr.planning.web;

import com.dlr.planning.application.PlanningService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
public class PlanningController {

    private final PlanningService service;

    public PlanningController(PlanningService service) {
        this.service = service;
    }

    @GetMapping
    public PlanningService.CalendarView calendar(
            @RequestParam(defaultValue = "14") int days,
            @RequestParam(defaultValue = "JAVA") String path
    ) {
        return service.calendar(days, path);
    }

    @PostMapping("/{date}")
    public PlanningService.StudySession record(
            @PathVariable LocalDate date,
            @Valid @RequestBody PlanningService.SessionRecord request
    ) {
        return service.record(date, request);
    }
}
