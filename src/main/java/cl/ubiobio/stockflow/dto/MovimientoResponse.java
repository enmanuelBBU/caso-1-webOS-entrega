package cl.ubiobio.stockflow.dto;

import cl.ubiobio.stockflow.model.MovimientoStock;
import cl.ubiobio.stockflow.model.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        Long productoId,
        TipoMovimiento tipo,
        Integer cantidad,
        Integer stockResultante,
        Long pedidoId,
        String motivo,
        LocalDateTime fecha) {

    public static MovimientoResponse desde(MovimientoStock movimiento) {
        Long pedidoId = movimiento.getPedido() != null ? movimiento.getPedido().getId() : null;
        return new MovimientoResponse(movimiento.getId(), movimiento.getProducto().getId(), movimiento.getTipo(),
                movimiento.getCantidad(), movimiento.getStockResultante(), pedidoId, movimiento.getMotivo(),
                movimiento.getFecha());
    }
}
