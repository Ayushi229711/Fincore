package com.ayushi.fincore.Dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
