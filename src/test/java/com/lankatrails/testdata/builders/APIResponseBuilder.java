package com.lankatrails.testdata.builders;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

/**
 * Fluent builder for APIResponse test data.
 * Provides convenient factory methods and fluent setters for creating
 * API response DTOs in tests.
 * 
 * Usage Examples:
 * <pre>
 * // Success response with data
 * APIResponse<String> success = aSuccessResponse()
 *     .withMessage("Operation completed successfully")
 *     .withData("result data")
 *     .build();
 * 
 * // Error response
 * APIResponse<Void> error = anErrorResponse()
 *     .withMessage("Validation failed")
 *     .withDetails(validationErrors)
 *     .build();
 * 
 * // Login success response
 * APIResponse<LoginResponse> loginSuccess = aSuccessResponse(LoginResponse.class)
 *     .withMessage("Login successful")
 *     .withData(loginResponse)
 *     .build();
 * 
 * // Generic response
 * APIResponse<Object> generic = anApiResponse()
 *     .withSuccess(true)
 *     .withMessage("Custom message")
 *     .build();
 * </pre>
 */
public class APIResponseBuilder<T> {
    
    private boolean success;
    private String message;
    private T data;
    private Object details;
    
    private APIResponseBuilder() {
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        this.success = true;
        this.message = "Operation successful";
        this.data = null;
        this.details = null;
    }
    
    // =================
    // Factory Methods
    // =================
    
    @SuppressWarnings("unchecked")
    public static <T> APIResponseBuilder<T> anApiResponse() {
        return new APIResponseBuilder<>();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> APIResponseBuilder<T> anApiResponse(Class<T> dataType) {
        return new APIResponseBuilder<>();
    }
    
    public static <T> APIResponseBuilder<T> aSuccessResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(true)
            .withMessage("Operation completed successfully");
    }
    
    public static <T> APIResponseBuilder<T> aSuccessResponse(Class<T> dataType) {
        return new APIResponseBuilder<T>()
            .withSuccess(true)
            .withMessage("Operation completed successfully");
    }
    
    public static <T> APIResponseBuilder<T> anErrorResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(false)
            .withMessage("Operation failed");
    }
    
    public static <T> APIResponseBuilder<T> anErrorResponse(Class<T> dataType) {
        return new APIResponseBuilder<T>()
            .withSuccess(false)
            .withMessage("Operation failed");
    }
    
    // Common response types
    public static APIResponseBuilder<String> aStringResponse() {
        return new APIResponseBuilder<>();
    }
    
    public static APIResponseBuilder<Void> aVoidResponse() {
        return new APIResponseBuilder<>();
    }
    
    public static APIResponseBuilder<Object> anObjectResponse() {
        return new APIResponseBuilder<>();
    }
    
    // Specific success responses
    public static <T> APIResponseBuilder<T> aCreatedResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(true)
            .withMessage("Resource created successfully");
    }
    
    public static <T> APIResponseBuilder<T> anUpdatedResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(true)
            .withMessage("Resource updated successfully");
    }
    
    public static <T> APIResponseBuilder<T> aDeletedResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(true)
            .withMessage("Resource deleted successfully");
    }
    
    // Specific error responses
    public static <T> APIResponseBuilder<T> aValidationErrorResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(false)
            .withMessage("Validation failed");
    }
    
    public static <T> APIResponseBuilder<T> aNotFoundErrorResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(false)
            .withMessage("Resource not found");
    }
    
    public static <T> APIResponseBuilder<T> anUnauthorizedErrorResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(false)
            .withMessage("Unauthorized access");
    }
    
    public static <T> APIResponseBuilder<T> aForbiddenErrorResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(false)
            .withMessage("Access forbidden");
    }
    
    public static <T> APIResponseBuilder<T> anInternalServerErrorResponse() {
        return new APIResponseBuilder<T>()
            .withSuccess(false)
            .withMessage("Internal server error");
    }
    
    // =================
    // Fluent Setters
    // =================
    
    public APIResponseBuilder<T> withSuccess(boolean success) {
        this.success = success;
        return this;
    }
    
    public APIResponseBuilder<T> withMessage(String message) {
        this.message = message;
        return this;
    }
    
    public APIResponseBuilder<T> withData(T data) {
        this.data = data;
        return this;
    }
    
    public APIResponseBuilder<T> withDetails(Object details) {
        this.details = details;
        return this;
    }
    
    // =================
    // Convenience Methods
    // =================
    
    public APIResponseBuilder<T> successful() {
        this.success = true;
        return this;
    }
    
    public APIResponseBuilder<T> failed() {
        this.success = false;
        return this;
    }
    
    public APIResponseBuilder<T> withoutData() {
        this.data = null;
        return this;
    }
    
    public APIResponseBuilder<T> withoutDetails() {
        this.details = null;
        return this;
    }
    
    public APIResponseBuilder<T> withEmptyMessage() {
        this.message = "";
        return this;
    }
    
    public APIResponseBuilder<T> withNullMessage() {
        this.message = null;
        return this;
    }
    
    // Authentication specific responses
    public APIResponseBuilder<T> withLoginSuccessMessage() {
        this.message = "Login successful";
        return this;
    }
    
    public APIResponseBuilder<T> withLoginFailureMessage() {
        this.message = "Invalid credentials";
        this.success = false;
        return this;
    }
    
    public APIResponseBuilder<T> withRegistrationSuccessMessage() {
        this.message = "Registration successful";
        return this;
    }
    
    public APIResponseBuilder<T> withRegistrationFailureMessage() {
        this.message = "Registration failed";
        this.success = false;
        return this;
    }
    
    public APIResponseBuilder<T> withEmailVerificationMessage() {
        this.message = "Email verification sent";
        return this;
    }
    
    public APIResponseBuilder<T> withPasswordResetMessage() {
        this.message = "Password reset email sent";
        return this;
    }
    
    // =================
    // Build Method
    // =================
    
    public APIResponse<T> build() {
        return APIResponse.<T>builder()
            .success(success)
            .message(message)
            .data(data)
            .details(details)
            .build();
    }
}