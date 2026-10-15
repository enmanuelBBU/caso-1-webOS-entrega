package cl.ubiobio.stockflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hola")
public class HolaController {

    @GetMapping
    public Map<String, String> hola() {
        return Map.of("mensaje", "Hola desde StockFlow");
    }
}
