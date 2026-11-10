package cl.inmobiliaria.authuser.controller;

import cl.inmobiliaria.authuser.service.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    @GetMapping
    public ResponseEntity<List<Long>> obtenerFavoritos(Authentication authentication) {
        String correo = authentication.getName();
        List<Long> favoritos = favoritoService.obtenerIdsFavoritos(correo);
        return ResponseEntity.ok(favoritos);
    }

    @PostMapping("/{propiedadId}")
    public ResponseEntity<Boolean> toggleFavorito(@PathVariable Long propiedadId, Authentication authentication) {
        String correo = authentication.getName();
        boolean agregado = favoritoService.toggleFavorito(correo, propiedadId);
        return ResponseEntity.ok(agregado);
    }
}