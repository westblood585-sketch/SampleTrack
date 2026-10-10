package com.lab.sample.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Tum hatalari {@link ErrorResponse} formatina cevirir.
 * Spring MVC'nin standart hatalari ResponseEntityExceptionHandler uzerinden ayni formata yonlendirilir.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                    WebRequest request) {
        List<FieldErrorDetail> details = ex.getConstraintViolations().stream()
                .map(v -> new FieldErrorDetail(lastNode(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "İstek parametreleri geçersiz", request, details);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                              WebRequest request) {
        log.warn("Veri butunlugu ihlali: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "İşlem veri bütünlüğü kurallarını ihlal ediyor", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled server exception occurred: ", ex);
        log.error("Beklenmeyen hata", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Beklenmeyen bir hata oluştu", request, List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR", "İstek doğrulaması başarısız", pathOf(request), details));
    }

    /** Spring MVC'nin standart (400/404/405/415...) hatalarinin tamami buradan gecer. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                             HttpHeaders headers, HttpStatusCode statusCode,
                                                             WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        HttpStatus resolved = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.of(resolved, codeFor(ex), messageFor(ex, resolved),
                pathOf(request), List.of());
        return ResponseEntity.status(statusCode).headers(headers).body(response);
    }

    private static String codeFor(Exception ex) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "MALFORMED_REQUEST";
        }
        if (ex instanceof TypeMismatchException) {
            return "TYPE_MISMATCH";
        }
        if (ex instanceof MissingServletRequestParameterException) {
            return "MISSING_PARAMETER";
        }
        if (ex instanceof NoResourceFoundException) {
            return "ENDPOINT_NOT_FOUND";
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return "METHOD_NOT_ALLOWED";
        }
        return "REQUEST_ERROR";
    }

    private static String messageFor(Exception ex, HttpStatus status) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "İstek gövdesi okunamadı veya geçersiz";
        }
        if (ex instanceof TypeMismatchException typeMismatch) {
            return "'%s' parametresi için geçersiz değer".formatted(typeMismatch.getPropertyName());
        }
        if (ex instanceof MissingServletRequestParameterException missing) {
            return "Zorunlu parametre eksik: %s".formatted(missing.getParameterName());
        }
        if (ex instanceof NoResourceFoundException) {
            return "İstenen adres bulunamadı";
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return "Bu adres için HTTP metodu desteklenmiyor";
        }
        return status.getReasonPhrase();
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                       WebRequest request, List<FieldErrorDetail> details) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, code, message, pathOf(request), details));
    }

    private static String pathOf(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return "";
    }

    private static String lastNode(String propertyPath) {
        int idx = propertyPath.lastIndexOf('.');
        return idx >= 0 ? propertyPath.substring(idx + 1) : propertyPath;
    }
}
