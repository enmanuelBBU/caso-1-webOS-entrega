package cl.ubiobio.stockflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "debe tener el formato 12345678-9") String rut,
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String telefono,
        @Size(max = 250) String direccion) {
}
