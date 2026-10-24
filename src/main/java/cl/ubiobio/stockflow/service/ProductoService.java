package cl.ubiobio.stockflow.service;

import cl.ubiobio.stockflow.dto.ActualizarProductoRequest;
import cl.ubiobio.stockflow.dto.AjusteStockRequest;
import cl.ubiobio.stockflow.dto.MovimientoResponse;
import cl.ubiobio.stockflow.dto.PaginaResponse;
import cl.ubiobio.stockflow.dto.ProductoRequest;
import cl.ubiobio.stockflow.dto.ProductoResponse;
import cl.ubiobio.stockflow.exception.ConflictoException;
import cl.ubiobio.stockflow.exception.RecursoNoEncontradoException;
import cl.ubiobio.stockflow.exception.SolicitudInvalidaException;
import cl.ubiobio.stockflow.model.MovimientoStock;
import cl.ubiobio.stockflow.model.Producto;
import cl.ubiobio.stockflow.model.TipoMovimiento;
import cl.ubiobio.stockflow.repository.DetallePedidoRepository;
import cl.ubiobio.stockflow.repository.Especificaciones;
import cl.ubiobio.stockflow.repository.MovimientoStockRepository;
import cl.ubiobio.stockflow.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (productoRepository.existsBySku(request.sku())) {
            throw new ConflictoException("Ya existe un producto con SKU " + request.sku());
        }
        Producto producto = new Producto();
        producto.setSku(request.sku());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setCategoria(request.categoria());
        producto.setPrecio(request.precio());
        producto.setStockActual(request.stockActual());
        producto.setStockMinimo(request.stockMinimo());
        productoRepository.save(producto);

        if (producto.getStockActual() > 0) {
            movimientoRepository.save(new MovimientoStock(producto, TipoMovimiento.ENTRADA,
                    producto.getStockActual(), null, "Stock inicial"));
        }
        return ProductoResponse.desde(producto);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProductoResponse> listar(String nombre, String categoria, Boolean activo,
                                                   Long precioMin, Long precioMax, Pageable pageable) {
        if (precioMin != null && precioMax != null && precioMin > precioMax) {
            throw new SolicitudInvalidaException("precioMin no puede ser mayor que precioMax");
        }
        var especificacion = Especificaciones.productos(nombre, categoria, activo, precioMin, precioMax);
        return PaginaResponse.desde(productoRepository.findAll(especificacion, pageable).map(ProductoResponse::desde));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.desde(buscar(id));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ActualizarProductoRequest request) {
        Producto producto = buscar(id);
        if (productoRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new ConflictoException("Ya existe otro producto con SKU " + request.sku());
        }
        producto.setSku(request.sku());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setCategoria(request.categoria());
        producto.setPrecio(request.precio());
        producto.setStockMinimo(request.stockMinimo());
        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }
        productoRepository.flush();
        return ProductoResponse.desde(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        boolean tieneHistorial = detallePedidoRepository.existsByProductoId(id)
                || movimientoRepository.existsByProductoId(id);
        if (tieneHistorial) {
            producto.setActivo(false);
        } else {
            productoRepository.delete(producto);
        }
    }

    @Transactional
    public ProductoResponse ajustarStock(Long id, AjusteStockRequest request) {
        Producto producto = productoRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        int cantidad = request.cantidad();
        String motivo = request.motivo();

        switch (request.tipo()) {
            case ENTRADA -> {
                if (cantidad == 0) {
                    throw new SolicitudInvalidaException("La cantidad de una entrada debe ser mayor a 0");
                }
                producto.reponerStock(cantidad);
                movimientoRepository.save(new MovimientoStock(producto, TipoMovimiento.ENTRADA, cantidad, null,
                        motivo != null ? motivo : "Reposición de stock"));
            }
            case AJUSTE -> {
                int diferencia = cantidad - producto.getStockActual();
                producto.setStockActual(cantidad);
                movimientoRepository.save(new MovimientoStock(producto, TipoMovimiento.AJUSTE, diferencia, null,
                        motivo != null ? motivo : "Ajuste de inventario"));
            }
            case SALIDA -> throw new SolicitudInvalidaException("Las salidas de stock se registran mediante pedidos");
        }
        productoRepository.flush();
        return ProductoResponse.desde(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarBajoStockMinimo() {
        return productoRepository.findBajoStockMinimo().stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientos(Long id) {
        buscar(id);
        return movimientoRepository.findByProductoIdOrderByFechaDescIdDesc(id).stream()
                .map(MovimientoResponse::desde)
                .toList();
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
    }
}
