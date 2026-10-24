package cl.ubiobio.stockflow.dto;

import cl.ubiobio.stockflow.model.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AjusteStockRequest(
        @NotNull TipoMovimiento tipo,
        @NotNull @PositiveOrZero Integer cantidad,
        @Size(max = 200) String motivo) {
}
