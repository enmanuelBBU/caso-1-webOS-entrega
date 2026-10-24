package cl.ubiobio.stockflow.service;

import cl.ubiobio.stockflow.dto.FaltanteStock;
import cl.ubiobio.stockflow.dto.ItemPedidoRequest;
import cl.ubiobio.stockflow.dto.PaginaResponse;
import cl.ubiobio.stockflow.dto.PedidoRequest;
import cl.ubiobio.stockflow.dto.PedidoResponse;
import cl.ubiobio.stockflow.exception.ConflictoException;
import cl.ubiobio.stockflow.exception.RecursoNoEncontradoException;
import cl.ubiobio.stockflow.exception.SolicitudInvalidaException;
import cl.ubiobio.stockflow.exception.StockInsuficienteException;
import cl.ubiobio.stockflow.model.Cliente;
import cl.ubiobio.stockflow.model.DetallePedido;
import cl.ubiobio.stockflow.model.EstadoPedido;
import cl.ubiobio.stockflow.model.MovimientoStock;
import cl.ubiobio.stockflow.model.Pedido;
import cl.ubiobio.stockflow.model.Producto;
import cl.ubiobio.stockflow.model.TipoMovimiento;
import cl.ubiobio.stockflow.repository.ClienteRepository;
import cl.ubiobio.stockflow.repository.Especificaciones;
import cl.ubiobio.stockflow.repository.MovimientoStockRepository;
import cl.ubiobio.stockflow.repository.PedidoRepository;
import cl.ubiobio.stockflow.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;

    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", request.clienteId()));

        Map<Long, Integer> cantidades = agruparCantidades(request.items());
        Map<Long, Producto> productos = bloquearProductos(cantidades.keySet());
        validarDisponibilidad(cantidades, productos);

        Pedido pedido = new Pedido(cliente);
        cantidades.forEach((productoId, cantidad) -> {
            Producto producto = productos.get(productoId);
            producto.descontarStock(cantidad);
            pedido.agregarDetalle(producto, cantidad);
        });
        pedidoRepository.save(pedido);

        movimientoRepository.saveAll(pedido.getDetalles().stream()
                .map(detalle -> new MovimientoStock(detalle.getProducto(), TipoMovimiento.SALIDA,
                        -detalle.getCantidad(), pedido, "Venta"))
                .toList());
        return PedidoResponse.desde(pedido);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoResponse> listar(EstadoPedido estado, Long clienteId, LocalDate desde,
                                                 LocalDate hasta, Pageable pageable) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        var especificacion = Especificaciones.pedidos(estado, clienteId, desde, hasta);
        return PaginaResponse.desde(pedidoRepository.findAll(especificacion, pageable).map(PedidoResponse::desde));
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        return pedidoRepository.findConDetallesById(id)
                .map(PedidoResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException("Cliente", clienteId);
        }
        return pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        if (!pedido.getEstado().puedeCambiarA(nuevoEstado)) {
            throw new ConflictoException("No se puede cambiar un pedido de " + pedido.getEstado() + " a " + nuevoEstado);
        }
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            devolverStock(pedido);
        }
        pedido.setEstado(nuevoEstado);
        pedidoRepository.flush();
        return PedidoResponse.desde(pedido);
    }

    private void devolverStock(Pedido pedido) {
        List<Long> ids = pedido.getDetalles().stream().map(detalle -> detalle.getProducto().getId()).toList();
        Map<Long, Producto> productos = bloquearProductos(ids);
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = productos.get(detalle.getProducto().getId());
            producto.reponerStock(detalle.getCantidad());
            movimientoRepository.save(new MovimientoStock(producto, TipoMovimiento.ENTRADA, detalle.getCantidad(),
                    pedido, "Cancelación del pedido " + pedido.getId()));
        }
    }

    private Map<Long, Integer> agruparCantidades(List<ItemPedidoRequest> items) {
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        items.forEach(item -> cantidades.merge(item.productoId(), item.cantidad(), Integer::sum));
        return cantidades;
    }

    private Map<Long, Producto> bloquearProductos(Collection<Long> ids) {
        return productoRepository.findAllByIdParaActualizar(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
    }

    private void validarDisponibilidad(Map<Long, Integer> cantidades, Map<Long, Producto> productos) {
        List<FaltanteStock> faltantes = new ArrayList<>();
        cantidades.forEach((productoId, cantidad) -> {
            Producto producto = productos.get(productoId);
            if (producto == null) {
                throw new RecursoNoEncontradoException("Producto", productoId);
            }
            if (!producto.getActivo()) {
                throw new ConflictoException("El producto " + producto.getSku() + " no está disponible para la venta");
            }
            if (!producto.tieneStock(cantidad)) {
                faltantes.add(new FaltanteStock(productoId, producto.getNombre(), cantidad, producto.getStockActual()));
            }
        });
        if (!faltantes.isEmpty()) {
            throw new StockInsuficienteException(faltantes);
        }
    }
}
