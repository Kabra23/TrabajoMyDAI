# Barça Athletic WebApp

![LOGO](ruta/a/tu/logo.png)

## Integrantes

- Tomas Andrei Negrota Ilie
- Emiliano Álvarez Flores

## Eslogan

> "La cantera también juega: vive el Barça Athletic como nunca antes."

---

## Resumen

Aplicación web dedicada al Barça Athletic (segundo equipo del FC Barcelona). Permite consultar información actualizada, comprar y gestionar entradas, y administrar usuarios (aficionados y administradores).

---

## Descripción

La plataforma conecta a los aficionados con el día a día del Barça Athletic, centralizando información oficial, actualidad del equipo y servicios relacionados con la experiencia de los partidos. Fomenta la interacción y participación, integrando herramientas de gestión de entradas, comunicación y soporte automatizado.

---

## Funcionalidades principales

- Mostrar información, noticias y estadísticas del Barça Athletic.
- Compra online de entradas para partidos, con selección de partido, zona y asiento.
- Gestión de usuarios con registro, login y roles (usuario normal, administrador).
- Panel de administración para gestionar usuarios, entradas y contenido.
- CRUD completo para usuarios, entradas y noticias.
- Autenticación segura mediante credenciales.

---

## CRUD y relaciones (actualizado)

General
- Todas las operaciones CRUD deben ejecutarse dentro de transacciones.
- Operaciones sensibles (crear/actualizar/eliminar usuarios, eventos, entradas) restringidas a administradores.
- Reglas de negocio (re-asignar, validar cancelaciones, reembolsos) manejarse en el servicio, no confiar solo en cascadas JPA.
- Para integridad referencial se puede usar:
    - Cascada JPA (`CascadeType.REMOVE`, `orphanRemoval=true`) cuando eliminar una entidad debe eliminar dependientes.
    - `ON DELETE CASCADE` en la BD si se requiere eliminación a nivel de base de datos.
    - `SET NULL` o re-asignación cuando se quiera conservar el registro hijo.

## Entidad `Usuario`

- **Crear**: solo administradores (`POST /admin/usuarios`).
- **Leer**: admin: lista y detalle; usuario: su propio perfil y entradas/recordatorios.
- **Actualizar**: admin o el propio usuario (según permiso).
- **Eliminar**: solo admin.
    - Efectos:
        - `Entrada`: eliminar `Usuario` debe eliminar sus entradas asociadas (cascada).
        - `eventos_usuarios` (inscripciones): eliminar usuario debe borrar sus inscripciones.
        - `Recordatorio`: eliminar usuario debe eliminar sus recordatorios (cascada).
    - Implementar en JPA con `CascadeType.ALL`/`orphanRemoval=true` o en BD con `ON DELETE CASCADE`.
 
### Atributos principales
- `dni` (PK)
- `nombre`
- `email`

### Relaciones

1. **Usuario → Ticket (1:N)**

`@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Ticket> tickets;`

- Un usuario puede tener varios tickets.
- La FK está en `Ticket` (`usuario_id`).
- Al borrar/guardar un usuario se borran/guardan también sus tickets (cascade + orphanRemoval).

2. **Usuario ↔ Evento (N:M)**

``@ManyToMany
@JoinTable(
    name = "usuario_eventos",
    joinColumns = @JoinColumn(name = "usuario_dni"),
    inverseJoinColumns = @JoinColumn(name = "eventos_id_evento")
)
private List<Evento> eventos;``

- Un usuario puede estar inscrito a varios eventos.
- Un evento puede tener varios usuarios.
- Aquí se crea una tabla intermedia (`usuario_eventos`) para guardar la relación.
- Si se elimina un usuario, lo que desaparece son sus filas en `usuario_eventos`, pero no se eliminan los eventos.
- Esto es diferente al caso Usuario–Ticket porque aquí la relación es muchos a muchos.

### Comportamiento CRUD entre las clases relacionadas
- Crear Usuario:
    - Se puede crear solo.
    - Si ya trae tickets en la lista también se guardan por el cascade.
    - Si se le agregan eventos con `addEvento` se guarda la relación en la tabla intermedia.

- Leer Usuario:
    - Se puede acceder a `getTickets()` y a `getEventos()`.

 - Actualizar Usuario:
    - Cambios, por ejemplo, en el nombre no afectan a tickets ni eventos, porque ellos solo guardan la referencia al usuario.

- Borrar Usuario:
  - Se borran sus tickets por la relación 1:N con cascade.
  - Se borran sus relaciones con eventos (las filas de la tabla intermedia), pero no se borran los eventos.

### Test que lo demuestra: `UsuarioRepositoryTest`

Este test prueba que:

- Al guardar un usuario con 2 tickets, se guardan los 2 tickets.
- Al borrar el usuario ya no quedan tickets en la BD.

