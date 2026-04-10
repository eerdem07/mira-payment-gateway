package com.eerdem07.mira.gateway.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handleApplication(ApplicationException ex, HttpServletRequest request) {
        log.warn("Application exception: [Code: {}, Message: {}]", ex.getCode(), ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getType()
                .httpStatus());
        return problem(status, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex, HttpServletRequest request) {
        log.warn("Domain exception: [Code: {}, Message: {}]", ex.getCode(), ex.getMessage());
        // Clean Architecture gereği domain layer HTTP status bilmemeli, genel olarak 422'ye map edilir.
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return problem(status, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(InfrastructureException.class)
    public ProblemDetail handleInfrastructure(InfrastructureException ex, HttpServletRequest request) {
        log.error("Infrastructure exception: [Code: {}, Message: {}]", ex.getCode(), ex.getMessage(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode(), "An internal infrastructure error occurred.", request);
    }

    /**
     * @Valid on request body (DTO) -> MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Method argument validation failed: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        ProblemDetail pd = baseProblem(status, "VALIDATION_ERROR", "Request validation failed.", request);
        pd.setProperty("errors", errors);
        return pd;
    }

    /**
     * @Validated on params/path/query -> ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<Map<String, Object>> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.<String, Object>of(
                        "path", String.valueOf(v.getPropertyPath()),
                        "message", v.getMessage()
                ))
                .toList();

        ProblemDetail pd = baseProblem(status, "VALIDATION_ERROR", "Constraint violation.", request);
        pd.setProperty("errors", errors);
        return pd;
    }
    
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected server error occurred: ", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Unexpected server error.", request);
    }

    // ---------------- helpers ----------------

    private ProblemDetail problem(HttpStatus status, String code, String detail, HttpServletRequest request) {
        ProblemDetail pd = baseProblem(status, code, detail, request);

        // İstersen correlation için header’dan ekle:
        // String traceId = request.getHeader("X-Trace-Id");
        // if (traceId != null && !traceId.isBlank()) pd.setProperty("traceId", traceId);

        return pd;
    }

    private ProblemDetail baseProblem(HttpStatus status, String code, String detail, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);

        // RFC7807 alanları
        pd.setTitle(code);
        pd.setType(URI.create("urn:mira:error:" + code));
        pd.setInstance(URI.create(request.getRequestURI()));

        // Ek meta (tutarlı debugging için)
        pd.setProperty("timestamp", Instant.now()
                .toString());
        pd.setProperty("path", request.getRequestURI());

        return pd;
    }

    private Map<String, Object> toFieldError(FieldError e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("field", e.getField());
        m.put("message", e.getDefaultMessage());

        // Güvenlik Zafiyeti Çözümü: Hassas verilerin sızdırılmasını engelle
        Object rejectedValue = e.getRejectedValue();
        if (isSensitiveField(e.getField())) {
            rejectedValue = "***";
        }
        m.put("rejectedValue", rejectedValue);

        return m;
    }

    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lower = fieldName.toLowerCase();
        return lower.contains("password") || lower.contains("secret") || lower.contains("token") || lower.contains("credential");
    }
}