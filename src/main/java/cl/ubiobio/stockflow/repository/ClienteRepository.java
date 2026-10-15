package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
