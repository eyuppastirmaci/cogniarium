package com.eyuppastirmaci.cogniariumbackend.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid request",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation error: {}", ex.message)
        
        val errorMessage = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = errorMessage,
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(
        ex: BadCredentialsException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Bad credentials: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = "Invalid email or password",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(DisabledException::class)
    fun handleDisabledException(
        ex: DisabledException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("User account disabled: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = "Please verify your email address",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal state: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid request state",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(EmbeddingGenerationException::class)
    fun handleEmbeddingGenerationException(
        ex: EmbeddingGenerationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Embedding generation failed: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "Failed to generate embedding",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: org.springframework.security.access.AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = "You do not have permission to access this resource",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException::class)
    fun handleAuthenticationException(
        ex: org.springframework.security.core.AuthenticationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Authentication failed: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Unauthorized",
            message = ex.message ?: "Authentication failed",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(java.util.concurrent.TimeoutException::class)
    fun handleTimeoutException(
        ex: java.util.concurrent.TimeoutException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Request timeout: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.REQUEST_TIMEOUT.value(),
            error = "Request Timeout",
            message = "The request took too long to process",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.requestURI
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

