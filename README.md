# StockFlow

API REST de gestión de inventario y pedidos para GlobalRetail S.A. (Estudio de Caso 1, Desarrollo Web Orientado a Objeto, Universidad del Bío-Bío).

| Componente | Tecnología |
| --- | --- |
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Validation) |
| Base de datos | PostgreSQL 16, en un contenedor de Podman |
| Documentación | Swagger UI (springdoc-openapi) |
| Pruebas | JUnit 5, MockMvc, H2 y colección de Postman |

## Requisitos

| Software | Versión | Cómo verificar | Dónde obtenerlo |
| --- | --- | --- | --- |
| Java JDK | 17 o superior | `java -version` | <https://adoptium.net> |
| Podman | 4 o superior | `podman --version` | <https://podman.io> |
| Proveedor de compose | cualquiera | `podman compose version` | `pip install podman-compose` (o el `docker-compose` de Docker Desktop) |
| Postman | opcional | — | <https://www.postman.com/downloads> |

- **Maven no es necesario:** el proyecto trae el Maven Wrapper (`mvnw.cmd` / `mvnw`), que descarga Maven solo la primera vez.
- **Puertos libres:** la base de datos usa el `5432` y la API el `8080`.
- **Proveedor de compose:** si no tienes uno, igual puedes levantar la base con `podman run` (ver paso 2).

## Primera vez

Estos pasos se hacen una sola vez por computador.

1. Clonar el repositorio y entrar a la carpeta:

   ```
   git clone <url-del-repositorio>
   cd <carpeta-del-repositorio>
   ```

2. Crear la máquina virtual de Podman (en Windows usa WSL2):

   ```
   podman machine init
   ```

   Si responde que la máquina ya existe, no hay que hacer nada más.

## Ejecución

Todos los comandos se ejecutan desde la carpeta del proyecto. En **PowerShell**, el wrapper se llama con `.\` delante: `.\mvnw.cmd`.

### 1. Iniciar Podman

```
podman machine start
```

Si responde que ya está corriendo, sigue al paso 2.

### 2. Levantar la base de datos

```
podman compose up -d
```

Comprueba que el contenedor esté arriba:

```
podman ps
```

Debe aparecer `stockflow-db` con estado `Up`.

Si `podman compose` no funciona porque no hay proveedor de compose, el mismo contenedor se crea con:

```
podman run -d --name stockflow-db -p 5432:5432 -e POSTGRES_DB=stockflow -e POSTGRES_USER=stockflow -e POSTGRES_PASSWORD=stockflow -v stockflow-data:/var/lib/postgresql/data docker.io/library/postgres:16-alpine
```

Con `podman run` el contenedor se crea solo la primera vez. Las siguientes veces basta con `podman start stockflow-db`.

### 3. Ejecutar la API

```
.\mvnw.cmd spring-boot:run      # Windows (PowerShell o cmd)
./mvnw spring-boot:run          # Linux, macOS o Git Bash
```

La primera ejecución tarda unos minutos porque descarga Maven y las dependencias. La API está lista cuando aparece:

```
Started StockflowApplication in ... seconds
```

Al arrancar, la API ejecuta `schema.sql` (crea las tablas si no existen) y `data.sql` (carga los datos de ejemplo si no están).

**Desde IntelliJ IDEA:** abre la carpeta del proyecto, espera a que termine de importar Maven y ejecuta la clase `StockflowApplication` con el botón ▶.

### 4. Comprobar que funciona

- Swagger UI, para probar todos los endpoints desde el navegador: <http://localhost:8080/swagger-ui.html>
- Productos bajo stock mínimo, desde PowerShell:

  ```
  Invoke-RestMethod http://localhost:8080/api/productos/stock-bajo
  ```

  Debe devolver los productos `CAB-005` y `MON-003`, que en los datos de ejemplo están bajo su stock mínimo.

### Datos de ejemplo

| Tipo | IDs | Detalle |
| --- | --- | --- |
| Clientes | 1 a 3 | María González, Juan Pérez, Comercial Andes Ltda. |
| Productos | 1 a 6 | `TEC-001`, `MOU-002`, `MON-003`, `AUD-004`, `CAB-005`, `SSD-006` |

Pedido de ejemplo para `POST /api/pedidos`:

```json
{
  "clienteId": 1,
  "items": [
    { "productoId": 1, "cantidad": 2 },
    { "productoId": 3, "cantidad": 1 }
  ]
}
```

## Detener todo

| Qué | Comando |
| --- | --- |
| Detener la API | `Ctrl + C` en la terminal donde corre |
| Detener la base de datos (conserva los datos) | `podman compose down` |
| Detener la base y borrar sus datos | `podman compose down -v` |
| Apagar la máquina de Podman | `podman machine stop` |

Después de `podman compose down -v`, la próxima vez la base parte vacía y se vuelve a cargar `data.sql`.

## Probar con Postman

La colección está en `postman/StockFlow.postman_collection.json`.

1. En Postman, presiona **Import** y elige ese archivo.
2. Aparece la colección **StockFlow API**, con 5 carpetas.
3. Con la API corriendo, haz clic derecho sobre la colección, elige **Run collection** y presiona **Run** sin cambiar el orden.

Las peticiones dependen unas de otras (las primeras crean el cliente y los productos que usan las siguientes), por eso hay que correrlas en orden. Cada ejecución crea datos nuevos, así que se puede repetir. La URL base está en la variable `baseUrl` de la colección.

## Configuración

La conexión a la base de datos y el puerto se cambian con variables de entorno, sin tocar el código:

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/stockflow` |
| `DB_USER` | `stockflow` |
| `DB_PASSWORD` | `stockflow` |
| `SERVER_PORT` | `8080` |

