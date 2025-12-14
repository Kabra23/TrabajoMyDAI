/**
 * Script para manejar la recarga de saldo mediante AJAX
 * Actualiza el saldo en el navbar sin redireccionar
 */
(function() {
    'use strict';

    document.addEventListener('DOMContentLoaded', function() {
        // Buscar todos los formularios de recarga de saldo (por action o por clase)
        const formsRecargaSaldo = document.querySelectorAll('form[action*="agregar-saldo"], form.form-agregar-saldo');

        console.log('Formularios de saldo encontrados:', formsRecargaSaldo.length);

        formsRecargaSaldo.forEach(function(form) {
            form.addEventListener('submit', function(e) {
                e.preventDefault();

                const cantidadInput = form.querySelector('input[name="cantidad"]');
                const cantidad = parseFloat(cantidadInput.value);

                // Validación básica
                if (!cantidad || cantidad <= 0) {
                    mostrarMensaje('La cantidad debe ser mayor que 0', 'error');
                    return;
                }

                if (cantidad > 1000) {
                    mostrarMensaje('No puedes agregar más de 1000€ a la vez', 'error');
                    return;
                }

                // Deshabilitar el botón mientras se procesa
                const submitBtn = form.querySelector('button[type="submit"]');
                const originalText = submitBtn.innerHTML;
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Procesando...';

                // Enviar petición AJAX
                fetch('/cuenta/agregar-saldo-ajax', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'cantidad=' + encodeURIComponent(cantidad)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // Actualizar el saldo en todos los lugares donde aparece
                        actualizarSaldoEnPagina(data.nuevoSaldo);

                        // Mostrar mensaje de éxito
                        mostrarMensaje(data.mensaje, 'success');

                        // Limpiar el input
                        cantidadInput.value = '';

                        // Cerrar el dropdown después de 2 segundos
                        setTimeout(function() {
                            const dropdown = form.closest('.saldo-dropdown');
                            if (dropdown) {
                                dropdown.classList.remove('show');
                            }
                        }, 2000);
                    } else {
                        mostrarMensaje(data.error || 'Error al agregar saldo', 'error');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    mostrarMensaje('Error de conexión. Inténtalo de nuevo.', 'error');
                })
                .finally(() => {
                    // Rehabilitar el botón
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                });
            });
        });
    });

    /**
     * Actualiza el saldo en todos los elementos de la página
     */
    function actualizarSaldoEnPagina(nuevoSaldo) {
        const saldoFormateado = nuevoSaldo.toFixed(2) + '€';

        // Actualizar en el botón trigger del dropdown
        const saldoAmounts = document.querySelectorAll('.saldo-amount');
        saldoAmounts.forEach(function(element) {
            element.textContent = saldoFormateado;
        });

        // Actualizar en el header del dropdown
        const saldoDisplays = document.querySelectorAll('.saldo-display');
        saldoDisplays.forEach(function(element) {
            element.textContent = saldoFormateado;
        });

        // Animar el cambio
        saldoAmounts.forEach(function(element) {
            element.style.transition = 'all 0.3s ease';
            element.style.transform = 'scale(1.2)';
            element.style.color = '#4CAF50';

            setTimeout(function() {
                element.style.transform = 'scale(1)';
                element.style.color = '';
            }, 500);
        });
    }

    /**
     * Muestra un mensaje temporal en la página
     */
    function mostrarMensaje(mensaje, tipo) {
        // Eliminar mensajes previos
        const mensajePrevio = document.querySelector('.saldo-mensaje-flotante');
        if (mensajePrevio) {
            mensajePrevio.remove();
        }

        // Crear el mensaje
        const mensajeDiv = document.createElement('div');
        mensajeDiv.className = 'saldo-mensaje-flotante';
        mensajeDiv.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            padding: 15px 25px;
            border-radius: 10px;
            color: white;
            font-weight: 600;
            z-index: 10000;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideInRight 0.3s ease;
            max-width: 350px;
        `;

        if (tipo === 'success') {
            mensajeDiv.style.background = 'linear-gradient(135deg, #4CAF50, #45a049)';
            mensajeDiv.innerHTML = '<i class="fas fa-check-circle me-2"></i>' + mensaje;
        } else {
            mensajeDiv.style.background = 'linear-gradient(135deg, #f44336, #da190b)';
            mensajeDiv.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>' + mensaje;
        }

        document.body.appendChild(mensajeDiv);

        // Eliminar después de 4 segundos
        setTimeout(function() {
            mensajeDiv.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(function() {
                mensajeDiv.remove();
            }, 300);
        }, 4000);
    }

    // Agregar animaciones CSS si no existen
    if (!document.querySelector('#saldo-animations')) {
        const style = document.createElement('style');
        style.id = 'saldo-animations';
        style.textContent = `
            @keyframes slideInRight {
                from {
                    transform: translateX(400px);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            
            @keyframes slideOutRight {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(400px);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }
})();

