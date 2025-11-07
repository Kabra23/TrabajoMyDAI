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

La entidad `Usuario` es la raiz de todo el modelo. De esta entidad dependen otras como `Ticket`, donde no puede existir tickets sin usuario primeramente. En cambio, el usuario sí tiene sentido por sí mismo ya que puede existir aunque todavía no tenga tickets o eventos.

En nuestra aplicación el usuario es aquel que se registra, compra ticketsy asiste a los eventos.

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
    - Se puede crear un usuario solo con nombre e email y si al mismo tiempo añadimos un ticket a su lista con `(u.getTickets().add(t1);`, al guardar el usuario se guardan los tickets por `cascade = ALL`. De igual forma si le añadimos eventos por medio del helper `addEvento(evento)` se guarda la relación en la tabla intermedia.

- Leer Usuario:
    - Al traer un usuario desde la BD podemos navegar a ver sus tickets usando `usuario.getTickets()` y ver los eventos a los que está inscrito por `usuario.getEventos()`, demostrando que las relaciones están bien mapeadas.

 - Actualizar Usuario:
    - Si cambiamos nombre o email solo cambia el usuario y los tickets no necesitan actualizarse porque ellos guardan solo la FK (`usuario_id`). Lo mismo pasa con los eventos, porque la relación ManyToMany no duplica datos del usuario, solo guarda los IDs.

- Borrar Usuario:
  - Cuando borramos un usuario se deben de borrar sus tickets por la relación 1:N con cascade y se deben de borrar sus filas en la tabla `usuario_eventos`, donde están las relaciones con eventos y estos mismos no se borran. Esto hace que el modelo no deje tickets huérfanos y no se borran entidades que no tienen que desaparecer, por ejemplo con `Evento` en donde solo se elimina la asociación en la tabla intermedia.

### Test `UsuarioRepositoryTest`

Dentro del test probamos que:

- Se cree un usuario y dos tickets y que estén asociados por ambos lados de la relación, sabiendo el ticket quién es su usuario por medio de (`t1.setUsuario(u)`) y que el usuario tiene los tickets en su lista usando (`u.getTickets().add(t1)`)
- Por medio de `em.persistFlushFind(u)` guardamos el usuario y con el `cascade = ALL` se guarden también los tickets. Con flush se ecribe en la BD y lo vuelve a leer para comprobar. Con esto nos aseguramos que el usuario exista en la BD y que tenga dos tickets asignados.
- Si borramos una entidad usando `em.remove(saved)` y `em.flush()` se borra el usuario y con ayuda de `cascade`y `orphanRemoval` se borran sus tickets. Posteriormente, consultamos a la tabla de tickets y verificamos que no quedó ninguno. 

Con esto se demuestra que el mapeo entre Usuario y Ticket está bien y el método cascade funciona correctamente.

---

## Entidad `Entrada` (Ticket)

La entidad `Ticket` es una entidad dependiente que prueba que un usuario está yendo a un evento, en un asiento y por un precio.

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
    - Necesita que existan antes un Usuario y un Evento (porque tiene FKs). Esto se hace usando `em.persist(u);` y `em.persist(e);`. Posteriormente se crean los tickets, usamos `t.setUsuario(u);` y `t.setEvento(e);` para llenar las FKs y cuando guardamos el ticket no se modifica ni el usuario ni el evento, tan solo se crea una fila en `ticket` para señálar que el ticket es de un usuario X y un evento Y.
 
- Leer Ticket:
  - Por medio de `find` tenemos acceso a `ticket.getUsuario()` usuario que compró el ticket, `ticket.getEvento()` para qué evento es y `ticket.getPrecio()` y `ticket.getAsiento()` para obtener el precio y asiento.

    Con esto demostramos que el mapeo ManyToOne funciona ya que el ticket sabe a quién pertenece y a qué evento.

 - Actualizar Ticket:
   - Se puede cambiar precio y asiento sin tocar al usuario ni al evento porque son datos del ticket. También podemos modificar el `ticket.setUsuario()` o `ticket.setEvento()` y cambiar la FK en la tabla `ticket` para así actualizar a quién pertenece ese ticket.

     Con esto nos aseguramos que si cambiamos el nombre del usuario en la tabla `usuario`, todos los tickets que apuntan a él lo van a seguir mostrando bien cuando lo consultes, porque la relación es por ID.

 - Borrar Ticket:
   - Si se borra el ticket, no se borra ni el usuario ni el evento, mostrando un caso contratrio a lo que teniamos en `Usuario → Ticket` donde se usaba el `cascade`. Aquí no hay cascade porque sería contraproducente que por borrar un ticket se borrara un usuario o un evento.
  
     Al final demostramos que el ticket se puede borrar y que no se rompe la relación con usuario y evento.
  
