package cl.inmobiliaria.authuser.controller;

import cl.inmobiliaria.authuser.dto.LoginRequest;
import cl.inmobiliaria.authuser.dto.LoginResponse;
import cl.inmobiliaria.authuser.dto.RegistroRequest;
import cl.inmobiliaria.authuser.security.JwtAutenticacionFilter;
import cl.inmobiliaria.authuser.security.JwtService;
import cl.inmobiliaria.authuser.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Pruebas Unitarias - AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAutenticacionFilter jwtAutenticacionFilter;

    private final Faker faker = new Faker(new Locale("es"));

    private RegistroRequest registroRequestMock;
    private LoginRequest loginRequestMock;
    private LoginResponse loginResponseMock;
    private String correoMock;

    @BeforeEach
    void setUp() {
        correoMock = faker.internet().emailAddress().toLowerCase();
        String claveValida = "ClaveSegura123!";
        String nombreMock = faker.name().firstName();
        String apellidoMock = faker.name().lastName();
        String rutMock = "12.345.678-5";

        registroRequestMock = RegistroRequest.builder()
                .correo(correoMock)
                .clave(claveValida)
                .nombre(nombreMock)
                .apellido(apellidoMock)
                .rut(rutMock)
                .build();

        loginRequestMock = LoginRequest.builder()
                .correo(correoMock)
                .clave(claveValida)
                .build();

        loginResponseMock = LoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock")
                .id(1L)
                .correo(correoMock)
                .nombre(nombreMock)
                .apellido(apellidoMock)
                .rut(rutMock)
                .tipoRol("USUARIO")
                .build();
    }

    // TESTS DE RUTAS PÚBLICAS

    @Test
    @DisplayName("Debe retornar HTTP 201 y el LoginResponse con token al registrar un usuario válido")
    void registrar_Retorna201() throws Exception {
        // GIVEN
        when(authService.registrar(any(RegistroRequest.class))).thenReturn(loginResponseMock);

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequestMock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.correo").value(correoMock))
                .andExpect(jsonPath("$.tipoRol").value("USUARIO"));

        verify(authService).registrar(any(RegistroRequest.class));
    }

    @Test
    @DisplayName("Debe retornar HTTP 400 cuando los datos de registro no cumplen las validaciones (@Valid)")
    void registrar_Retorna400PorValidacionDto() throws Exception {
        // GIVEN: Contraseña débil y RUT con formato inválido
        RegistroRequest requestInvalido = RegistroRequest.builder()
                .correo("correo-invalido")
                .clave("123")
                .nombre("A")
                .apellido("B")
                .rut("123456789")
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, never()).registrar(any(RegistroRequest.class));
    }

    @Test
    @DisplayName("Debe retornar HTTP 409 cuando el correo o RUT ya están registrados")
    void registrar_Retorna409PorConflicto() throws Exception {
        // GIVEN
        when(authService.registrar(any(RegistroRequest.class)))
                .thenThrow(new DataIntegrityViolationException("El correo ya se encuentra registrado"));

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequestMock)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El correo ya se encuentra registrado"));
    }

    @Test
    @DisplayName("Debe retornar HTTP 200 y el token JWT al iniciar sesión con credenciales válidas")
    void login_Retorna200() throws Exception {
        // GIVEN
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponseMock);

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(loginResponseMock.getToken()))
                .andExpect(jsonPath("$.correo").value(correoMock))
                .andExpect(jsonPath("$.tipoRol").value("USUARIO"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Debe retornar HTTP 401 cuando las credenciales de login son incorrectas")
    void login_Retorna401PorCredencialesInvalidas() throws Exception {
        // GIVEN
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestMock)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    // TESTS DE RUTA PROTEGIDA

    @Test
    @DisplayName("Debe retornar HTTP 200 y los datos del perfil del usuario autenticado")
    void obtenerPerfil_Retorna200() throws Exception {
        // GIVEN
        UsernamePasswordAuthenticationToken authMock =
                new UsernamePasswordAuthenticationToken(correoMock, null);
        when(authService.obtenerPerfil(correoMock)).thenReturn(loginResponseMock);

        // WHEN & THEN
        mockMvc.perform(get("/api/auth/perfil")
                        .principal(authMock))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value(correoMock))
                .andExpect(jsonPath("$.rut").value("12.345.678-5"))
                .andExpect(jsonPath("$.tipoRol").value("USUARIO"));

        verify(authService).obtenerPerfil(correoMock);
    }
}