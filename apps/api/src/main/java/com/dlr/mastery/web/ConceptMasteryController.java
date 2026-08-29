package com.dlr.mastery.web;

import com.dlr.mastery.application.ConceptMasteryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mastery/concepts")
public class ConceptMasteryController {

    private final ConceptMasteryService service;

    public ConceptMasteryController(ConceptMasteryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConceptMasteryService.ConceptMastery> list() {
        return service.list();
    }
}
