package cl.inmobiliaria.authuser.repository;

import cl.inmobiliaria.authuser.model.Favorito;
import cl.inmobiliaria.authuser.model.Rol;
import cl.inmobiliaria.authuser.model.Usuario;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Pruebas de Repositorio - FavoritoRepository")
class FavoritoRepositoryTest {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private final Faker faker = new Faker(new Locale("es"));

    private Usuario usuarioGuardado;
    private final Long propiedadIdExistente = 10L;

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.save(Rol.builder().tipoRol("USUARIO").build());

        usuarioGuardado = usuarioRepository.save(Usuario.builder()
                .correo(faker.internet().emailAddress())
                .claveHash("$2a$10$hashFavoritos")
                .nombre(faker.name().firstName())
                .apellido(faker.name().lastName())
                .rut("14.567.890-3")
                .rol(rol)
                .build());

        favoritoRepository.save(Favorito.builder()
                .usuario(usuarioGuardado)
                .propiedadId(propiedadIdExistente)
                .build());

        favoritoRepository.save(Favorito.builder()
                .usuario(usuarioGuardado)
                .propiedadId(20L)
                .build());
    }

    @Test
    @DisplayName("Debe retornar todos los favoritos asociados a un usuario")
    void findByUsuarioId_DebeRetornarListaFavoritos() {
        // WHEN
        List<Favorito> favoritos = favoritoRepository.findByUsuarioId(usuarioGuardado.getId());

        // THEN
        assertEquals(2, favoritos.size(), "El usuario debe tener 2 propiedades favoritas guardadas");
    }

    @Test
    @DisplayName("Debe encontrar un favorito específico por usuarioId y propiedadId")
    void findByUsuarioIdAndPropiedadId_DebeRetornarFavoritoCuandoExiste() {
        // WHEN
        Optional<Favorito> resultado = favoritoRepository.findByUsuarioIdAndPropiedadId(
                usuarioGuardado.getId(), propiedadIdExistente);

        // THEN
        assertTrue(resultado.isPresent());
        assertEquals(propiedadIdExistente, resultado.get().getPropiedadId());
    }

    @Test
    @DisplayName("Debe retornar true si la propiedad ya está en favoritos del usuario")
    void existsByUsuarioIdAndPropiedadId_DebeRetornarTrueCuandoExiste() {
        // WHEN
        boolean existe = favoritoRepository.existsByUsuarioIdAndPropiedadId(
                usuarioGuardado.getId(), propiedadIdExistente);

        // THEN
        assertTrue(existe);
    }

    @Test
    @DisplayName("Debe retornar false si la propiedad no está en favoritos del usuario")
    void existsByUsuarioIdAndPropiedadId_DebeRetornarFalseCuandoNoExiste() {
        // WHEN
        boolean existe = favoritoRepository.existsByUsuarioIdAndPropiedadId(
                usuarioGuardado.getId(), 999L);

        // THEN
        assertFalse(existe);
    }

    @Test
    @DisplayName("Debe eliminar el favorito indicado por usuarioId y propiedadId")
    void deleteByUsuarioIdAndPropiedadId_DebeEliminarFavorito() {
        // GIVEN
        assertTrue(favoritoRepository.existsByUsuarioIdAndPropiedadId(
                usuarioGuardado.getId(), propiedadIdExistente));

        // WHEN
        favoritoRepository.deleteByUsuarioIdAndPropiedadId(
                usuarioGuardado.getId(), propiedadIdExistente);

        // THEN
        assertFalse(favoritoRepository.existsByUsuarioIdAndPropiedadId(
                usuarioGuardado.getId(), propiedadIdExistente));
        assertEquals(1, favoritoRepository.findByUsuarioId(usuarioGuardado.getId()).size());
    }
}
