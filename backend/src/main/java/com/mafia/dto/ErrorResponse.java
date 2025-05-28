package com.mafia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard error response format")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Timestamp of when the error occurred", example = "2024-05-28T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "General error message or code", example = "Validation Failed")
    private String error;

    @Schema(description = "Specific error message", example = "One or more fields are invalid.")
    private String message;

    @Schema(description = "Path of the request that caused the error", example = "/api/auth/register")
    private String path;

    @Schema(description = "Detailed field-specific errors for validation issues")
    private Map<String, String> fieldErrors;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
            Map<String, String> fieldErrors) {
        this(timestamp, status, error, message, path);
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}