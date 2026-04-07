package com.example.authservice.service;

import com.example.authservice.dto.JwtResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.exception.InvalidAuthRequestException;
import com.example.authservice.exception.ResourceConflictException;
import com.example.authservice.model.Rol;
import com.example.authservice.model.Usuario;
import com.example.authservice.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_deberiaAutenticarYRetornarToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("christian");
        request.setPassword("secreto123");

        Usuario usuario = Usuario.builder()
                .username("christian")
                .password("hash")
                .rol(Rol.USER)
                .build();

        when(usuarioRepository.findByUsername("christian")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken("christian", "USER")).thenReturn("jwt-token");

        JwtResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_deberiaLanzarExcepcionSiUsernameEsInvalido() {
        LoginRequest request = new LoginRequest();
        request.setUsername(" ");
        request.setPassword("secreto123");

        InvalidAuthRequestException exception = assertThrows(InvalidAuthRequestException.class,
                () -> authService.login(request));

        assertEquals("username es obligatorio", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void register_deberiaCrearUsuarioUserPorDefectoYRetornarToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("christian");
        request.setPassword("secreto123");
        request.setEmail("christian@test.com");

        when(usuarioRepository.existsByUsername("christian")).thenReturn(false);
        when(usuarioRepository.existsByEmail("christian@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("hash");
        when(jwtService.generateToken("christian", "USER")).thenReturn("jwt-token");

        JwtResponse response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void register_deberiaLanzarConflictoSiUsernameYaExiste() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("christian");
        request.setPassword("secreto123");
        request.setEmail("christian@test.com");

        when(usuarioRepository.existsByUsername("christian")).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> authService.register(request));

        assertEquals("El nombre de usuario ya está en uso", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void register_deberiaLanzarConflictoSiEmailYaExiste() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("christian");
        request.setPassword("secreto123");
        request.setEmail("christian@test.com");

        when(usuarioRepository.existsByUsername("christian")).thenReturn(false);
        when(usuarioRepository.existsByEmail("christian@test.com")).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> authService.register(request));

        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void register_deberiaLanzarExcepcionSiSeIntentaRegistrarAdmin() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("secreto123");
        request.setEmail("admin@test.com");
        request.setRol(Rol.ADMIN);

        when(usuarioRepository.existsByUsername("admin")).thenReturn(false);
        when(usuarioRepository.existsByEmail("admin@test.com")).thenReturn(false);

        InvalidAuthRequestException exception = assertThrows(InvalidAuthRequestException.class,
                () -> authService.register(request));

        assertEquals("No se permite registrar usuarios ADMIN desde el endpoint publico", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }
}
