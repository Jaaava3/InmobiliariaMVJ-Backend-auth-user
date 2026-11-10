package cl.inmobiliaria.authuser.repository;

import cl.inmobiliaria.authuser.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByTipoRol(String tipoRol);
}