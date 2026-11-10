package cl.inmobiliaria.authuser.controller;

import cl.inmobiliaria.authuser.dto.LoginRequest;
import cl.inmobiliaria.authuser.dto.LoginResponse;
import cl.inmobiliaria.authuser.dto.RegistroRequest;
import cl.inmobiliaria.authuser.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroRequest dto) {
        LoginResponse response = authService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest dto) {
        LoginResponse response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/perfil")
    public ResponseEntity<LoginResponse> obtenerPerfil(Authentication authentication) {
        String correo = authentication.getName();
        LoginResponse response = authService.obtenerPerfil(correo);
        return ResponseEntity.ok(response);
    }
}
