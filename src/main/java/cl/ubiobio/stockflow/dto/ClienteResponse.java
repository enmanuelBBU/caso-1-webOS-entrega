package cl.ubiobio.stockflow.dto;

import cl.ubiobio.stockflow.model.Cliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String rut,
        String nombre,
        String email,
        String telefono,
        String direccion,
        LocalDateTime fechaRegistro) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getRut(), cliente.getNombre(), cliente.getEmail(),
                cliente.getTelefono(), cliente.getDireccion(), cliente.getFechaRegistro());
    }
}
