package cl.inmobiliaria.authuser.service;

import cl.inmobiliaria.authuser.model.Favorito;
import cl.inmobiliaria.authuser.model.Rol;
import cl.inmobiliaria.authuser.model.Usuario;
import cl.inmobiliaria.authuser.repository.FavoritoRepository;
import cl.inmobiliaria.authuser.repository.UsuarioRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - FavoritoService")
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    private final Faker faker = new Faker(new Locale("es"));

    private Usuario usuarioMock;
    private String correoMock;
    private Long usuarioIdMock;

    @BeforeEach
    void setUp() {
        usuarioIdMock = 1L;
        correoMock = faker.internet().emailAddress().toLowerCase();

        Rol rolMock = Rol.builder().id(2L).tipoRol("USUARIO").build();

        usuarioMock = Usuario.builder()
                .id(usuarioIdMock)
                .correo(correoMock)
                .claveHash("$2a$10$hash")
                .nombre(faker.name().firstName())
                .apellido(faker.name().lastName())
                .rut("12.345.678-5")
                .rol(rolMock)
                .build();
    }

    // FLUJOS DE: obtenerIdsFavoritos()

    @Test
    @DisplayName("Debe retornar la lista de IDs de propiedades favoritas del usuario")
    void obtenerIdsFavoritos_Exito() {
        // GIVEN
        Favorito fav1 = Favorito.builder().id(1L).usuario(usuarioMock).propiedadId(101L).build();
        Favorito fav2 = Favorito.builder().id(2L).usuario(usuarioMock).propiedadId(205L).build();

        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.of(usuarioMock));
        when(favoritoRepository.findByUsuarioId(usuarioIdMock)).thenReturn(List.of(fav1, fav2));

        // WHEN
        List<Long> resultado = favoritoService.obtenerIdsFavoritos(correoMock);

        // THEN
        assertEquals(2, resultado.size());
        assertEquals(List.of(101L, 205L), resultado);
        verify(favoritoRepository).findByUsuarioId(usuarioIdMock);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si el usuario no existe al consultar favoritos")
    void obtenerIdsFavoritos_UsuarioNoEncontrado() {
        // GIVEN
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(NoSuchElementException.class,
                () -> favoritoService.obtenerIdsFavoritos(correoMock));

        verify(favoritoRepository, never()).findByUsuarioId(any());
    }

    // FLUJOS DE: toggleFavorito()

    @Test
    @DisplayName("Debe agregar la propiedad a favoritos y retornar true cuando aún no estaba guardada")
    void toggleFavorito_AgregaCuandoNoExiste() {
        // GIVEN
        Long propiedadId = 105L;
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.of(usuarioMock));
        when(favoritoRepository.existsByUsuarioIdAndPropiedadId(usuarioIdMock, propiedadId)).thenReturn(false);

        // WHEN
        boolean agregado = favoritoService.toggleFavorito(correoMock, propiedadId);

        // THEN
        assertTrue(agregado, "Debe retornar true al agregar la propiedad a favoritos");
        verify(favoritoRepository).save(any(Favorito.class));
        verify(favoritoRepository, never()).deleteByUsuarioIdAndPropiedadId(any(), any());
    }

    @Test
    @DisplayName("Debe eliminar la propiedad de favoritos y retornar false cuando ya estaba guardada")
    void toggleFavorito_EliminaCuandoYaExiste() {
        // GIVEN
        Long propiedadId = 105L;
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.of(usuarioMock));
        when(favoritoRepository.existsByUsuarioIdAndPropiedadId(usuarioIdMock, propiedadId)).thenReturn(true);

        // WHEN
        boolean agregado = favoritoService.toggleFavorito(correoMock, propiedadId);

        // THEN
        assertFalse(agregado, "Debe retornar false al eliminar la propiedad de favoritos");
        verify(favoritoRepository).deleteByUsuarioIdAndPropiedadId(usuarioIdMock, propiedadId);
        verify(favoritoRepository, never()).save(any(Favorito.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el ID de la propiedad es nulo")
    void toggleFavorito_FallaPorPropiedadIdNulo() {
        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> favoritoService.toggleFavorito(correoMock, null));

        verify(usuarioRepository, never()).findByCorreo(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el ID de la propiedad es menor o igual a cero")
    void toggleFavorito_FallaPorPropiedadIdInvalido() {
        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> favoritoService.toggleFavorito(correoMock, 0L));

        assertThrows(IllegalArgumentException.class,
                () -> favoritoService.toggleFavorito(correoMock, -10L));

        verify(usuarioRepository, never()).findByCorreo(any());
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si el usuario no existe al alternar favorito")
    void toggleFavorito_UsuarioNoEncontrado() {
        // GIVEN
        when(usuarioRepository.findByCorreo(correoMock)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(NoSuchElementException.class,
                () -> favoritoService.toggleFavorito(correoMock, 105L));

        verify(favoritoRepository, never()).save(any());
    }
}