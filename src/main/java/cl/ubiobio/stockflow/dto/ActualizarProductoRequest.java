package cl.ubiobio.stockflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ActualizarProductoRequest(
        @NotBlank @Size(max = 50) String sku,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion,
        @NotBlank @Size(max = 80) String categoria,
        @NotNull @PositiveOrZero Long precio,
        @NotNull @PositiveOrZero Integer stockMinimo,
        Boolean activo) {
}
