package com.school.equipment.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;

    public ValidationErrorResponse(int status, String message,  Map<String, String> errors) {
        super(status, message);
        this.errors = errors;
    }

}