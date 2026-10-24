package cl.ubiobio.stockflow.dto;

import cl.ubiobio.stockflow.model.DetallePedido;

public record DetallePedidoResponse(
        Long productoId,
        String sku,
        String nombreProducto,
        Integer cantidad,
        Long precioUnitario,
        Long subtotal) {

    public static DetallePedidoResponse desde(DetallePedido detalle) {
        return new DetallePedidoResponse(detalle.getProducto().getId(), detalle.getProducto().getSku(),
                detalle.getProducto().getNombre(), detalle.getCantidad(), detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }
}
