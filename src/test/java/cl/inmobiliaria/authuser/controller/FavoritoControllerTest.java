package cl.inmobiliaria.authuser.controller;

import cl.inmobiliaria.authuser.security.JwtAutenticacionFilter;
import cl.inmobiliaria.authuser.security.JwtService;
import cl.inmobiliaria.authuser.service.FavoritoService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoritoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Pruebas Unitarias - FavoritoController")
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoritoService favoritoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAutenticacionFilter jwtAutenticacionFilter;

    private final Faker faker = new Faker(new Locale("es"));

    private String correoMock;
    private UsernamePasswordAuthenticationToken authMock;

    @BeforeEach
    void setUp() {
        correoMock = faker.internet().emailAddress().toLowerCase();
        authMock = new UsernamePasswordAuthenticationToken(correoMock, null);
    }

    @Test
    @DisplayName("Debe retornar HTTP 200 y la lista de IDs de propiedades favoritas del usuario")
    void obtenerFavoritos_Retorna200() throws Exception {
        // GIVEN
        when(favoritoService.obtenerIdsFavoritos(correoMock)).thenReturn(List.of(101L, 205L, 310L));

        // WHEN & THEN
        mockMvc.perform(get("/api/favoritos")
                        .principal(authMock))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value(101))
                .andExpect(jsonPath("$[1]").value(205))
                .andExpect(jsonPath("$[2]").value(310));

        verify(favoritoService).obtenerIdsFavoritos(correoMock);
    }

    @Test
    @DisplayName("Debe retornar HTTP 200 y true cuando se agrega una propiedad a favoritos")
    void toggleFavorito_RetornaTrueCuandoAgrega() throws Exception {
        // GIVEN
        Long propiedadId = 105L;
        when(favoritoService.toggleFavorito(correoMock, propiedadId)).thenReturn(true);

        // WHEN & THEN
        mockMvc.perform(post("/api/favoritos/{propiedadId}", propiedadId)
                        .principal(authMock))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(favoritoService).toggleFavorito(correoMock, propiedadId);
    }

    @Test
    @DisplayName("Debe retornar HTTP 200 y false cuando se elimina una propiedad de favoritos")
    void toggleFavorito_RetornaFalseCuandoElimina() throws Exception {
        // GIVEN
        Long propiedadId = 105L;
        when(favoritoService.toggleFavorito(correoMock, propiedadId)).thenReturn(false);

        // WHEN & THEN
        mockMvc.perform(post("/api/favoritos/{propiedadId}", propiedadId)
                        .principal(authMock))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(favoritoService).toggleFavorito(correoMock, propiedadId);
    }

    @Test
    @DisplayName("Debe retornar HTTP 400 cuando el ID de propiedad es inválido")
    void toggleFavorito_Retorna400CuandoIdInvalido() throws Exception {
        // GIVEN
        Long propiedadIdInvalido = 0L;
        when(favoritoService.toggleFavorito(correoMock, propiedadIdInvalido))
                .thenThrow(new IllegalArgumentException("El ID de la propiedad debe ser válido"));

        // WHEN & THEN
        mockMvc.perform(post("/api/favoritos/{propiedadId}", propiedadIdInvalido)
                        .principal(authMock))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El ID de la propiedad debe ser válido"));
    }
}
