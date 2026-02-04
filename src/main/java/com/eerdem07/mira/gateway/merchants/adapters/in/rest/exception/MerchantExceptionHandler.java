package com.eerdem07.mira.gateway.merchants.adapters.in.rest.exception;

import com.eerdem07.mira.gateway.merchants.application.exception.MerchantNotFoundException;
import com.eerdem07.mira.gateway.shared.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class MerchantExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(MerchantExceptionHandler.class);

    // 400 - Bean Validation errors (jakarta.validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setType(URI.create("https://errors.mira-gateway/validation"));

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("errors", errors);

        return pd;
    }

    // 404 - Application-level not found
    @ExceptionHandler(MerchantNotFoundException.class)
    public ProblemDetail handleNotFound(MerchantNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Merchant not found");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://errors.mira-gateway/merchant-not-found"));
        return pd;
    }

    // 409 - Domain rule violations
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Domain rule violation");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://errors.mira-gateway/domain-rule-violation"));
        pd.setProperty("code", ex.code());
        return pd;
    }

    // 500 - Fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal server error");
        pd.setDetail("Unexpected error occurred.");
        pd.setType(URI.create("https://errors.mira-gateway/internal"));
        return pd;
    }

}
