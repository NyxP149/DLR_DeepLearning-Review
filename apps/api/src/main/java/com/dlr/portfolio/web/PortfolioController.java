package com.dlr.portfolio.web;

import com.dlr.portfolio.application.PortfolioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService service;
    public PortfolioController(PortfolioService service) { this.service = service; }

    @GetMapping("/projects") public List<PortfolioService.Project> list() { return service.list(); }

    @GetMapping("/draft")
    public ResponseEntity<PortfolioService.Draft> draft() {
        return service.draft().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/draft")
    public PortfolioService.Draft saveDraft(@Valid @RequestBody DraftRequest request) {
        return service.saveDraft(request.title(), request.summary(), request.labCodes(), request.decisions());
    }

    @PostMapping("/projects")
    public PortfolioService.Project create(@Valid @RequestBody ProjectRequest request) {
        return service.create(request.title(), request.summary(), request.labCodes(), request.decisions());
    }

    @GetMapping(value = "/projects/{id}/readme", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public String readme(@PathVariable UUID id) { return service.markdown(id); }

    @GetMapping(value = "/projects/{id}/export", produces = "application/zip")
    public ResponseEntity<byte[]> export(@PathVariable UUID id) {
        PortfolioService.Project project = service.find(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + project.slug() + ".zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(service.zip(id));
    }

    public record DraftRequest(@NotNull @Size(max = 160) String title,
                               @NotNull @Size(max = 1200) String summary,
                               @NotNull @Size(max = 200) List<@NotBlank @Pattern(regexp = "[A-Z_]+-\\d{2}") String> labCodes,
                               @NotNull @Size(max = 4000) String decisions) {}

    public record ProjectRequest(@NotBlank @Size(max = 160) String title,
                                 @NotBlank @Size(max = 1200) String summary,
                                 @NotEmpty @Size(max = 12) List<@NotBlank String> labCodes,
                                 @Size(max = 12) List<@NotBlank @Size(max = 1000) String> decisions) {
        public ProjectRequest { decisions = decisions == null ? List.of() : decisions; }
    }
}
