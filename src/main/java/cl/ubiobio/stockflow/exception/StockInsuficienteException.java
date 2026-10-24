package cl.ubiobio.stockflow.exception;

import cl.ubiobio.stockflow.dto.FaltanteStock;
import lombok.Getter;

import java.util.List;

@Getter
public class StockInsuficienteException extends RuntimeException {

    private final List<FaltanteStock> faltantes;

    public StockInsuficienteException(List<FaltanteStock> faltantes) {
        super("Stock insuficiente para " + faltantes.size() + " producto(s); el pedido no fue registrado");
        this.faltantes = List.copyOf(faltantes);
    }
}
