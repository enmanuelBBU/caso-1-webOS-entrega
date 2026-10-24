package cl.ubiobio.stockflow.controller;

import cl.ubiobio.stockflow.dto.ActualizarProductoRequest;
import cl.ubiobio.stockflow.dto.AjusteStockRequest;
import cl.ubiobio.stockflow.dto.MovimientoResponse;
import cl.ubiobio.stockflow.dto.PaginaResponse;
import cl.ubiobio.stockflow.dto.ProductoRequest;
import cl.ubiobio.stockflow.dto.ProductoResponse;
import cl.ubiobio.stockflow.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping
    public PaginaResponse<ProductoResponse> listar(@RequestParam(required = false) String nombre,
                                                   @RequestParam(required = false) String categoria,
                                                   @RequestParam(defaultValue = "true") Boolean activo,
                                                   @RequestParam(required = false) Long precioMin,
                                                   @RequestParam(required = false) Long precioMax,
                                                   @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return productoService.listar(nombre, categoria, activo, precioMin, precioMax, pageable);
    }

    @GetMapping("/stock-bajo")
    public List<ProductoResponse> listarBajoStockMinimo() {
        return productoService.listarBajoStockMinimo();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ProductoResponse ajustarStock(@PathVariable Long id, @Valid @RequestBody AjusteStockRequest request) {
        return productoService.ajustarStock(id, request);
    }

    @GetMapping("/{id}/movimientos")
    public List<MovimientoResponse> listarMovimientos(@PathVariable Long id) {
        return productoService.listarMovimientos(id);
    }
}
