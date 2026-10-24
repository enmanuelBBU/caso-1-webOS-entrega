package cl.ubiobio.stockflow.dto;

import cl.ubiobio.stockflow.model.EstadoPedido;
import cl.ubiobio.stockflow.model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long clienteId,
        String clienteNombre,
        EstadoPedido estado,
        Long total,
        List<DetallePedidoResponse> detalles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static PedidoResponse desde(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getCliente().getId(), pedido.getCliente().getNombre(),
                pedido.getEstado(), pedido.getTotal(),
                pedido.getDetalles().stream().map(DetallePedidoResponse::desde).toList(),
                pedido.getFechaCreacion(), pedido.getFechaActualizacion());
    }
}
