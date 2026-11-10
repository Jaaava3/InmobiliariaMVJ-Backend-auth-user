package cl.inmobiliaria.authuser.service;

import cl.inmobiliaria.authuser.dto.LoginRequest;
import cl.inmobiliaria.authuser.dto.LoginResponse;
import cl.inmobiliaria.authuser.dto.RegistroRequest;
import cl.inmobiliaria.authuser.model.Rol;
import cl.inmobiliaria.authuser.model.Usuario;
import cl.inmobiliaria.authuser.repository.RolRepository;
import cl.inmobiliaria.authuser.repository.UsuarioRepository;
import cl.inmobiliaria.authuser.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse registrar(RegistroRequest dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("El correo ya se encuentra registrado");
        }

        // @mvj.cl es ADMIN, cualquier otro dominio es USUARIO
        String nombreRol = dto.getCorreo().toLowerCase().endsWith("@mvj.cl") ? "ADMIN" : "USUARIO";

        Rol rolAsignado = rolRepository.findByTipoRol(nombreRol)
                .orElseThrow(() -> new RuntimeException("El rol " + nombreRol + " no existe"));

        Usuario nuevoUsuario = Usuario.builder()
                .correo(dto.getCorreo().toLowerCase().trim())
                .claveHash(passwordEncoder.encode(dto.getClave()))
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .rut(dto.getRut())
                .telefono(dto.getTelefono())
                .rol(rolAsignado)
                .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        String token = jwtService.generarToken(guardado.getCorreo(), guardado.getRol().getTipoRol());

        return LoginResponse.builder()
                .token(token)
                .id(guardado.getId())
                .correo(guardado.getCorreo())
                .nombre(guardado.getNombre())
                .apellido(guardado.getApellido())
                .rut(guardado.getRut())
                .telefono(guardado.getTelefono())
                .tipoRol(guardado.getRol().getTipoRol())
                .build();
    }

    public LoginResponse login(LoginRequest dto) {
        Usuario usuario = usuarioRepository.findByCorreo(dto.getCorreo().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.getClave(), usuario.getClaveHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuario.getCorreo(), usuario.getRol().getTipoRol());

        return LoginResponse.builder()
                .token(token)
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rut(usuario.getRut())
                .telefono(usuario.getTelefono())
                .tipoRol(usuario.getRol().getTipoRol())
                .build();
    }

    public LoginResponse obtenerPerfil(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return LoginResponse.builder()
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rut(usuario.getRut())
                .telefono(usuario.getTelefono())
                .tipoRol(usuario.getRol().getTipoRol())
                .build();
    }
}
