package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
