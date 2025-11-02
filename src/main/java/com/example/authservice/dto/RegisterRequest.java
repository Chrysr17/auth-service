package com.example.authservice.dto;

import com.example.authservice.entity.Rol;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String dni;
    private String password;
    private Rol rol ;

}
