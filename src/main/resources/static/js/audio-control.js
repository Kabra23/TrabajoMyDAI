/**
 * Control de música de fondo sincronizado entre páginas
 * Utiliza localStorage para mantener el estado entre navegaciones
 */
(function() {
    'use strict';

    // Configuración
    const AUDIO_STATE_KEY = 'barcaAtleticAudioState';
    const AUDIO_VOLUME_KEY = 'barcaAtleticAudioVolume';
    const DEFAULT_VOLUME = 0.2; // 20%

    // Función para inicializar el control de audio
    function initAudioControl() {
        const audio = document.getElementById('backgroundMusic');
        const playPauseBtn = document.getElementById('playPauseBtn');
        const volumeControl = document.getElementById('volumeControl');
        const volumeValue = document.getElementById('volumeValue');

        if (!audio || !playPauseBtn || !volumeControl || !volumeValue) {
            console.warn('Elementos de control de audio no encontrados');
            return;
        }

        // Recuperar estado guardado
        const savedState = localStorage.getItem(AUDIO_STATE_KEY);
        const savedVolume = localStorage.getItem(AUDIO_VOLUME_KEY);

        // Configurar volumen
        const volume = savedVolume ? parseFloat(savedVolume) : DEFAULT_VOLUME;
        audio.volume = volume;
        volumeControl.value = Math.round(volume * 100);
        volumeValue.textContent = Math.round(volume * 100) + '%';

        // Configurar estado de reproducción - SIEMPRE intentar reproducir por defecto
        // Solo pausar si el usuario lo pausó explícitamente antes
        const shouldPlay = savedState !== 'paused';

        if (shouldPlay) {
            // Intentar reproducir automáticamente
            const playPromise = audio.play();
            if (playPromise !== undefined) {
                playPromise.then(function() {
                    updateButton(playPauseBtn, true);
                    saveState('playing');
                }).catch(function(error) {
                    console.log('Reproducción automática bloqueada por el navegador:', error);
                    console.log('El usuario debe interactuar con la página para iniciar el audio');
                    updateButton(playPauseBtn, false);
                    // No guardar como pausado, para que intente de nuevo en la siguiente página
                });
            }
        } else {
            audio.pause();
            updateButton(playPauseBtn, false);
        }

        // Event listeners
        playPauseBtn.addEventListener('click', function() {
            if (audio.paused) {
                audio.play();
                updateButton(playPauseBtn, true);
                saveState('playing');
            } else {
                audio.pause();
                updateButton(playPauseBtn, false);
                saveState('paused');
            }
        });

        volumeControl.addEventListener('input', function() {
            const newVolume = this.value / 100;
            audio.volume = newVolume;
            volumeValue.textContent = this.value + '%';
            saveVolume(newVolume);
        });

        // Guardar estado al salir de la página
        window.addEventListener('beforeunload', function() {
            if (!audio.paused) {
                saveState('playing');
            } else {
                saveState('paused');
            }
        });

        // Sincronizar con otras pestañas
        window.addEventListener('storage', function(e) {
            if (e.key === AUDIO_STATE_KEY) {
                if (e.newValue === 'playing' && audio.paused) {
                    audio.play();
                    updateButton(playPauseBtn, true);
                } else if (e.newValue === 'paused' && !audio.paused) {
                    audio.pause();
                    updateButton(playPauseBtn, false);
                }
            } else if (e.key === AUDIO_VOLUME_KEY && e.newValue) {
                const newVolume = parseFloat(e.newValue);
                audio.volume = newVolume;
                volumeControl.value = Math.round(newVolume * 100);
                volumeValue.textContent = Math.round(newVolume * 100) + '%';
            }
        });
    }

    // Función para actualizar el botón
    function updateButton(button, isPlaying) {
        if (isPlaying) {
            button.innerHTML = '<i class="fas fa-pause"></i> Pausar';
        } else {
            button.innerHTML = '<i class="fas fa-play"></i> Música';
        }
    }

    // Función para guardar el estado
    function saveState(state) {
        try {
            localStorage.setItem(AUDIO_STATE_KEY, state);
        } catch (e) {
            console.warn('No se pudo guardar el estado del audio:', e);
        }
    }

    // Función para guardar el volumen
    function saveVolume(volume) {
        try {
            localStorage.setItem(AUDIO_VOLUME_KEY, volume.toString());
        } catch (e) {
            console.warn('No se pudo guardar el volumen del audio:', e);
        }
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAudioControl);
    } else {
        initAudioControl();
    }
})();

