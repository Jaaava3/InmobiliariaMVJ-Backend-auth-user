package cl.inmobiliaria.authuser.repository;

import cl.inmobiliaria.authuser.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Pruebas de Repositorio - RolRepository")
class RolRepositoryTest {

    @Autowired
    private RolRepository rolRepository;

    @BeforeEach
    void setUp() {
        rolRepository.save(Rol.builder().tipoRol("ADMIN").build());
        rolRepository.save(Rol.builder().tipoRol("USUARIO").build());
    }

    @Test
    @DisplayName("Debe encontrar y retornar el Rol cuando el tipoRol existe en la base de datos")
    void findByTipoRol_DebeRetornarRolCuandoExiste() {
        // GIVEN: El setup ya guardó los roles ADMIN y USUARIO

        // WHEN
        Optional<Rol> resultado = rolRepository.findByTipoRol("ADMIN");

        // THEN
        assertTrue(resultado.isPresent(), "Debe encontrar el rol ADMIN");
        assertEquals("ADMIN", resultado.get().getTipoRol());
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando se busca un tipoRol que no existe")
    void findByTipoRol_DebeRetornarVacioCuandoNoExiste() {
        // GIVEN
        String rolInexistente = "SUPERVISOR";

        // WHEN
        Optional<Rol> resultado = rolRepository.findByTipoRol(rolInexistente);

        // THEN
        assertFalse(resultado.isPresent(), "Debe retornar vacío para un rol inexistente");
    }

    @Test
    @DisplayName("Debe guardar correctamente un nuevo Rol asignándole un ID autogenerado")
    void save_DebeGuardarNuevoRol() {
        // GIVEN
        Rol nuevoRol = Rol.builder().tipoRol("CORREDOR").build();

        // WHEN
        Rol guardado = rolRepository.save(nuevoRol);

        // THEN
        assertNotNull(guardado.getId());
        assertEquals("CORREDOR", guardado.getTipoRol());
    }
}
