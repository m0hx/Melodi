package com.ga.melodi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InformationNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(InformationNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(404, ex.getMessage()));
	}

	@ExceptionHandler(InformationExistException.class)
	public ResponseEntity<ApiError> handleConflict(InformationExistException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(409, ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		FieldError fieldError = ex.getBindingResult().getFieldError();
		String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(400, message));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(401, "Invalid email or password"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(403, "Access denied"));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(400, ex.getMessage()));
	}
}
