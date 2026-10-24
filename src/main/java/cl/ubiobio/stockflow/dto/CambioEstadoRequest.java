package cl.ubiobio.stockflow.dto;

import cl.ubiobio.stockflow.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull EstadoPedido estado) {
}
