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
        String correoLimpio = dto.getCorreo().toLowerCase().trim();
        String rutLimpio = dto.getRut().trim().toUpperCase();

        if (!esRutChilenoValido(rutLimpio)) {
            throw new RuntimeException("El dígito verificador del RUT no es válido");
        }

        if (usuarioRepository.existsByCorreo(correoLimpio)) {
            throw new RuntimeException("El correo ya se encuentra registrado");
        }

        if (usuarioRepository.existsByRut(rutLimpio)) {
            throw new RuntimeException("El RUT ya se encuentra registrado");
        }

        String nombreRol = correoLimpio.endsWith("@mvj.cl") ? "ADMIN" : "USUARIO";

        Rol rolAsignado = rolRepository.findByTipoRol(nombreRol)
                .orElseThrow(() -> new RuntimeException("El rol " + nombreRol + " no existe"));

        Usuario nuevoUsuario = Usuario.builder()
                .correo(correoLimpio)
                .claveHash(passwordEncoder.encode(dto.getClave()))
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .rut(rutLimpio)
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
                .tipoRol(guardado.getRol().getTipoRol())
                .build();
    }

    private boolean esRutChilenoValido(String rutFormateado) {
        String limpio = rutFormateado.replaceAll("[^0-9kK]", "").toUpperCase();
        if (limpio.length() < 8 || limpio.length() > 9) return false;

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dvIngresado = limpio.charAt(limpio.length() - 1);

        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);
        char dvEsperado = (resto == 11) ? '0' : (resto == 10) ? 'K' : Character.forDigit(resto, 10);

        return dvIngresado == dvEsperado;
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
                .tipoRol(usuario.getRol().getTipoRol())
                .build();
    }
}
