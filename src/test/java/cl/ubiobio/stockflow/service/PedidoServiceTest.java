package cl.ubiobio.stockflow.service;

import cl.ubiobio.stockflow.dto.ItemPedidoRequest;
import cl.ubiobio.stockflow.dto.PedidoRequest;
import cl.ubiobio.stockflow.dto.PedidoResponse;
import cl.ubiobio.stockflow.exception.ConflictoException;
import cl.ubiobio.stockflow.exception.RecursoNoEncontradoException;
import cl.ubiobio.stockflow.exception.StockInsuficienteException;
import cl.ubiobio.stockflow.model.Cliente;
import cl.ubiobio.stockflow.model.EstadoPedido;
import cl.ubiobio.stockflow.model.Pedido;
import cl.ubiobio.stockflow.model.Producto;
import cl.ubiobio.stockflow.repository.ClienteRepository;
import cl.ubiobio.stockflow.repository.MovimientoStockRepository;
import cl.ubiobio.stockflow.repository.PedidoRepository;
import cl.ubiobio.stockflow.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private MovimientoStockRepository movimientoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Cliente cliente;
    private Producto teclado;
    private Producto mouse;

    @BeforeEach
    void preparar() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("María González");
        teclado = producto(10L, "TEC-001", 45990L, 5);
        mouse = producto(20L, "MOU-002", 15990L, 1);
    }

    @Test
    void crearPedidoConStockSuficienteDescuentaStockYQuedaPendiente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findAllByIdParaActualizar(anyCollection())).thenReturn(List.of(teclado, mouse));

        PedidoResponse respuesta = pedidoService.crear(pedido(item(10L, 2), item(20L, 1)));

        assertThat(respuesta.estado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(respuesta.total()).isEqualTo(2 * 45990L + 15990L);
        assertThat(respuesta.detalles()).hasSize(2);
        assertThat(teclado.getStockActual()).isEqualTo(3);
        assertThat(mouse.getStockActual()).isZero();
        verify(pedidoRepository).save(any(Pedido.class));
        verify(movimientoRepository).saveAll(anyIterable());
    }

    @Test
    void crearPedidoConUnProductoSinStockRechazaTodoSinDescontar() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findAllByIdParaActualizar(anyCollection())).thenReturn(List.of(teclado, mouse));

        assertThatThrownBy(() -> pedidoService.crear(pedido(item(10L, 2), item(20L, 3))))
                .isInstanceOf(StockInsuficienteException.class)
                .satisfies(ex -> {
                    var faltantes = ((StockInsuficienteException) ex).getFaltantes();
                    assertThat(faltantes).hasSize(1);
                    assertThat(faltantes.get(0).productoId()).isEqualTo(20L);
                    assertThat(faltantes.get(0).solicitado()).isEqualTo(3);
                    assertThat(faltantes.get(0).disponible()).isEqualTo(1);
                });

        assertThat(teclado.getStockActual()).isEqualTo(5);
        assertThat(mouse.getStockActual()).isEqualTo(1);
        verify(pedidoRepository, never()).save(any());
        verify(movimientoRepository, never()).saveAll(anyIterable());
    }

    @Test
    void crearPedidoSumaCantidadesDeUnMismoProducto() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findAllByIdParaActualizar(anyCollection())).thenReturn(List.of(teclado));

        assertThatThrownBy(() -> pedidoService.crear(pedido(item(10L, 3), item(10L, 3))))
                .isInstanceOf(StockInsuficienteException.class)
                .satisfies(ex -> assertThat(((StockInsuficienteException) ex).getFaltantes().get(0).solicitado())
                        .isEqualTo(6));
        assertThat(teclado.getStockActual()).isEqualTo(5);
    }

    @Test
    void crearPedidoConProductoInexistenteRespondeNoEncontrado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findAllByIdParaActualizar(anyCollection())).thenReturn(List.of(teclado));

        assertThatThrownBy(() -> pedidoService.crear(pedido(item(10L, 1), item(99L, 1))))
                .isInstanceOf(RecursoNoEncontradoException.class);
        assertThat(teclado.getStockActual()).isEqualTo(5);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cancelarPedidoDevuelveElStock() {
        Pedido pedido = new Pedido(cliente);
        pedido.setId(7L);
        teclado.descontarStock(2);
        pedido.agregarDetalle(teclado, 2);
        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findAllByIdParaActualizar(anyCollection())).thenReturn(List.of(teclado));

        PedidoResponse respuesta = pedidoService.cambiarEstado(7L, EstadoPedido.CANCELADO);

        assertThat(respuesta.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(teclado.getStockActual()).isEqualTo(5);
    }

    @Test
    void cambiarEstadoConTransicionInvalidaLanzaConflicto() {
        Pedido pedido = new Pedido(cliente);
        pedido.setEstado(EstadoPedido.ENTREGADO);
        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cambiarEstado(7L, EstadoPedido.CANCELADO))
                .isInstanceOf(ConflictoException.class);
    }

    private Producto producto(Long id, String sku, Long precio, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setSku(sku);
        producto.setNombre(sku);
        producto.setPrecio(precio);
        producto.setStockActual(stock);
        producto.setStockMinimo(0);
        return producto;
    }

    private ItemPedidoRequest item(Long productoId, int cantidad) {
        return new ItemPedidoRequest(productoId, cantidad);
    }

    private PedidoRequest pedido(ItemPedidoRequest... items) {
        return new PedidoRequest(1L, List.of(items));
    }
}
