package org.comppress.customnewsapi.exceptions;

import org.comppress.customnewsapi.constant.ErrorCodes;
import org.comppress.customnewsapi.dto.ErrorResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getVariable())
                .errorCode(ErrorCodes.CNA_001)
                .build());
    }

    @ExceptionHandler(CriteriaDoesNotExistException.class)
    public ResponseEntity<ErrorResponseDto> handleCriteriaDoesNotExistException(CriteriaDoesNotExistException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getVariable())
                .errorCode(ErrorCodes.CNA_002)
                .build());
    }

    @ExceptionHandler(ArticleDoesNotExistException.class)
    public ResponseEntity<ErrorResponseDto> handleArticleDoesNotExistException(ArticleDoesNotExistException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getVariable())
                .errorCode(ErrorCodes.CNA_003)
                .build());
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEntryException(DuplicateEntryException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getVariable())
                .errorCode(ErrorCodes.CNA_004)
                .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getVariable())
                .errorCode(ErrorCodes.CNA_005)
                .build());
    }

    @ExceptionHandler(PublisherDoesNotExistException.class)
    public ResponseEntity<ErrorResponseDto> handlePublisherDoesNotExistException(PublisherDoesNotExistException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getVariable())
                .errorCode(ErrorCodes.CNA_006)
                .build());
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(GeneralException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getVariable())
                .errorCode(ErrorCodes.CNA_006)
                .build());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseDto.builder()
                .message(ex.getMessage())
                .variable(ex.getLocalizedMessage())
                .errorCode(ErrorCodes.CNA_999)
                .build());
    }
}