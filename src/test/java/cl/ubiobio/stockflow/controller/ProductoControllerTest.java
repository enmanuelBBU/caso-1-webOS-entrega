package cl.ubiobio.stockflow.controller;

import cl.ubiobio.stockflow.dto.ProductoResponse;
import cl.ubiobio.stockflow.exception.RecursoNoEncontradoException;
import cl.ubiobio.stockflow.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void crearConDatosInvalidosRespondeBadRequest() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": \"\", \"nombre\": \"Teclado\", \"categoria\": \"Periféricos\", "
                                + "\"precio\": -1, \"stockActual\": 5, \"stockMinimo\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.sku").exists())
                .andExpect(jsonPath("$.detalles.precio").exists());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearValidoRespondeCreatedConLocation() throws Exception {
        when(productoService.crear(any())).thenReturn(new ProductoResponse(1L, "TEC-001", "Teclado", null,
                "Periféricos", 45990L, 5, 1, true, false, null, null));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": \"TEC-001\", \"nombre\": \"Teclado\", \"categoria\": \"Periféricos\", "
                                + "\"precio\": 45990, \"stockActual\": 5, \"stockMinimo\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/productos/1")))
                .andExpect(jsonPath("$.sku").value("TEC-001"));
    }

    @Test
    void obtenerProductoInexistenteRespondeNotFound() throws Exception {
        when(productoService.obtener(99L)).thenThrow(new RecursoNoEncontradoException("Producto", 99L));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Producto con id 99 no existe"));
    }
}
