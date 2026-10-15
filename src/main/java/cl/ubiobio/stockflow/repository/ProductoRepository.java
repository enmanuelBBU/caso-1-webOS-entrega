package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
