package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.EstadoPedido;
import cl.ubiobio.stockflow.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    boolean existsByClienteId(Long clienteId);

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    Optional<Pedido> findConDetallesById(Long id);

    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    @Query("""
            select count(distinct p.id) as cantidadPedidos,
                   coalesce(sum(d.cantidad), 0) as unidadesVendidas,
                   coalesce(sum(d.subtotal), 0) as montoTotal
            from Pedido p join p.detalles d
            where p.estado <> :excluido
              and p.fechaCreacion >= :desde
              and p.fechaCreacion < :hasta
            """)
    ResumenVentas resumenVentas(@Param("excluido") EstadoPedido excluido,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta);
}
