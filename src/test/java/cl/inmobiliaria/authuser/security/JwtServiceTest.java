package cl.inmobiliaria.authuser.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET_KEY_TEST =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY_TEST);
    }

    @Test
    @DisplayName("Debe generar un token JWT válido y extraer correctamente el correo y el rol")
    void generarYExtraerClaims_Exito() {
        String correo = "usuario@mvj.cl";
        String rol = "ADMIN";

        String token = jwtService.generarToken(correo, rol);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(correo, jwtService.extraerCorreo(token));
        assertEquals(rol, jwtService.extraerRol(token));
    }

    @Test
    @DisplayName("Debe retornar true cuando el token pertenece al correo y no ha expirado")
    void esTokenValido_RetornaTrue() {
        String correo = "cliente@gmail.com";
        String token = jwtService.generarToken(correo, "USUARIO");

        assertTrue(jwtService.esTokenValido(token, correo));
    }

    @Test
    @DisplayName("Debe retornar false cuando el correo del token no coincide con el esperado")
    void esTokenValido_RetornaFalsePorCorreoDistinto() {
        String token = jwtService.generarToken("cliente@gmail.com", "USUARIO");

        assertFalse(jwtService.esTokenValido(token, "otro@gmail.com"));
    }

    @Test
    @DisplayName("Debe lanzar ExpiredJwtException cuando el token ya expiró")
    void validarToken_FallaPorTokenExpirado() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY_TEST);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String tokenExpirado = Jwts.builder()
                .setSubject("expirado@gmail.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 20_000))
                .setExpiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(ExpiredJwtException.class,
                () -> jwtService.esTokenValido(tokenExpirado, "expirado@gmail.com"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el token está malformado o alterado")
    void validarToken_FallaPorTokenMalformado() {
        assertThrows(MalformedJwtException.class,
                () -> jwtService.extraerCorreo("token.invalido.alterado"));
    }
}
