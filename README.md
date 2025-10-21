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

## CRUD de las entidades principales

### Usuario (solo administradores)

- **Crear**: Registro de nuevos usuarios solo para administradores.
- **Leer**: Consulta de datos de usuario (perfil, roles, historial de compras) solo para administradores.
- **Actualizar**: Modificación de datos personales y roles solo por administradores.
- **Eliminar**: Baja de usuario solo por administradores.


## Funcionalidades principales

- Solo los administradores pueden registrarse y acceder mediante login.
- Los usuarios aficionados no pueden registrarse ni iniciar sesión.

---

### Entrada (Ticket)

- **Crear**: Compra de entradas, generando un ticket personal con código único.
- **Leer**: Consulta de entradas disponibles y entradas adquiridas por usuario.
- **Actualizar**: Modificación de datos de la entrada (por ejemplo, cambio de asiento antes del partido, si la política lo permite).
- **Eliminar**: Cancelación de entradas (según condiciones del club).

### Noticia

- **Crear**: Publicación de nuevas noticias por administradores.
- **Leer**: Visualización de noticias y detalles por parte de todos los usuarios.
- **Actualizar**: Edición de noticias existentes (solo administradores).
- **Eliminar**: Eliminación de noticias (solo administradores).

---

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

## Contacto

Para dudas o sugerencias, contacta a los integrantes del proyecto.

---

Puedes personalizar las rutas, imágenes y detalles según tu implementación real.