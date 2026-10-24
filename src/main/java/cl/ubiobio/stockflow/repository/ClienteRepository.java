package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    boolean existsByRut(String rut);

    boolean existsByEmail(String email);

    boolean existsByRutAndIdNot(String rut, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}
