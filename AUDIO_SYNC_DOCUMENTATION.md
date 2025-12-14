# Sistema de Audio Sincronizado - Barça Atlètic Web

## 🎵 Funcionalidades Implementadas

### Sincronización entre Páginas
El sistema de audio ahora está **completamente sincronizado** entre todas las páginas de la aplicación web usando `localStorage`. Esto significa:

✅ **Estado Persistente**: Si pausas la música en una página, permanecerá pausada al navegar a otra página
✅ **Volumen Sincronizado**: El nivel de volumen se mantiene en todas las páginas
✅ **Sincronización en Tiempo Real**: Si tienes múltiples pestañas abiertas, los cambios se sincronizan automáticamente

## 🔧 Implementación Técnica

### Archivo Principal
- **Ubicación**: `src/main/resources/static/js/audio-control.js`
- **Tecnología**: JavaScript vanilla con localStorage API

### Datos Almacenados en localStorage
- `barcaAtleticAudioState`: Estado de reproducción ('playing' o 'paused')
- `barcaAtleticAudioVolume`: Nivel de volumen (0.0 - 1.0)

### Características Técnicas

1. **Reproducción Automática Inteligente**
   - Intenta reproducir automáticamente al cargar la página
   - Si el navegador bloquea la reproducción, espera a la interacción del usuario
   - Respeta el último estado guardado

2. **Persistencia de Datos**
   - Guarda el estado antes de salir de la página (`beforeunload`)
   - Recupera el estado al cargar una nueva página
   - Maneja errores de localStorage gracefully

3. **Sincronización Multi-pestaña**
   - Usa el evento `storage` para detectar cambios en otras pestañas
   - Actualiza automáticamente el estado del audio
   - Sincroniza tanto el estado de play/pause como el volumen

## 📄 Archivos Actualizados

Todos los archivos HTML han sido actualizados para usar el script compartido:

### Páginas Principales
- index.html
- eventos.html
- noticias.html
- plantilla.html
- login.html
- registro.html

### Páginas de Usuario
- tickets.html
- mi-cuenta.html
- editar-perfil.html
- eliminar-cuenta.html
- comprar-ticket.html
- chatbot.html

### Recordatorios
- recordatorios.html
- recordatorios/list.html
- recordatorios/crear.html
- recordatorios/editar.html

### Admin
- admin-users.html
- admin-user-form.html
- admin-eventos.html
- admin-editar-evento.html
- admin-crear-evento.html

## 🎯 Cómo Funciona

### 1. Primera Carga
```javascript
// Al cargar la página por primera vez:
- El script verifica si hay estado guardado en localStorage
- Si NO hay estado guardado: intenta reproducir automáticamente al 20% de volumen
- Si hay estado guardado: restaura el estado (playing/paused) y el volumen
```

### 2. Interacción del Usuario
```javascript
// Cuando el usuario hace clic en play/pause:
- Cambia el estado del audio
- Actualiza el botón visual
- Guarda el nuevo estado en localStorage
```

### 3. Cambio de Volumen
```javascript
// Cuando el usuario ajusta el volumen:
- Cambia el volumen del audio
- Actualiza el indicador visual (%)
- Guarda el nuevo volumen en localStorage
```

### 4. Navegación entre Páginas
```javascript
// Al navegar a una nueva página:
- El script se ejecuta automáticamente
- Lee el estado guardado en localStorage
- Restaura el audio exactamente como estaba
```

### 5. Múltiples Pestañas
```javascript
// Si tienes varias pestañas abiertas:
- Los cambios en una pestaña se detectan en las demás
- Todas las pestañas se sincronizan automáticamente
- El audio se mantiene consistente en todas partes
```

## 🎨 Posicionamiento de Controles

### Páginas Generales
- **Ubicación**: Footer (parte inferior de la página)
- **Estilo**: Integrado con el diseño del footer

### Página de Plantilla
- **Ubicación**: Esquina inferior izquierda (flotante)
- **Motivo**: Evita interferir con el botón del chatbot (inferior derecha)
- **z-index**: 9998 (debajo del chatbot que es 1000)

### Páginas de Login/Registro
- **Ubicación**: Centro inferior (flotante)
- **Estilo**: Control más discreto y centrado

## 🚀 Ventajas del Sistema

1. **Experiencia de Usuario Mejorada**
   - No hay interrupciones de audio al navegar
   - El usuario tiene control total sobre el audio
   - Configuración persistente

2. **Código Mantenible**
   - Un solo archivo JavaScript para toda la aplicación
   - Fácil de actualizar y modificar
   - Sin duplicación de código

3. **Rendimiento**
   - Script ligero (~4KB)
   - Carga asíncrona
   - No afecta el tiempo de carga de la página

## 🔍 Depuración

Para ver el estado del audio en la consola del navegador:
```javascript
// Ver estado guardado
console.log(localStorage.getItem('barcaAtleticAudioState'));
console.log(localStorage.getItem('barcaAtleticAudioVolume'));

// Resetear estado (útil para pruebas)
localStorage.removeItem('barcaAtleticAudioState');
localStorage.removeItem('barcaAtleticAudioVolume');
```

## 📝 Notas Importantes

- El audio es el archivo: `/Ser-del-Barca-es.mp3`
- Volumen por defecto: 20% (0.2)
- El navegador puede bloquear la reproducción automática (política de navegadores modernos)
- La sincronización funciona solo en el mismo dominio
- localStorage tiene un límite de ~5-10MB por dominio (más que suficiente para nuestro caso)

## 🎵 Archivo de Audio

**Ubicación**: `src/main/resources/static/Ser-del-Barca-es.mp3`
**Características**:
- Loop infinito
- Reproducción de fondo
- Control de volumen dinámico

---

**Fecha de Implementación**: 13 de diciembre de 2025
**Desarrollado para**: Barça Atlètic Web Application

