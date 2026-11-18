package cl.inmobiliaria.authuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Debe ingresar un correo válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 10, max = 100, message = "La contraseña debe tener entre 10 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{10,}$",
        message = "La contraseña debe tener al menos 10 caracteres, una mayúscula, una minúscula y un carácter especial"
    )
    private String clave;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 60, message = "El apellido debe tener entre 2 y 60 caracteres")
    private String apellido;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(
        regexp = "^\\d{1,2}\\.\\d{3}\\.\\d{3}-[0-9kK]$",
        message = "El RUT debe tener formato válido (ej: 12.345.678-9)"
    )
    private String rut;
}