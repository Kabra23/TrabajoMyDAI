// Archivo de compatibilidad - redirige al sistema Canvas 2D
function initEstadio3D() {
    console.log('⚠️ Redirigiendo a Canvas 2D');
    if (typeof inicializarEstadioCanvas === 'function') {
        inicializarEstadioCanvas();
    }
}

// Auto-inicializar si se carga este script
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initEstadio3D);
} else {
    initEstadio3D();
}
