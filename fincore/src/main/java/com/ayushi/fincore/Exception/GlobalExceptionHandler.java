package com.ayushi.fincore.Exception;

import org.springframework.http.*;
import com.ayushi.fincore.Exception.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex){
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );

    }
}
