package cl.inmobiliaria.authuser.service;

import cl.inmobiliaria.authuser.model.Favorito;
import cl.inmobiliaria.authuser.model.Usuario;
import cl.inmobiliaria.authuser.repository.FavoritoRepository;
import cl.inmobiliaria.authuser.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Long> obtenerIdsFavoritos(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        return favoritoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(Favorito::getPropiedadId)
                .toList();
    }

    @Transactional
    public boolean toggleFavorito(String correo, Long propiedadId) {
        if (propiedadId == null || propiedadId <= 0) {
            throw new IllegalArgumentException("El ID de la propiedad debe ser válido");
        }
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        boolean yaExiste = favoritoRepository.existsByUsuarioIdAndPropiedadId(usuario.getId(), propiedadId);

        if (yaExiste) {
            favoritoRepository.deleteByUsuarioIdAndPropiedadId(usuario.getId(), propiedadId);
            return false; 
        } else {
            Favorito favorito = Favorito.builder()
                    .usuario(usuario)
                    .propiedadId(propiedadId)
                    .build();
            favoritoRepository.save(favorito);
            return true; 
        }
    }
}