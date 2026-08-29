package com.dlr.sync.web;

import com.dlr.sync.application.SyncService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService service;

    public SyncController(SyncService service) {
        this.service = service;
    }

    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.CREATED)
    public SyncService.PairingResult pair(@Valid @RequestBody PairDeviceRequest request,
                                          @RequestHeader(value = "X-DLR-Pairing-Code", required = false) String pairingCode,
                                          HttpServletRequest servletRequest) {
        return service.pair(request.name(), pairingCode, servletRequest.getRemoteAddr());
    }

    @GetMapping("/devices")
    public List<SyncService.DeviceView> devices(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.devices(authorization);
    }

    @DeleteMapping("/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                       @PathVariable UUID deviceId) {
        service.revoke(authorization, deviceId);
    }

    @PostMapping("/changes")
    public SyncService.PushResult push(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                       @Valid @RequestBody PushRequest request) {
        return service.push(authorization, request.changes().stream()
                .map(change -> new SyncService.IncomingChange(
                        change.operationId(), change.entityType(), change.entityId(), change.entityVersion(),
                        change.payload(), change.clientModifiedAt()))
                .toList());
    }

    @GetMapping("/changes")
    public SyncService.ChangePage pull(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                       @RequestParam(defaultValue = "0") @Min(0) long after,
                                       @RequestParam(defaultValue = "100") @Min(1) @Max(200) int limit) {
        return service.pull(authorization, after, limit);
    }

    @GetMapping("/conflicts")
    public List<SyncService.ConflictView> conflicts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.conflicts(authorization);
    }

    public record PairDeviceRequest(@NotBlank @Size(max = 80) String name) {}
    public record PushRequest(@NotEmpty @Size(max = 50) List<@Valid ChangeRequest> changes) {}
    public record ChangeRequest(
            @NotNull UUID operationId,
            @NotBlank String entityType,
            @NotBlank @Size(max = 120) String entityId,
            @Min(1) long entityVersion,
            @NotNull JsonNode payload,
            @NotNull Instant clientModifiedAt
    ) {}
}
