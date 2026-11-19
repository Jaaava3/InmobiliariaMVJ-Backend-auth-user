package cl.inmobiliaria.authuser.service;

import cl.inmobiliaria.authuser.dto.LoginRequest;
import cl.inmobiliaria.authuser.dto.LoginResponse;
import cl.inmobiliaria.authuser.dto.RegistroRequest;
import cl.inmobiliaria.authuser.model.Rol;
import cl.inmobiliaria.authuser.model.Usuario;
import cl.inmobiliaria.authuser.repository.RolRepository;
import cl.inmobiliaria.authuser.repository.UsuarioRepository;
import cl.inmobiliaria.authuser.security.JwtService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - AuthService")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private final Faker faker = new Faker(new Locale("es"));

    private Rol rolUsuarioMock;
    private Rol rolAdminMock;
    private Usuario usuarioMock;
    private RegistroRequest registroRequestMock;
    private LoginRequest loginRequestMock;
    private String correoMock;
    private String claveMock;
    private String rutValidoMock;
    private Long idMock;

    @BeforeEach
    void setUp() {
        idMock = 1L;
        correoMock = faker.internet().emailAddress().toLowerCase();
        claveMock = "ClaveSegura123!";
        rutValidoMock = "12.345.678-5"; // Dígito verificador válido según Módulo 11

        rolUsuarioMock = Rol.builder().id(2L).tipoRol("USUARIO").build();
        rolAdminMock = Rol.builder().id(1L).tipoRol("ADMIN").build();

        String nombreMock = faker.name().firstName();
        String apellidoMock = faker.name().lastName();

        usuarioMock = Usuario.builder()
                .id(idMock)
                .correo(correoMock)
                .claveHash("$2a$10$hashEncriptado")
                .nombre(nombreMock)
                .apellido(apellidoMock)
                .rut(rutValidoMock)
                .rol(rolUsuarioMock)
                .build();

        registroRequestMock = RegistroRequest.builder()
                .correo(correoMock)
                .clave(claveMock)
                .nombre(nombreMock)
                .apellido(apellidoMock)
                .rut(rutValidoMock)
                .build();

        loginRequestMock = LoginRequest.builder()
                .correo(correoMock)
                .clave(claveMock)
                .build();
    }

    // FLUJOS DE: registrar()

    @Test
    @DisplayName("Debe registrar un usuario normal con rol USUARIO y retornar su token JWT")
    void registrar_ExitoComoUsuario() {
        // GIVEN
        when(usuarioRepository.existsByCorreo(correoMock)).thenReturn(false);
        when(usuarioRepository.existsByRut(rutValidoMock)).thenReturn(false);
        when(rolRepository.findByTipoRol("USUARIO")).thenReturn(Optional.of(rolUsuarioMock));
        when(passwordEncoder.encode(claveMock)).thenReturn("$2a$10$hashEncriptado");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);
        when(jwtService.generarToken(correoMock, "USUARIO")).thenReturn("jwt.token.usuario");

        // WHEN
        LoginResponse response = authService.registrar(registroRequestMock);

        // THEN
        assertNotNull(response);
        assertEquals("jwt.token.usuario", response.getToken());
        assertEquals(correoMock, response.getCorreo());
        assertEquals("USUARIO", response.getTipoRol());
        verify(usuarioRepository).save(any(Usuario.class));
        verify(jwtService).generarToken(correoMock, "USUARIO");
    }

    @Test
    @DisplayName("Debe asignar automáticamente rol ADMIN cuando el correo termina en @mvj.cl")
    void registrar_ExitoComoAdmin() {
        // GIVEN
        String correoAdmin = "admin@mvj.cl";
        String rutConK = "10.000.013-K"; // RUT válido terminando en K

        RegistroRequest adminRequest = RegistroRequest.builder()
                .correo(correoAdmin)
                .clave(claveMock)
                .nombre("Admin")
                .apellido("MVJ")
                .rut(rutConK)
                .build();

        Usuario adminGuardado = Usuario.builder()
                .id(2L)
                .correo(correoAdmin)
                .claveHash("$2a$10$hashAdmin")
                .nombre("Admin")
                .apellido("MVJ")
                .rut(rutConK)
                .rol(rolAdminMock)
                .build();

        when(usuarioRepository.existsByCorreo(correoAdmin)).thenReturn(false);
        when(usuarioRepository.existsByRut(rutConK)).thenReturn(false);
        when(rolRepository.findByTipoRol("ADMIN")).thenReturn(Optional.of(rolAdminMock));
        when(passwordEncoder.encode(claveMock)).thenReturn("$2a$10$hashAdmin");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(adminGuardado);
        when(jwtService.generarToken(correoAdmin, "ADMIN")).thenReturn("jwt.token.admin");

        // WHEN
        LoginResponse response = authService.registrar(adminRequest);

        // THEN
        assertNotNull(response);
        assertEquals("ADMIN", response.getTipoRol());
        assertEquals("jwt.token.admin", response.getToken());
        verify(rolRepository).findByTipoRol("ADMIN");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el dígito verificador del RUT es inválido")
    void registrar_FallaPorRutInvalido() {
        // GIVEN
        registroRequestMock.setRut("12.345.678-9"); // DV incorrecto (debería ser 5)

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> authService.registrar(registroRequestMock));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el largo del RUT es inválido")
    void registrar_FallaPorLongitudRutInvalida() {
        // GIVEN
        registroRequestMock.setRut("123-4");

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> authService.registrar(registroRequestMock));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe registrar exitosamente usuarios con RUT terminado en -0 y en -K")
    void registrar_RutsEspecialesCeroYK_Exito() {
        Rol rolUsuario = Rol.builder().id(2L).tipoRol("USUARIO").build();

        RegistroRequest dtoCero = RegistroRequest.builder()
                .nombre("Camila")
                .apellido("Soto")
                .rut("11.111.117-0")
                .correo("camila@gmail.com")
                .clave("ClaveSegura#1")
                .build();

        RegistroRequest dtoK = RegistroRequest.builder()
                .nombre("Diego")
                .apellido("Rojas")
                .rut("11.111.112-K")
                .correo("diego@gmail.com")
                .clave("ClaveSegura#1")
                .build();

        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(usuarioRepository.existsByRut(anyString())).thenReturn(false);
        when(rolRepository.findByTipoRol("USUARIO")).thenReturn(Optional.of(rolUsuario));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtService.generarToken(anyString(), eq("USUARIO"))).thenReturn("token-jwt");

        LoginResponse respCero = authService.registrar(dtoCero);
        LoginResponse respK = authService.registrar(dtoK);

        assertEquals("11.111.117-0", respCero.getRut());
        assertEquals("11.111.112-K", respK.getRut());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el RUT excede el largo máximo permitido")
    void registrar_RutDemasiadoLargo_Falla() {
        RegistroRequest dtoLargo = RegistroRequest.builder()
                .nombre("Pedro")
                .apellido("Pérez")
                .rut("111.111.111-1")
                .correo("pedro@gmail.com")
                .clave("ClaveSegura#1")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.registrar(dtoLargo));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar DataIntegrityViolationException si el correo ya está registrado")
    void registrar_FallaPorCorreoDuplicado() {
        // GIVEN
        when(usuarioRepository.existsByCorreo(correoMock)).thenReturn(true);

        // WHEN & THEN
        assertThrows(DataIntegrityViolationException.class,
                () -> authService.registrar(registroRequestMock));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar DataIntegrityViolationException si el RUT ya está registrado")
    void registrar_FallaPorRutDuplicado() {
        // GIVEN
        when(usuarioRepository.existsByCorreo(correoMock)).thenReturn(false);
        when(usuarioRepository.existsByRut(rutValidoMock)).thenReturn(true);

        // WHEN & THEN
        assertThrows(DataIntegrityViolationException.class,
                () -> authService.registrar(registroRequestMock));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si el rol asignado no existe en la base de datos")
    void registrar_FallaPorRolNoEncontrado() {
        // GIVEN
        when(usuarioRepository.existsByCorreo(correoMock)).thenReturn(false);
        when(usuarioRepository.existsByRut(rutValidoMock)).thenReturn(false);
        when(rolRepository.findByTipoRol("USUARIO")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(NoSuchElementException.class,
                () -> authService.registrar(registroRequestMock));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // FLUJOS DE: login()

    @Test
    @DisplayName("Debe validar credenciales y retornar LoginResponse con token JWT")
    void login_Exito() {
        // GIVEN
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches(claveMock, usuarioMock.getClaveHash())).thenReturn(true);
        when(jwtService.generarToken(correoMock, "USUARIO")).thenReturn("jwt.token.login");

        // WHEN
        LoginResponse response = authService.login(loginRequestMock);

        // THEN
        assertNotNull(response);
        assertEquals("jwt.token.login", response.getToken());
        assertEquals(correoMock, response.getCorreo());
        assertEquals("USUARIO", response.getTipoRol());
        verify(jwtService).generarToken(correoMock, "USUARIO");
    }

    @Test
    @DisplayName("Debe lanzar BadCredentialsException cuando el correo no existe en login")
    void login_FallaPorCorreoNoExistente() {
        // GIVEN
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequestMock));

        verify(jwtService, never()).generarToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar BadCredentialsException cuando la contraseña no coincide")
    void login_FallaPorClaveIncorrecta() {
        // GIVEN
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches(claveMock, usuarioMock.getClaveHash())).thenReturn(false);

        // WHEN & THEN
        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequestMock));

        verify(jwtService, never()).generarToken(anyString(), anyString());
    }

    // FLUJOS DE: obtenerPerfil()

    @Test
    @DisplayName("Debe retornar los datos del perfil cuando el correo existe")
    void obtenerPerfil_Exito() {
        // GIVEN
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.of(usuarioMock));

        // WHEN
        LoginResponse response = authService.obtenerPerfil(correoMock);

        // THEN
        assertNotNull(response);
        assertEquals(idMock, response.getId());
        assertEquals(correoMock, response.getCorreo());
        assertEquals(rutValidoMock, response.getRut());
        assertEquals("USUARIO", response.getTipoRol());
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si el correo del perfil no existe")
    void obtenerPerfil_UsuarioNoEncontrado() {
        // GIVEN
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(NoSuchElementException.class,
                () -> authService.obtenerPerfil(correoMock));
    }
}