### Test `TicketRepositoryTest`

Este test prueba las 4 operaciones básicas de CRUD sobre Ticket y que estén bien establecidas las relaciones JPA:

- Dentro del test se prueba a crear un nuevo evento y usuario, ya que sin esto no se puede asignar un ticket.
- Se crea un nuevo ticket y se guarda. Esto no modifica al usuario ni al evento, solo se crea una fila en `ticket` que señála la relación con el usuario y el evento.
- Usamos el método find para tener acceso quién lo compró, para qué evento, precio y asiento.
- Se pueden actualizar los datos propios del ticket, sin tocar al usuario o el evento, por lo que si se cambia el nombre del usuario en la tabla `usuario`, todos los tickets que apuntan a él lo van a seguir mostrando bien cuando se consulte, porque la relación es por ID.
- Al borrar un Ticket, el usuario y el evento sigue existiendo, contrario a lo que pasa con `Usuario → Ticket` donde había cascade desde el usuario.

Resumidamente, la entidad `Ticket` es dependiente de `Usuario` y `Evento`, pero su eliminación no afecta a las entidades padre.
  
---

## Entidad `Evento`

`Evento`es la entidad que representa algo a lo que los usuarios se pueden apuntar como es un partido, una charla, una presentación del Barça B, etc.

- **Crear**: solo administradores.
- **Leer**: público.
- **Actualizar**: solo administradores.
- **Eliminar**: solo administradores.
    - Efectos:
        - `Entrada` e `eventos_usuarios` deben eliminarse o marcarse como canceladas al borrar un `Evento`.
        - Preferible diseñar soft-delete o reglas de notificación antes de eliminación.

### Atributos principales
- `id_evento` (PK)
- `nombre_evento`
- `fecha_evento`
- `lugar_evento`
- `descripcion_evento`
- `tipo_evento`

### Relaciones

1. **Evento → Usuario (M:N)**

`@ManyToMany(mappedBy = "eventos")
private List<Usuario> usuarios = new LinkedList<>()`

- Muchos usuarios pueden ir a muchos eventos.
- El dueño de la relación en este caso es `Usuario` debido al `@JoinTable(...)`.
- La tabla intermedia `usuario_eventos` la define Usuario.
- `Evento` solo muestra qué usuarios están apuntados, no crea la tabla.

### Comportamiento CRUD entre las clases relacionadas

- Crear Evento:
  - Para crear un evento no son necesarios los usuarios, simplemente si desde el lado de `Usuario` usamos `u.addEvento(e)`, al guardar se crea la relación en la tabla intermedia. Con esto sabemos que al crear un evento no se crean los usuarios, solo queda listo para que se le asocien.
  
- Leer Evento:
  - Cuando recuperamos un evento desde la BD usando (`em.find(Evento.class, id)`) podemos usar `evento.getUsuarios();` para ver la lista de usuarios asociados con ese evento.

- Actualizar Evento:
  - Si cambiamos parámetros como `nombre_evento` o `fecha_evento` no afecta a los usuarios y esto es porque los usuarios no guardan el nombre del evento, solo se relacionan con él en la tabla intermedia que creamos.

- Borrar Evento:
  - Si borramos un evento, también se borrarán las filas de la tabla intermedia (`usuario_eventos`) que lo relacionaban con usuarios, pero esto no borra a los usuarios por la relación ManyToMany. Es decir, si borramos un evento que tiene usuarios asociados no se borran los usuarios, solo se rompe la relación.
 
### Test `EventoUsuarioRelationTest`

En este test probamos que se guarde la relación en la tabla intermedia y que se pueda quitar la relación, con lo que al volver a leer el evento, ya no aparezcan los usuarios que antes estaban relacionados.

