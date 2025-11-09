package com.school.equipment.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidCredentialsException extends Exception {
    String message;
}
