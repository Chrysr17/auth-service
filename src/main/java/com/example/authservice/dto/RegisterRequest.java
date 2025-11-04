package com.example.authservice.dto;

import com.example.authservice.entity.Rol;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private Rol rol ;

}