- Primero creamos un usuario y un evento para después, por medio del método `u.addEvento(e)` en `Usuario`, podemos agregar el evento a la lista del usuario y aseguremos que el usuario también quede en la lista del evento. Con esto se sincronizan ambos lados de la relación.
- Como sigueinte paso, guardamos las dos entidades  por medio de `em.persist(e);`, `em.persist(u);` y `em.flush();` y se crea la fila en la tabla intermedia de `usuario_eventos`.
- Usando `Evento foundEvento = em.find(Evento.class, e.getId());
assertNotNull(foundEvento);
assertEquals(1, foundEvento.getUsuarios().size());` comprobamos no solo que el usuario tenga el evento, sino que el evento también tenga al usuario. Con esto garantizamos que al guardar desde `Usuario` también se pueda leer en `Evento`.
- Ahora quitamos, guardamos y limpiamos la relación usando `u.removeEvento(e);
em.persist(u);
em.flush();
em.clear();`.
- Comprobamos que la relación se haya borrado por medio de `Evento afterDelete = em.find(Evento.class, e.getId());
assertNotNull(afterDelete);
assertTrue(afterDelete.getUsuarios().isEmpty());`. Con esto veremos que si se quita la relación entre un usuario y un evento, al volver a leer el evento el usuario ya no debe de salir.
- Finalmente borramos el usuario y comprobamos que el evento sigue usando `em.remove(em.find(Usuario.class, u.getDni()));
Evento afterUserRemove = em.find(Evento.class, e.getId());
assertNotNull(afterUserRemove);
assertTrue(afterUserRemove.getUsuarios().isEmpty());`.
    Con esto ya sabemos que si se borra el usuario, el evento no se borra y solo desaparece la relación en la tabla intermedia.

## Entidad `Recordatorio`

La entidad `Recordatorio` sirve para guardar mensajes o avisos relacionados con un usuario y un evento, como por ejemplo recordar una inscripción a un evento, recordar un evento para mañana, etc.

- Asociado a `Usuario` y opcionalmente a un `Evento`.
- **Crear/Leer/Actualizar/Eliminar**: usuario administra sus recordatorios; admin puede gestionar globalmente.
- Al eliminar `Usuario`, borrar sus recordatorios.

### Atributos principales
- `id_recordatorio` (PK)
- `usuario`
- `evento`
- `mensaje`
- `fecha`

### Relaciones

1. **Recordatorio → Usuario (N:1)**

`@ManyToOne
private Usuario usuario;
`

- Muchos recordatorios pueden pertenecer al mismo usuario.
- La tabla `recordatorio` tendrá una columna `usuario_dni` como FK hacia la tabla `usuario`.

2. **Recordatorio → Evento (N:1)**

`@ManyToOne
private Evento evento;`

- Muchos recordatorios pueden referirse al mismo evento.
- La tabla `recordatorio` tendrá también una columna `evento_id_evento` como FK hacia la tabla  `evento`.

De esta forma cada `Recordatorio` conecta un usuario con un evento y además guarda otro tipo de información como (`mensaje` y `fecha`).

### Comportamiento CRUD entre las clases relacionadas

- Crear Recordatorio:
  - Para crear un recordatorio se necesita que existan antes un `Usuario` y un `Evento`, así que al crear un recordatorio los asignamos por medio de `r.setUsuario(u);
r.setEvento(e);` y con esto llenamos las foreign keys.
  -  Guardamos por medio del método (`em.persistFlushFind(r)`) y se crea una fila en la tabla recordatorio que contiene:
        - El ID del usuario (`usuario_dni`).
        - El ID del evento (`evento_id_dni`).
        - Menasaje.
        - Fecha.

- Leer Recordatorio:
  - Podemos acceder a los datos propios del recordatorio como (`mensaje` y `fecha`) y también al usuario y al evento a los que pertenece por medio de `recordatorio.getUsuario().getNombre();` y `recordatorio.getEvento().getNombre();` demostrando que la relación ManyToOne funciona.

- Actualizar Recordatorio:
  - Si cambiamos parámetros como `mensaje` o `fecha` no afecta a los usuarios ni tampoco al evento, pero si quisieramos cambiar sus atributos lo podríamos hacer por medio de `recordatorio.setUsuario(otroUsuario);` y `recordatorio.setEvento(otroEvento);`.
    Con esto se actualizarían las FKs de la tabla `recodatorio`.

- Borrar Evento:
  - Si borramos un recordatorio, el usuario y el evento siguen existiendo. Como no hay método `cascade` hacia las entidades, solo se elimina la fila del recordatorio.
 
### Test `RecordatorioRepositoryTest`

En el test probamos el ciclo CRUD  y que las relaciones estén bien configuradas.

