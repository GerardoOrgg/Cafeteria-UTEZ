# Cafetería UTEZ

## Equipo

| Nombre | Matrícula |
|---|---|
| Luis Gerardo Barrón Flores | 20253ds064 |
| Miguel Chávez García | 20253ds075 |
| Cristian Eduardo Gómez Nava | 20253ds085 |

App Android para hacer pre-pedidos en las cafeterías de la UTEZ (**Nido de Halcón** y **El Balcón**) y pasar a recogerlos. Tiene dos partes:

| Carpeta | Qué es | Tecnología |
|---|---|---|
| `app/` | App móvil (alumno y encargado) | Kotlin, Jetpack Compose, MVVM, Room, DataStore, Retrofit, Coil |
| `api/` | Servidor API REST | Java 17, Spring Boot 3, Spring Data JPA, MySQL |

## Qué hace

- **Inicio de sesión** con dos pestañas: Estudiante y Encargado.
- **Alumno:** elige cafetería, ve el especial del día y lo del diario, abre la hoja de detalle de cada platillo (opción, piezas, notas), arma su carrito, confirma y sigue el estado en *Mis pedidos*.
- **Encargado:** solo ve los pedidos y el menú de **su** cafetería:
  - *Comandas:* avanza cada pedido (Recibido → Preparando → Listo → Entregado) o lo cancela.
  - *Entregados:* lo despachado en el día.
  - *Menú & Stock:* alta, edición y borrado de platillos, foto con la cámara y marcar agotado.
  - *Cafetería:* tiempo de espera.
- **Sensores:**
  - GPS: la app solo funciona a 800 m de la UTEZ (se activa en `Configuracion.kt`).
  - Cámara: fotos de platillos.
  - Acelerómetro: agita el teléfono y te sugiere un platillo.

## Cómo correrlo

### 1. La API (Spring Boot)

1. Abre la carpeta `api/` en IntelliJ IDEA y espera a que Maven descargue las dependencias.
2. En `api/src/main/resources/application.properties` pon el usuario y la contraseña de tu MySQL (`spring.datasource.username` y `spring.datasource.password`). La base `cafeteria_utez` se crea sola.
3. Corre `CafeteriaApiApplication`. La primera vez se llenan las tablas con el menú, las cuentas y unos pedidos de ejemplo.
4. Prueba en el navegador: `http://localhost:8080/api` debe decir *"API Cafetería UTEZ funcionando"*.

Si no tienes MySQL, activa el perfil `h2` (Run → Edit Configurations → Active profiles: `h2`) y usa una base en archivo, sin instalar nada.

En `api/CafeteriaUTEZ.postman_collection.json` hay una colección de Postman con todas las peticiones.

### 2. La app (Android Studio)

1. En Windows abre `cmd`, escribe `ipconfig` y copia la *Dirección IPv4* de tu PC (ej. `192.168.1.70`).
2. Ponla en `app/src/main/java/mx/edu/utez/cafeteria/config/Configuracion.kt`:
   ```kotlin
   const val URL_SERVIDOR = "http://192.168.1.70:8080/"
   ```
   En el emulador usa `http://10.0.2.2:8080/`.
3. El teléfono y la PC deben estar en la misma red Wi-Fi. Si el teléfono no llega, permite Java en el Firewall de Windows (redes privadas).
4. Sync y Run.

### Cuentas de prueba

| Rol | Usuario | Contraseña |
|---|---|---|
| Alumno | `20213TI045` | `123456` |
| Encargado Nido de Halcón | `nido@utez.edu.mx` | `nido123` |
| Encargado El Balcón | `balcon@utez.edu.mx` | `balcon123` |

## Endpoints de la API

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/auth/login` | Inicia sesión (`usuario`, `contrasena`, `rol`) |
| POST | `/api/auth/registro` | Crea cuenta de alumno |
| POST | `/api/auth/restablecer` | Cambia la contraseña olvidada (matrícula + nombre) |
| GET | `/api/productos?cafeteriaId=nido` | Menú de una cafetería (sin parámetro: las dos) |
| POST | `/api/productos` | Alta de platillo (encargado) |
| PUT | `/api/productos/{id}` | Edita un platillo (encargado de esa cafetería) |
| PATCH | `/api/productos/{id}/disponible` | Hay / agotado |
| DELETE | `/api/productos/{id}` | Borra un platillo |
| POST | `/api/fotos` | Sube la foto de un platillo (multipart, campo `archivo`) |
| GET | `/api/pedidos?cafeteriaId=nido` | Comandas de una cafetería |
| GET | `/api/pedidos?matricula=20213TI045` | Pedidos de un alumno |
| POST | `/api/pedidos` | Crea un pedido (el servidor calcula precios y valida) |
| PATCH | `/api/pedidos/{numero}/avanzar` | Siguiente estado |
| PATCH | `/api/pedidos/{numero}/cancelar` | Cancela con motivo |
| GET | `/api/cafeterias` | Tiempos de espera |
| PUT | `/api/cafeterias/{id}/tiempo` | Cambia el tiempo de espera |

Las acciones de encargado mandan el encabezado `X-Usuario-Id` y el servidor revisa que sea encargado de esa cafetería. Los errores regresan `{"mensaje": "..."}` con su código HTTP (400, 401, 403, 404, 409).
