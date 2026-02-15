package com.opr3.opr3.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.opr3.opr3.dto.ErrorResponse;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Handles ResourceNotFoundException
         * HTTP Status: 404 NOT_FOUND
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {

                log.warn("[404] Resource not found: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        /**
         * Handles ResourceAlreadyExistsException
         * HTTP Status: 409 CONFLICT
         */
        @ExceptionHandler(ResourceAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(
                        ResourceAlreadyExistsException ex, WebRequest request) {

                log.warn("[409] Resource already exists: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        /**
         * Handles InvalidRequestException
         * HTTP Status: 400 BAD_REQUEST
         */
        @ExceptionHandler(InvalidRequestException.class)
        public ResponseEntity<ErrorResponse> handleInvalidRequestException(
                        InvalidRequestException ex, WebRequest request) {

                log.warn("[400] Invalid request: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles IllegalArgumentException
         * HTTP Status: 400 BAD_REQUEST
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex, WebRequest request) {

                log.warn("[400] Illegal argument: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles BadCredentialsException
         * HTTP Status: 401 UNAUTHORIZED
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, WebRequest request) {

                log.warn("[401] Bad credentials: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Invalid email or password")
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles UnauthorizedException
         * HTTP Status: 401 UNAUTHORIZED
         */
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        UnauthorizedException ex, WebRequest request) {

                log.warn("[401] Unauthorized: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles InsufficientAuthenticationException
         * HTTP Status: 401 UNAUTHORIZED
         */
        @ExceptionHandler(InsufficientAuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleInsufficientAuthenticationException(
                        InsufficientAuthenticationException ex, WebRequest request) {

                log.warn("[401] Insufficient authentication: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Authentication required")
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles AuthenticationException
         * HTTP Status: 401 UNAUTHORIZED
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex, WebRequest request) {

                log.warn("[401] Authentication failed: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Authentication failed")
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles ForbiddenException
         * HTTP Status: 403 FORBIDDEN
         */
        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbiddenException(
                        ForbiddenException ex, WebRequest request) {

                log.warn("[403] Forbidden: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles JwtException
         * HTTP Status: 401 UNAUTHORIZED
         */
        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ErrorResponse> handleJwtException(
                        JwtException ex, WebRequest request) {

                log.warn("[401] JWT error: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Invalid or expired token")
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles NullPointerException
         * HTTP Status: 500 INTERNAL_SERVER_ERROR
         */
        @ExceptionHandler(NullPointerException.class)
        public ResponseEntity<ErrorResponse> handleNullPointerException(
                        NullPointerException ex, WebRequest request) {

                log.error("[500] Null pointer exception: {}", ex.getMessage(), ex);

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("Internal server error: missing required data")
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        /**
         * Handles IllegalStateException
         * HTTP Status: 409 CONFLICT
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleIllegalStateException(
                        IllegalStateException ex, WebRequest request) {

                log.warn("[409] Illegal state: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        /**
         * Handles all other uncaught exceptions.
         * HTTP Status: 500 INTERNAL_SERVER_ERROR
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex, WebRequest request) {

                log.error("[500] Unexpected error: {}", ex.getMessage(), ex);

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("An unexpected error occurred")
                                .timestamp(LocalDateTime.now())
                                .path(getRequestPath(request))
                                .build();

                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private String getRequestPath(WebRequest request) {
                try {
                        return request.getDescription(false).replace("uri=", "");
                } catch (Exception e) {
                        return "";
                }
        }
}
