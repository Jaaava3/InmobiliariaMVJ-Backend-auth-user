package cl.inmobiliaria.authuser.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - JwtAutenticacionFilter")
class JwtAutenticacionFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAutenticacionFilter jwtAutenticacionFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe continuar la cadena sin autenticar cuando no hay cabecera Authorization")
    void doFilterInternal_SinCabeceraAuthorization() throws ServletException, IOException {
        jwtAutenticacionFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Debe continuar la cadena sin autenticar cuando la cabecera no empieza con Bearer")
    void doFilterInternal_CabeceraSinPrefijoBearer() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic abc12345");

        jwtAutenticacionFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Debe autenticar al usuario en SecurityContextHolder cuando el token Bearer es válido")
    void doFilterInternal_TokenValido() throws ServletException, IOException {
        String token = "jwt.valido.123";
        String correo = "admin@mvj.cl";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extraerCorreo(token)).thenReturn(correo);
        when(jwtService.extraerRol(token)).thenReturn("ADMIN");
        when(jwtService.esTokenValido(token, correo)).thenReturn(true);

        jwtAutenticacionFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(correo, auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("No debe autenticar cuando esTokenValido retorna false")
    void doFilterInternal_TokenNoValido() throws ServletException, IOException {
        String token = "jwt.no.valido";
        String correo = "cliente@gmail.com";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extraerCorreo(token)).thenReturn(correo);
        when(jwtService.extraerRol(token)).thenReturn("USUARIO");
        when(jwtService.esTokenValido(token, correo)).thenReturn(false);

        jwtAutenticacionFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe capturar excepciones de tokens expirados o corruptos sin romper el filtro")
    void doFilterInternal_TokenLanzaExcepcion() throws ServletException, IOException {
        String token = "jwt.corrupto";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extraerCorreo(token)).thenThrow(new RuntimeException("Token expirado"));

        jwtAutenticacionFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
