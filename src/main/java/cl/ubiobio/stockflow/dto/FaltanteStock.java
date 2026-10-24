package cl.ubiobio.stockflow.dto;

public record FaltanteStock(Long productoId, String nombre, int solicitado, int disponible) {
}
