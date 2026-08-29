package com.dlr.shared.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import com.dlr.tutor.application.TutorUnavailableException;
import com.dlr.sync.application.SyncAuthenticationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SyncAuthenticationException.class)
    ProblemDetail handleSyncAuthentication(SyncAuthenticationException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        detail.setTitle("SYNC_AUTHENTICATION_REQUIRED");
        detail.setType(URI.create("urn:dlr:error:sync-authentication-required"));
        return enrich(detail, request, "Appaire cet appareil ou renouvelle son jeton de synchronisation.");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setTitle(exception.code());
        detail.setType(URI.create("urn:dlr:error:" + exception.code().toLowerCase()));
        return enrich(detail, request, "Vérifie l'identifiant demandé puis recharge la ressource.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "La requête contient des valeurs invalides.");
        detail.setTitle("VALIDATION_ERROR");
        detail.setType(URI.create("urn:dlr:error:validation"));
        detail.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList());
        return enrich(detail, request, "Corrige les champs indiqués puis renvoie la requête.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setTitle("INVALID_REQUEST");
        detail.setType(URI.create("urn:dlr:error:invalid-request"));
        return enrich(detail, request, "Corrige les données envoyées puis réessaie.");
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleIllegalState(IllegalStateException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setTitle("INVALID_ATTEMPT_STATE");
        detail.setType(URI.create("urn:dlr:error:invalid-attempt-state"));
        return enrich(detail, request, "Recharge l'état courant du laboratoire avant de continuer.");
    }

    @ExceptionHandler(TutorUnavailableException.class)
    ProblemDetail handleTutorUnavailable(TutorUnavailableException exception, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        detail.setTitle("OLLAMA_UNAVAILABLE");
        detail.setType(URI.create("urn:dlr:error:ollama-unavailable"));
        return enrich(detail, request, "Démarre Ollama ou poursuis le laboratoire en mode dégradé.");
    }

    private ProblemDetail enrich(ProblemDetail detail, HttpServletRequest request, String suggestedAction) {
        Object correlationId = request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        detail.setProperty("correlationId", correlationId == null ? "unknown" : correlationId.toString());
        detail.setProperty("suggestedAction", suggestedAction);
        return detail;
    }
}
