package cl.ubiobio.stockflow.dto;

import java.time.LocalDate;

public record ReporteVentasResponse(
        LocalDate desde,
        LocalDate hasta,
        long cantidadPedidos,
        long unidadesVendidas,
        long montoTotal) {
}
