package com.ssafy.exhi.advice;

import com.ssafy.exhi.base.ApiResponse;
import com.ssafy.exhi.base.code.ErrorReasonDTO;
import com.ssafy.exhi.base.status.ErrorStatus;
import com.ssafy.exhi.exception.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(ExceptionHandler.class)
    public ResponseEntity<?> handleTempHandlerException(ExceptionHandler t) {
        ErrorReasonDTO reason = t.getCode().getReason();
        log.error(reason.toString());

        return ResponseEntity.status(t.getCode().getReasonHttpStatus().getHttpStatus())
                .body(ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation Failed");
        body.put("errors", errors);
		ErrorStatus error = ErrorStatus.NOT_ENOUGH_REQUEST_BODY_ERROR;
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.onFailure(
                        error.getCode(),
						error.getMessage(),
                        body
                ));
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handleSQLException(SQLException e) {
        log.error("SQLException Handler: {}", e.getMessage());

        ErrorReasonDTO reason = switch (e.getErrorCode()) {
            // Duplicate Key Violations
            case 1062 -> {
                log.error("중복된 데이터 입력 시도: {}", e.getMessage());
                yield ErrorStatus.DATABASE_DUPLICATE_RESOURCE.getReason();
            }

            // Default case for unhandled SQL errors
            default -> {
                log.error("처리되지 않은 SQL 예외 발생. Error Code: {}, Message: {}",
                        e.getErrorCode(), e.getMessage());
                yield ErrorStatus.DATABASE_INTERNAL_SERVER_ERROR.getReason();
            }
        };

        return ResponseEntity
                .status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(
                        reason.getCode(),
                        reason.getMessage(),
                        Map.of("sqlErrorCode", String.valueOf(e.getErrorCode()))
                ));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllException(Exception e, WebRequest request) {
        log.error("Exception Handler: {}", e.getMessage());
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.onFailure(
                        ErrorStatus._INTERNAL_SERVER_ERROR.getCode(),
                        e.getMessage(),
                        null
                ));

    }

}
