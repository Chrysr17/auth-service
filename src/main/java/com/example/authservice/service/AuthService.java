package com.example.authservice.service;

import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.Rol;
import com.example.authservice.entity.Usuario;
import com.example.authservice.exception.AuthException;
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
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())){
            throw new AuthException("El correo ya está registrado");
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
}
