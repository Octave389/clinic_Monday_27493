package kigali.clinic.clinicmanagement.utils;

import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

// bad input is answered here once, so controllers can parse without try/catch
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleBadDate(DateTimeParseException e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Dates must use the format yyyy-MM-dd"));
    }

    // a request parameter or path value that can not be turned into its Java type, e.g. ?status=WRONG
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleBadParameter(MethodArgumentTypeMismatchException e) {

        Class<?> type = e.getRequiredType();

        if (type != null && type.isEnum()) {
            String allowed = Arrays.stream(type.getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("message", e.getName() + " must be one of " + allowed));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Invalid value for " + e.getName()));
    }

    // an unknown sort property on the paging endpoint, e.g. sort=colour,desc
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<?> handleUnknownProperty(PropertyReferenceException e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Unknown sort property " + e.getPropertyName()));
    }
}
