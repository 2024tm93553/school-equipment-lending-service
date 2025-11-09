package com.school.equipment.dto.user;

import com.school.equipment.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Role role;
}
