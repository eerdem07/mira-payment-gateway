package com.eerdem07.mira.gateway.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handleApplication(ApplicationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getType()
                .httpStatus());
        return problem(status, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getType()
                .httpStatus());
        return problem(status, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @Valid on request body (DTO) -> MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
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
        // Prod ortamında ex.getMessage() / stack trace sızdırma.
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
        // rejectedValue bazen hassas olabilir (password vs). İstersen bunu kapatırız.
        m.put("rejectedValue", e.getRejectedValue());
        return m;
    }
}