- Primero creamos las entidades básicas necesarias, como son `usuario` y `evento` ya que sin ellas, como mencionamos, no pueden existir los recordatorios.
- Posteriormente establecemos las relaciones ManyToOne con `usuario` y `evento` por medio de `Recordatorio r = new Recordatorio();
        r.setUsuario(u);
        r.setEvento(e);
        r.setMensaje("Recordar inscripción");
        r.setFecha("2025-11-01");`
- Usando `persistFlushFind` guardamos el recordatiro y lo volvemos a buscar en la BD y por medio de `assertNotNull(saved.getId_recordatorio());
        assertEquals("Recordar inscripción", saved.getMensaje());`, verificamos que haya generado un ID y que el mensaje se guardó correctamente.
- Finalmente eliminamos el recordatorio y verificamos que ya no exista usando `assertNotNull(saved.getId_recordatorio());
        assertEquals("Recordar inscripción", saved.getMensaje());`. Sabemos que el usuario y el evento no se borran, porque no hay método `cascade`.

## Prueba global de integración `Global Test`

En este test probamos el comportamiento de las disitntas entidades al mismo tiempo y su objetivo es verificar que las relaciones entre ellas funcionan correctamente durante las operaciones CRUD.

- Crear:
  - En el primer bloque de la prueba se contruyen las entidades involucradas, las cuales son un `Usuario` y `Evento` independientes. Posteriormente se crea un `Recordatorio` que hace la unión entre ambos debido a que tiene dos relaciones `@ManyToOne` en (`usuario` y `evento`).
  - Se hace persistencia, se guardan las referencias al usuario y al evento y se genere el `id_recordatorio` como PK.
 
- Leer:
  - Después de la creación, el test revisa que los datos se guardaron correctamente por medio de `assertNotNull(saved.getId_recordatorio());
assertEquals("Recordar inscripción", saved.getMensaje());
assertEquals("2025-11-01", saved.getFecha());
assertNotNull(saved.getUsuario());
assertNotNull(saved.getEvento());
assertEquals("Luis", saved.getUsuario().getNombre());
assertEquals("Seminario", saved.getEvento().getNombre());`.

    Aquí se valida que el recordatorio tiene un ID, que los datos como `mensaje` y `fecha` se conservaron sin alterarse, que las relaciones ManyToOne funcionan viendo que el recordatorio reconoce quién es su usuario y a qué evento pertenece y que al leer el recordatorio desde la base de datos, sus objetos relacionados también se cargan correctamente.

- Actualizar:
  - Por medio de `saved.setMensaje("Nuevo mensaje de recordatorio");
saved.getUsuario().setNombre("Luis Actualizado");
saved.getEvento().setNombre("Seminario Actualizado");
em.persist(saved);
em.flush();` se actualizan los datos.

    En primer nivel `Recordatorio` cambia su propio mensaje y esto comprueba que los datos de la entidad dependiente se pueden modificar sin perder las relaciones.

    En segundo nivel `Usuario` y `Evento` actualizan el nombre de sus atributos vinculados al recordatorio. Se detectan cambios en las entidades relacionadas y (`managed entities`) los sincroniza automáticamente.

    Aquí se demuestra que los objetos pueden actualizarse de forma conjunta dentro del mismo contexto de persistencia sin romper las referencias ni generar errores de sincronización.

  - Borrar:
    - En este punto se prueba que la eliminación del recordatorio funciona correctamente, ya que el registro desaparece de la tabla `recordatorio`. Esto lo hacemos por medio de `em.remove(updated);
em.flush();
Recordatorio deleted = em.find(Recordatorio.class, saved.getId_recordatorio());
assertNull(deleted);`,.

      Aquí validamos las relaciones ManyToOne y vemos que al borrar el hijo (Recordatorio), los padres (Usuario y Evento) se mantienen, por lo que no hay riesgo de eliminar datos principales por accidente.

- Revisión final:
  - El test realiza una consulta directa a la base de datos para asegurarse de que el usuario y el evento no fueron afectados y se demuestra que el `Usuario` y el `Evento` siguen persistentes tras eliminar el `Recordatorio`, que los cambios hechos en `Actualizar` se conservaron y que no hay borrados accidental en cascada.
        

## Entidad `eventos_usuarios` (inscripciones)

- Tabla relacional entre `Usuario` y `Evento` (puede ser entidad `EventoUsuario` con datos adicionales: rol, fecha inscripción).
- CRUD: crear inscripción al comprar o reservar; eliminar al cancelar o al eliminar usuario/evento.


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
