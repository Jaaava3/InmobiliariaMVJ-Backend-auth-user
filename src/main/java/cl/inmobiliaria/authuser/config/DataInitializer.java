package cl.inmobiliaria.authuser.config;

import cl.inmobiliaria.authuser.model.Rol;
import cl.inmobiliaria.authuser.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        if (rolRepository.findByTipoRol("ADMIN").isEmpty()) {
            rolRepository.save(Rol.builder().tipoRol("ADMIN").build());
        }
        if (rolRepository.findByTipoRol("USUARIO").isEmpty()) {
            rolRepository.save(Rol.builder().tipoRol("USUARIO").build());
        }
    }
}
