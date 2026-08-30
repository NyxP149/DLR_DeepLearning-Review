package com.dlr.portfolio.web;

import com.dlr.portfolio.application.PortfolioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio/projects")
public class PortfolioController {
    private final PortfolioService service;
    public PortfolioController(PortfolioService service) { this.service = service; }

    @GetMapping public List<PortfolioService.Project> list() { return service.list(); }

    @PostMapping
    public PortfolioService.Project create(@Valid @RequestBody ProjectRequest request) {
        return service.create(request.title(), request.summary(), request.labCodes(), request.decisions());
    }

    @GetMapping(value = "/{id}/readme", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public String readme(@PathVariable UUID id) { return service.markdown(id); }

    @GetMapping(value = "/{id}/export", produces = "application/zip")
    public ResponseEntity<byte[]> export(@PathVariable UUID id) {
        PortfolioService.Project project = service.find(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + project.slug() + ".zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(service.zip(id));
    }

    public record ProjectRequest(@NotBlank @Size(max = 160) String title,
                                 @NotBlank @Size(max = 1200) String summary,
                                 @NotEmpty @Size(max = 12) List<@NotBlank String> labCodes,
                                 @Size(max = 12) List<@NotBlank @Size(max = 1000) String> decisions) {
        public ProjectRequest { decisions = decisions == null ? List.of() : decisions; }
    }
}
