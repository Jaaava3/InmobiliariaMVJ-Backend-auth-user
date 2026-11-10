package cl.inmobiliaria.authuser.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class FavoritoRequest {
    @NotNull (message = "El ID de la propiedad es obligatorio")
    private Long propiedadId;
}
