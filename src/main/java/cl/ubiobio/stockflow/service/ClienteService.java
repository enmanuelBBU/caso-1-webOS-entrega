package cl.ubiobio.stockflow.service;

import cl.ubiobio.stockflow.dto.ClienteRequest;
import cl.ubiobio.stockflow.dto.ClienteResponse;
import cl.ubiobio.stockflow.dto.PaginaResponse;
import cl.ubiobio.stockflow.exception.ConflictoException;
import cl.ubiobio.stockflow.exception.RecursoNoEncontradoException;
import cl.ubiobio.stockflow.model.Cliente;
import cl.ubiobio.stockflow.repository.ClienteRepository;
import cl.ubiobio.stockflow.repository.Especificaciones;
import cl.ubiobio.stockflow.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        String rut = normalizarRut(request.rut());
        if (clienteRepository.existsByRut(rut)) {
            throw new ConflictoException("Ya existe un cliente con RUT " + rut);
        }
        if (clienteRepository.existsByEmail(request.email())) {
            throw new ConflictoException("Ya existe un cliente con email " + request.email());
        }
        Cliente cliente = new Cliente();
        copiarDatos(cliente, request, rut);
        return ClienteResponse.desde(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ClienteResponse> listar(String nombre, String email, Pageable pageable) {
        return PaginaResponse.desde(clienteRepository.findAll(Especificaciones.clientes(nombre, email), pageable)
                .map(ClienteResponse::desde));
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.desde(buscar(id));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscar(id);
        String rut = normalizarRut(request.rut());
        if (clienteRepository.existsByRutAndIdNot(rut, id)) {
            throw new ConflictoException("Ya existe otro cliente con RUT " + rut);
        }
        if (clienteRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictoException("Ya existe otro cliente con email " + request.email());
        }
        copiarDatos(cliente, request, rut);
        return ClienteResponse.desde(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscar(id);
        if (pedidoRepository.existsByClienteId(id)) {
            throw new ConflictoException("El cliente tiene pedidos asociados y no puede eliminarse");
        }
        clienteRepository.delete(cliente);
    }

    private Cliente buscar(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private void copiarDatos(Cliente cliente, ClienteRequest request, String rut) {
        cliente.setRut(rut);
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
    }

    private String normalizarRut(String rut) {
        return rut.trim().toUpperCase();
    }
}
