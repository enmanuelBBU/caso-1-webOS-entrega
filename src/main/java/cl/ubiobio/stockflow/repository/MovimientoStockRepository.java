package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    boolean existsByProductoId(Long productoId);

    List<MovimientoStock> findByProductoIdOrderByFechaDescIdDesc(Long productoId);
}
