package cl.pagoseguro.accounts.infrastructure.adapter.in.web;

import cl.pagoseguro.accounts.domain.exception.AccountNotFoundException;
import cl.pagoseguro.accounts.domain.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/** Traduce excepciones a respuestas HTTP con formato estándar RFC 9457 (ProblemDetail). */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail handleNotFound(AccountNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Cuenta no encontrada");
        return problem;
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomain(DomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Regla de negocio no cumplida");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> String.valueOf(e.getDefaultMessage()), (a, b) -> a));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud tiene campos inválidos");
        problem.setTitle("Solicitud inválida");
        problem.setProperty("errors", errors);
        return problem;
    }
}
