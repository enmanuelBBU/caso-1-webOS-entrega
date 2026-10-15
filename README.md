# StockFlow

API REST de gestión de inventario y pedidos para GlobalRetail S.A. (Estudio de Caso 1, Desarrollo Web Orientado a Objeto, Universidad del Bío-Bío).

**Entrega 2:** estructura del proyecto, entidades JPA mapeadas a PostgreSQL y un endpoint de prueba (`GET /api/hola`). El resto de las capas está creado y vacío; se completa en las próximas entregas.

| Componente | Tecnología |
| --- | --- |
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Validation) |
| Base de datos | PostgreSQL 16, en un contenedor de Podman |
| Documentación | Swagger UI (springdoc-openapi) |

## Requisitos

- Java JDK 17 o superior (`java -version`)
- Podman 4 o superior (`podman --version`) y un proveedor de compose (`podman compose version`)
- Maven no es necesario: el proyecto trae el Maven Wrapper.

## Ejecución

1. Iniciar Podman (la primera vez, antes: `podman machine init`):

   ```
   podman machine start
   ```

2. Levantar la base de datos:

   ```
   podman compose up -d
   ```

   Sin proveedor de compose:

   ```
   podman run -d --name stockflow-db -p 5432:5432 -e POSTGRES_DB=stockflow -e POSTGRES_USER=stockflow -e POSTGRES_PASSWORD=stockflow -v stockflow-data:/var/lib/postgresql/data docker.io/library/postgres:16-alpine
   ```

3. Ejecutar la API (en PowerShell, `.\mvnw.cmd`):

   ```
   ./mvnw spring-boot:run
   ```

4. Probar:

   - <http://localhost:8080/api/hola> → `{"mensaje":"Hola desde StockFlow"}`
   - <http://localhost:8080/swagger-ui.html>

Al iniciar, `schema.sql` crea las tablas y Hibernate valida (`ddl-auto=validate`) que las entidades coincidan con ellas.

## Estructura

```
src/main/java/cl/ubiobio/stockflow
├── config/        configuración (OpenAPI)
├── controller/    endpoints REST (HolaController operativo)
├── dto/           objetos de entrada y salida
├── exception/     excepciones y manejador global
├── model/         entidades JPA: Cliente, Producto, Pedido, DetallePedido, MovimientoStock
├── repository/    repositorios Spring Data JPA
└── service/       lógica de negocio
src/main/resources
├── application.properties
├── schema.sql     tablas, restricciones e índices
└── data.sql       datos de ejemplo (próxima entrega)
```
