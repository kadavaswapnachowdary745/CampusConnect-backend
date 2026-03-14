package com.example.CampusConnectMP.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private boolean blocked;
}
