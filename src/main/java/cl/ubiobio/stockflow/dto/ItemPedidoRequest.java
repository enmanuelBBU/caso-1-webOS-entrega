package cl.ubiobio.stockflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequest(
        @NotNull @Positive Long productoId,
        @NotNull @Positive Integer cantidad) {
}
