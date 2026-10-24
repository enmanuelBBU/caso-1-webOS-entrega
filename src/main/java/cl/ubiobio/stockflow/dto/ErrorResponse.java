package cl.ubiobio.stockflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(LocalDateTime fecha, int status, String error, String mensaje, Object detalles) {
}
