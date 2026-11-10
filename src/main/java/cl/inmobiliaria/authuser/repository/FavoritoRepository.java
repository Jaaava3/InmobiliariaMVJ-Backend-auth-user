package cl.inmobiliaria.authuser.repository;

import cl.inmobiliaria.authuser.model.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioId(Long usuarioId);

    Optional<Favorito> findByUsuarioIdAndPropiedadId(Long usuarioId, Long propiedadId);

    boolean existsByUsuarioIdAndPropiedadId(Long usuarioId, Long propiedadId);

    void deleteByUsuarioIdAndPropiedadId(Long usuarioId, Long propiedadId);
}
