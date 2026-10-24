package cl.ubiobio.stockflow.service;

import cl.ubiobio.stockflow.dto.ReporteVentasResponse;
import cl.ubiobio.stockflow.exception.SolicitudInvalidaException;
import cl.ubiobio.stockflow.model.EstadoPedido;
import cl.ubiobio.stockflow.repository.PedidoRepository;
import cl.ubiobio.stockflow.repository.ResumenVentas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public ReporteVentasResponse ventas(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        ResumenVentas resumen = pedidoRepository.resumenVentas(EstadoPedido.CANCELADO,
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        return new ReporteVentasResponse(desde, hasta,
                valor(resumen.getCantidadPedidos()),
                valor(resumen.getUnidadesVendidas()),
                valor(resumen.getMontoTotal()));
    }

    private long valor(Number numero) {
        return numero != null ? numero.longValue() : 0L;
    }
}
