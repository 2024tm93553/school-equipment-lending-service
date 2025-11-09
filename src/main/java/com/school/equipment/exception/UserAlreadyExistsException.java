package com.school.equipment.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserAlreadyExistsException extends Exception {
    String message;
}
