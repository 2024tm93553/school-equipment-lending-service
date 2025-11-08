package com.school.equipment.dto.user;

import com.school.equipment.dto.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Role role;
    private String fullName;
    private Long userId;
}
