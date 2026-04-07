package com.example.authservice.service;

import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.exception.AuthException;
import com.example.authservice.exception.InvalidAuthRequestException;
import com.example.authservice.exception.ResourceConflictException;
import com.example.authservice.model.Rol;
import com.example.authservice.model.Usuario;
import com.example.authservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtResponse login(LoginRequest request) {
        validarLoginRequest(request);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));
        String token = jwtService.generateToken(usuario.getUsername(), usuario.getRol().name());
        return new JwtResponse(token);
    }

    public JwtResponse register(RegisterRequest request) {
        validarRegisterRequest(request);

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("El correo ya está registrado");
        }
        if (request.getRol() == Rol.ADMIN) {
            throw new InvalidAuthRequestException("No se permite registrar usuarios ADMIN desde el endpoint publico");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .rol(request.getRol() == null ? Rol.USER : request.getRol())
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getUsername(), usuario.getRol().name());
        return new JwtResponse(token);
    }

    private void validarLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new InvalidAuthRequestException("La solicitud de login es obligatoria");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new InvalidAuthRequestException("username es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidAuthRequestException("password es obligatorio");
        }
    }

    private void validarRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new InvalidAuthRequestException("La solicitud de registro es obligatoria");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new InvalidAuthRequestException("username es obligatorio");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidAuthRequestException("email es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidAuthRequestException("password es obligatorio");
        }
    }
}
