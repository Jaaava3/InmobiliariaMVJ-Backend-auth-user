package cl.inmobiliaria.authuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class LoginResponse {
    private String token;
    private Long id;
    private String correo;
    private String nombre;
    private String apellido;
    private String rut;
    private String tipoRol; 
}
