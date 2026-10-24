package cl.ubiobio.stockflow.dto;

import cl.ubiobio.stockflow.model.Producto;

import java.time.LocalDateTime;

public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        String categoria,
        Long precio,
        Integer stockActual,
        Integer stockMinimo,
        Boolean activo,
        boolean bajoStockMinimo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getSku(), producto.getNombre(),
                producto.getDescripcion(), producto.getCategoria(), producto.getPrecio(),
                producto.getStockActual(), producto.getStockMinimo(), producto.getActivo(),
                producto.bajoStockMinimo(), producto.getFechaCreacion(), producto.getFechaActualizacion());
    }
}
