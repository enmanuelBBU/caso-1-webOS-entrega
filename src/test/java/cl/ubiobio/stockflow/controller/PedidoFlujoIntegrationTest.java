package cl.ubiobio.stockflow.controller;

import cl.ubiobio.stockflow.model.Cliente;
import cl.ubiobio.stockflow.model.Producto;
import cl.ubiobio.stockflow.repository.ClienteRepository;
import cl.ubiobio.stockflow.repository.MovimientoStockRepository;
import cl.ubiobio.stockflow.repository.PedidoRepository;
import cl.ubiobio.stockflow.repository.ProductoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoFlujoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private MovimientoStockRepository movimientoRepository;

    private Long clienteId;
    private Long tecladoId;
    private Long monitorId;

    @BeforeEach
    void preparar() {
        movimientoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        clienteRepository.deleteAll();

        Cliente cliente = new Cliente();
        cliente.setRut("12345678-5");
        cliente.setNombre("María González");
        cliente.setEmail("maria@correo.cl");
        clienteId = clienteRepository.save(cliente).getId();

        tecladoId = productoRepository.save(producto("TEC-001", "Teclado", 45990L, 10, 2)).getId();
        monitorId = productoRepository.save(producto("MON-003", "Monitor", 219990L, 3, 5)).getId();
    }

    @Test
    void registrarPedidoConStockSuficienteCreaPendienteYDescuentaStock() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson(tecladoId, 2, monitorId, 1)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(2 * 45990 + 219990))
                .andExpect(jsonPath("$.detalles.length()").value(2));

        assertThat(stockDe(tecladoId)).isEqualTo(8);
        assertThat(stockDe(monitorId)).isEqualTo(2);
        assertThat(movimientoRepository.count()).isEqualTo(2);
    }

    @Test
    void registrarPedidoConUnProductoSinStockRechazaTodoYNoDescuentaNada() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson(tecladoId, 2, monitorId, 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles.length()").value(1))
                .andExpect(jsonPath("$.detalles[0].productoId").value(monitorId))
                .andExpect(jsonPath("$.detalles[0].solicitado").value(5))
                .andExpect(jsonPath("$.detalles[0].disponible").value(3));

        assertThat(stockDe(tecladoId)).isEqualTo(10);
        assertThat(stockDe(monitorId)).isEqualTo(3);
        assertThat(pedidoRepository.count()).isZero();
        assertThat(movimientoRepository.count()).isZero();
    }

    @Test
    void registrarPedidoConItemsVaciosRespondeBadRequest() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": " + clienteId + ", \"items\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.items").exists());
    }

    @Test
    void cancelarPedidoDevuelveElStock() throws Exception {
        Long pedidoId = registrarPedido(tecladoId, 4, monitorId, 1);
        assertThat(stockDe(tecladoId)).isEqualTo(6);

        mockMvc.perform(patch("/api/pedidos/{id}/estado", pedidoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"CANCELADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        assertThat(stockDe(tecladoId)).isEqualTo(10);
        assertThat(stockDe(monitorId)).isEqualTo(3);
    }

    @Test
    void listarPedidosDeUnCliente() throws Exception {
        registrarPedido(tecladoId, 1, monitorId, 1);

        mockMvc.perform(get("/api/clientes/{id}/pedidos", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(clienteId));
    }

    @Test
    void listarProductosBajoStockMinimo() throws Exception {
        mockMvc.perform(get("/api/productos/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sku").value("MON-003"));
    }

    @Test
    void reporteDeVentasExcluyePedidosCancelados() throws Exception {
        registrarPedido(tecladoId, 2, monitorId, 1);
        Long cancelado = registrarPedido(tecladoId, 1, monitorId, 1);
        mockMvc.perform(patch("/api/pedidos/{id}/estado", cancelado)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\": \"CANCELADO\"}"));

        String hoy = LocalDate.now().toString();
        mockMvc.perform(get("/api/reportes/ventas").param("desde", hoy).param("hasta", hoy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadPedidos").value(1))
                .andExpect(jsonPath("$.unidadesVendidas").value(3))
                .andExpect(jsonPath("$.montoTotal").value(2 * 45990 + 219990));
    }

    private Long registrarPedido(Long productoA, int cantidadA, Long productoB, int cantidadB) throws Exception {
        String respuesta = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson(productoA, cantidadA, productoB, cantidadB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private String pedidoJson(Long productoA, int cantidadA, Long productoB, int cantidadB) {
        return """
                {
                  "clienteId": %d,
                  "items": [
                    {"productoId": %d, "cantidad": %d},
                    {"productoId": %d, "cantidad": %d}
                  ]
                }
                """.formatted(clienteId, productoA, cantidadA, productoB, cantidadB);
    }

    private int stockDe(Long productoId) {
        return productoRepository.findById(productoId).orElseThrow().getStockActual();
    }

    private Producto producto(String sku, String nombre, Long precio, int stock, int stockMinimo) {
        Producto producto = new Producto();
        producto.setSku(sku);
        producto.setNombre(nombre);
        producto.setCategoria("General");
        producto.setPrecio(precio);
        producto.setStockActual(stock);
        producto.setStockMinimo(stockMinimo);
        return producto;
    }
}