Con esto se demuestra que el mapeo entre Usuario y Ticket está bien y el método cascade funciona correctamente.

---

## Entidad `Entrada` (Ticket)
- **Crear**: compra de entrada, asociada a `Usuario` y a un `Evento`.
- **Leer**: usuario ve sus entradas; admin ve todas.
- **Actualizar**: cambios según política (asiento, estado).
- **Eliminar**: cancelación/reembolso; si se elimina `Usuario` o `Evento`, gestionar cascada según política.

### Atributos principales
- `id_ticket` (PK)
- `precio`
- `asiento`

### Relaciones

1. **Ticket → Usuario (N:1)**

`@ManyToOne
@JoinColumn(name = "usuario_id")
private Usuario usuario;
`

- Varios tickets pueden pertenecer al mismo usuario.
- La clase Ticket tiene la FK `usuario_id`.


2. **Ticket → Evento (N:1)**

`@ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;`

- Varios tickets pueden pertenecer a un mismo evento.
- La clase Ticket tiene la FK `evento_id`. 

### Comportamiento CRUD entre las clases relacionadas

- Crear Ticket:
    - Necesita que existan antes un Usuario y un Evento (porque tiene FKs).
    - Al guardarlo, no afecta al usuario ni al evento.
 
- Leer Ticket:
  - Con esto se puede saber a qué usuario y evento pertenece.

 - Actualizar Ticket:
   - Puedes cambiar precio, asiento, incluso el usuario o evento al que apunta.
   - No modifica al usuario ni al evento, solo las referencias.

 - Borrar Ticket:
   - Se borra el ticket y no se borra ni el usuario ni el evento.
   - Aquí no hay cascade hacia arriba.
  
### Test que lo demuestra: `TicketRepositoryTest`

Este test prueba las 4 operaciones básicas de CRUD sobre Ticket y que estén bien establecidas las relaciones JPA:

- Dentro del test se prueba a crear un nuevo evento y usuario, ya que sin esto no se puede asignar un ticket.
- Se crea un nuevo ticket y se guarda. Esto no modifica al usuario ni al evento, solo se crea una fila en `ticket` que señála la relación con el usuario y el evento.
- Usamos el método find para tener acceso quién lo compró, para qué evento, precio y asiento.
- Se pueden actualizar los datos propios del ticket, sin tocar al usuario o el evento, por lo que si se cambia el nombre del usuario en la tabla `usuario`, todos los tickets que apuntan a él lo van a seguir mostrando bien cuando lo consultes, porque la relación es por ID.
- Al borrar un Ticket, el usuario y el evento sigue existiendo, contrario a lo que pasa con `Usuario → Ticket` donde había cascade desde el usuario.
  
---

## Entidad `Evento`
- **Crear**: solo administradores.
- **Leer**: público.
- **Actualizar**: solo administradores.
- **Eliminar**: solo administradores.
    - Efectos:
        - `Entrada` e `eventos_usuarios` deben eliminarse o marcarse como canceladas al borrar un `Evento`.
        - Preferible diseñar soft-delete o reglas de notificación antes de eliminación.

Entidad `eventos_usuarios` (inscripciones)
- Tabla relacional entre `Usuario` y `Evento` (puede ser entidad `EventoUsuario` con datos adicionales: rol, fecha inscripción).
- CRUD: crear inscripción al comprar o reservar; eliminar al cancelar o al eliminar usuario/evento.

Entidad `Recordatorio`
- Asociado a `Usuario` y opcionalmente a un `Evento`.
- **Crear/Leer/Actualizar/Eliminar**: usuario administra sus recordatorios; admin puede gestionar globalmente.
- Al eliminar `Usuario`, borrar sus recordatorios.

## Funcionalidades opcionales y futuras

- Integración con Google Maps para rutas al estadio.
- Generación de código de barras/QR único por entrada.
- Integración con redes sociales (Twitter/X, Instagram, Facebook).

---

## Tecnologías utilizadas

- Java
- Spring Boot
- Maven
- H2 Database (desarrollo)
- (Agregar frameworks front-end si aplica)

---

## Ejecución del proyecto

1. Clona el repositorio.
2. Instala dependencias con Maven.
3. Configura la base de datos en `application.properties`.
4. Ejecuta la aplicación con tu IDE o `mvn spring-boot:run`.
5. Accede a la web en `http://localhost:8080`.

---

## Configuración de la Base de Datos
Este proyecto utiliza H2 como base de datos en memoria para desarrollo y pruebas.
Acceso a la consola H2:
Asegúrate de que la aplicación esté en ejecución.

Accede a http://localhost:8080/h2-console.

Usa la siguiente configuración:

1. JDBC URL: jdbc:h2:mem:testdb
2. User Name: sa
3. Password: (dejar en blanco)
