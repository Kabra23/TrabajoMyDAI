# Barça Athletic WebApp

![LOGO](<img width="2400" height="2400" alt="image" src="https://github.com/user-attachments/assets/be5ee7e0-8d0d-41e4-9609-04e490499a14" />
)

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

Entidad `Usuario`
- **Crear**: solo administradores (`POST /admin/usuarios`).
- **Leer**: admin: lista y detalle; usuario: su propio perfil y entradas/recordatorios.
- **Actualizar**: admin o el propio usuario (según permiso).
- **Eliminar**: solo admin.
    - Efectos:
        - `Entrada`: eliminar `Usuario` debe eliminar sus entradas asociadas (cascada).
        - `eventos_usuarios` (inscripciones): eliminar usuario debe borrar sus inscripciones.
        - `Recordatorio`: eliminar usuario debe eliminar sus recordatorios (cascada).
    - Implementar en JPA con `CascadeType.ALL`/`orphanRemoval=true` o en BD con `ON DELETE CASCADE`.

Entidad `Entrada` (Ticket)
- **Crear**: compra de entrada, asociada a `Usuario` y a un `Evento`.
- **Leer**: usuario ve sus entradas; admin ve todas.
- **Actualizar**: cambios según política (asiento, estado).
- **Eliminar**: cancelación/reembolso; si se elimina `Usuario` o `Evento`, gestionar cascada según política.

Entidad `Evento`
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
