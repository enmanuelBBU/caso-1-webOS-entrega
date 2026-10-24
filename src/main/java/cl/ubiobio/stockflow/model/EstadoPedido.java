package cl.ubiobio.stockflow.model;

public enum EstadoPedido {
    PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO;

    public boolean puedeCambiarA(EstadoPedido nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == CONFIRMADO || nuevo == CANCELADO;
            case CONFIRMADO -> nuevo == ENVIADO || nuevo == CANCELADO;
            case ENVIADO -> nuevo == ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
