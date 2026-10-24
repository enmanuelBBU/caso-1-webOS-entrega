package cl.ubiobio.stockflow.repository;

import cl.ubiobio.stockflow.model.Cliente;
import cl.ubiobio.stockflow.model.EstadoPedido;
import cl.ubiobio.stockflow.model.Pedido;
import cl.ubiobio.stockflow.model.Producto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Especificaciones {

    private Especificaciones() {
    }

    public static Specification<Producto> productos(String nombre, String categoria, Boolean activo,
                                                    Long precioMin, Long precioMax) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (tieneTexto(nombre)) {
                condiciones.add(contiene(cb, root.get("nombre"), nombre));
            }
            if (tieneTexto(categoria)) {
                condiciones.add(cb.equal(cb.lower(root.get("categoria")), categoria.trim().toLowerCase()));
            }
            if (activo != null) {
                condiciones.add(cb.equal(root.get("activo"), activo));
            }
            if (precioMin != null) {
                condiciones.add(cb.ge(root.get("precio"), precioMin));
            }
            if (precioMax != null) {
                condiciones.add(cb.le(root.get("precio"), precioMax));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    public static Specification<Cliente> clientes(String nombre, String email) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (tieneTexto(nombre)) {
                condiciones.add(contiene(cb, root.get("nombre"), nombre));
            }
            if (tieneTexto(email)) {
                condiciones.add(contiene(cb, root.get("email"), email));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    public static Specification<Pedido> pedidos(EstadoPedido estado, Long clienteId, LocalDate desde, LocalDate hasta) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
            }
            if (clienteId != null) {
                condiciones.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            if (desde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), desde.atStartOfDay()));
            }
            if (hasta != null) {
                condiciones.add(cb.lessThan(root.get("fechaCreacion"), hasta.plusDays(1).atStartOfDay()));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    private static Predicate contiene(CriteriaBuilder cb, Expression<String> campo, String texto) {
        return cb.like(cb.lower(campo), "%" + texto.trim().toLowerCase() + "%");
    }

    private static boolean tieneTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}
