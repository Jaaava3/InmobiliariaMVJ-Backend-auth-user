package cl.inmobiliaria.authuser.repository;

import cl.inmobiliaria.authuser.model.Rol;
import cl.inmobiliaria.authuser.model.Usuario;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Pruebas de Repositorio - UsuarioRepository")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private final Faker faker = new Faker(new Locale("es"));

    private Rol rolUsuario;
    private String correoExistente;
    private String rutExistente;

    @BeforeEach
    void setUp() {
        rolUsuario = rolRepository.save(Rol.builder().tipoRol("USUARIO").build());

        correoExistente = faker.internet().emailAddress();
        rutExistente = "12.345.678-9";

        Usuario usuario1 = Usuario.builder()
                .correo(correoExistente)
                .claveHash("$2a$10$hashDePrueba1")
                .nombre(faker.name().firstName())
                .apellido(faker.name().lastName())
                .rut(rutExistente)
                .rol(rolUsuario)
                .build();

        Usuario usuario2 = Usuario.builder()
                .correo(faker.internet().emailAddress())
                .claveHash("$2a$10$hashDePrueba2")
                .nombre(faker.name().firstName())
                .apellido(faker.name().lastName())
                .rut("9.876.543-2")
                .rol(rolUsuario)
                .build();

        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);
    }

    @Test
    @DisplayName("Debe encontrar y retornar el Usuario cuando el correo existe")
    void findByCorreo_DebeRetornarUsuarioCuandoCorreoExiste() {
        // WHEN
        Optional<Usuario> resultado = usuarioRepository.findByCorreo(correoExistente);

        // THEN
        assertTrue(resultado.isPresent(), "Debe encontrar el usuario con el correo registrado");
        assertEquals(correoExistente, resultado.get().getCorreo());
        assertEquals(rutExistente, resultado.get().getRut());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando se busca un correo no registrado")
    void findByCorreo_DebeRetornarVacioCuandoCorreoNoExiste() {
        // WHEN
        Optional<Usuario> resultado = usuarioRepository.findByCorreo("noexiste@inmobiliaria.cl");

        // THEN
        assertFalse(resultado.isPresent(), "Debe retornar vacío para un correo inexistente");
    }

    @Test
    @DisplayName("Debe retornar true al verificar existencia de un correo registrado")
    void existsByCorreo_DebeRetornarTrueCuandoCorreoExiste() {
        // WHEN
        boolean existe = usuarioRepository.existsByCorreo(correoExistente);

        // THEN
        assertTrue(existe, "Debe retornar true para un correo ya registrado");
    }

    @Test
    @DisplayName("Debe retornar false al verificar existencia de un correo no registrado")
    void existsByCorreo_DebeRetornarFalseCuandoCorreoNoExiste() {
        // WHEN
        boolean existe = usuarioRepository.existsByCorreo("fantasma@inmobiliaria.cl");

        // THEN
        assertFalse(existe, "Debe retornar false para un correo no registrado");
    }

    @Test
    @DisplayName("Debe retornar true al verificar existencia de un RUT registrado")
    void existsByRut_DebeRetornarTrueCuandoRutExiste() {
        // WHEN
        boolean existe = usuarioRepository.existsByRut(rutExistente);

        // THEN
        assertTrue(existe, "Debe retornar true para un RUT ya registrado");
    }

    @Test
    @DisplayName("Debe retornar false al verificar existencia de un RUT no registrado")
    void existsByRut_DebeRetornarFalseCuandoRutNoExiste() {
        // WHEN
        boolean existe = usuarioRepository.existsByRut("11.111.111-1");

        // THEN
        assertFalse(existe, "Debe retornar false para un RUT no registrado");
    }

    @Test
    @DisplayName("Debe guardar correctamente un nuevo Usuario con su Rol asignado")
    void save_DebeGuardarUsuarioConRolAsignado() {
        // GIVEN
        String nuevoCorreo = faker.internet().emailAddress();
        Usuario nuevo = Usuario.builder()
                .correo(nuevoCorreo)
                .claveHash("$2a$10$nuevoHash")
                .nombre("Camila")
                .apellido("Soto")
                .rut("15.432.198-K")
                .rol(rolUsuario)
                .build();

        // WHEN
        Usuario guardado = usuarioRepository.save(nuevo);

        // THEN
        assertNotNull(guardado.getId());
        assertEquals(nuevoCorreo, guardado.getCorreo());
        assertEquals("USUARIO", guardado.getRol().getTipoRol());
    }
}
