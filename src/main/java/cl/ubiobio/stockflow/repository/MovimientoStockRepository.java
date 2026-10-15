package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
}
