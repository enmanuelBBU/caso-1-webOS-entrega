package cl.ubiobio.stockflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stockflowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("StockFlow API")
                .description("Gestión de inventario y pedidos para GlobalRetail S.A.")
                .version("0.0.1"));
    }
}