Ejemplo en PowerShell (vale solo para esa terminal):

```
$env:SERVER_PORT = "8081"
.\mvnw.cmd spring-boot:run
```

Para usar un PostgreSQL instalado localmente en vez de Podman, crea la base y el usuario:

```sql
CREATE USER stockflow WITH PASSWORD 'stockflow';
CREATE DATABASE stockflow OWNER stockflow;
```

## Problemas frecuentes

| Error | Causa y solución |
| --- | --- |
| `mvnw.cmd no se reconoce como nombre de un cmdlet` | PowerShell exige `.\` delante: `.\mvnw.cmd spring-boot:run` |
| `JAVA_HOME not found` o `'java' no se reconoce` | Falta el JDK 17. Instálalo y abre una terminal nueva |
| `Connection to localhost:5432 refused` | La base no está corriendo. Revisa `podman ps` y ejecuta `podman compose up -d` |
| `Cannot connect to Podman` | La máquina de Podman está apagada: `podman machine start` |
| `password authentication failed for user "stockflow"` | Hay otro PostgreSQL usando el puerto 5432. Detenlo, o cambia el puerto en `compose.yaml` (`"5433:5432"`) y usa `DB_URL=jdbc:postgresql://localhost:5433/stockflow` |
| `Port 8080 was already in use` | Otro programa usa el puerto. Define `SERVER_PORT` con otro valor |
| `looking up compose provider failed` | Falta un proveedor de compose. Instala `podman-compose` o usa el `podman run` del paso 2 |
| Cambié `schema.sql` y no se refleja | Las tablas solo se crean si no existen. Borra la base con `podman compose down -v` y vuelve a levantarla |

## Pruebas automatizadas

```
.\mvnw.cmd test      # Windows
./mvnw test          # Linux, macOS o Git Bash
```

Las pruebas usan H2 en modo PostgreSQL con el mismo `schema.sql`, así que no necesitan Podman ni la base levantada.

| Clase | Qué prueba |
| --- | --- |
| `PedidoFlujoIntegrationTest` | Flujo HTTP completo: pedido aceptado, pedido rechazado sin descontar stock, cancelación, pedidos por cliente, stock bajo y reporte de ventas |
| `PedidoServiceTest` | Lógica del servicio de pedidos con Mockito |
| `ProductoControllerTest` | Validaciones y códigos HTTP del controlador de productos |

## Estructura

```
src/main/java/cl/ubiobio/stockflow
├── controller   Endpoints REST
├── service      Lógica de negocio y transacciones
├── repository   Acceso a datos (Spring Data JPA)
├── model        Entidades JPA
├── dto          Objetos de entrada y salida de la API
├── exception    Excepciones y manejador global de errores
└── config       Configuración de OpenAPI
src/main/resources
├── application.properties
├── schema.sql   Creación de tablas
└── data.sql     Datos de ejemplo
postman/         Colección de Postman
compose.yaml     Contenedor de PostgreSQL para Podman
```

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/productos` | Crear producto |
| GET | `/api/productos` | Listar productos (filtros: `nombre`, `categoria`, `activo`, `precioMin`, `precioMax`) |
| GET | `/api/productos/{id}` | Obtener producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto (lógico si tiene historial) |
| PATCH | `/api/productos/{id}/stock` | Reponer (`ENTRADA`) o ajustar (`AJUSTE`) stock |
| GET | `/api/productos/{id}/movimientos` | Historial de movimientos de stock |
| GET | `/api/productos/stock-bajo` | Productos bajo su stock mínimo |
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/clientes` | Listar clientes (filtros: `nombre`, `email`) |
| GET | `/api/clientes/{id}` | Obtener cliente |
| PUT | `/api/clientes/{id}` | Actualizar cliente |
| DELETE | `/api/clientes/{id}` | Eliminar cliente (solo sin pedidos) |
| GET | `/api/clientes/{id}/pedidos` | Pedidos de un cliente |
| POST | `/api/pedidos` | Registrar pedido (transaccional) |
| GET | `/api/pedidos` | Listar pedidos (filtros: `estado`, `clienteId`, `desde`, `hasta`) |
| GET | `/api/pedidos/{id}` | Obtener pedido con detalle |
| PATCH | `/api/pedidos/{id}/estado` | Cambiar estado (al cancelar se devuelve el stock) |
| GET | `/api/reportes/ventas?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | Reporte de ventas |

Los listados paginados aceptan `pagina` (desde 0), `tamano` (máx. 100) y `orden` (por ejemplo `orden=precio,desc`).

### Registro de pedidos

`POST /api/pedidos` se ejecuta en una sola transacción (`@Transactional`):

1. Bloquea las filas de los productos del pedido (`SELECT ... FOR UPDATE`) para que dos pedidos simultáneos no vendan el mismo stock.
2. Verifica que cada producto exista, esté activo y tenga stock suficiente. Si un producto se repite en el pedido, se suman sus cantidades.
3. Si algún producto no alcanza, responde `409 Conflict` con la lista de faltantes y no descuenta nada.
4. Si todo alcanza, descuenta el stock, registra los movimientos de salida y crea el pedido en estado `PENDIENTE` (`201 Created`).

Estados del pedido: `PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO`. Se puede cancelar desde `PENDIENTE` o `CONFIRMADO`.
