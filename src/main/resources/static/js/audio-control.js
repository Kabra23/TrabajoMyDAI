/**
 * Control de música de fondo sincronizado entre páginas con AUTOPLAY
 * Utiliza localStorage para mantener el estado entre navegaciones
 */
(function() {
    'use strict';

    // Configuración
    const AUDIO_STATE_KEY = 'barcaAtleticAudioState';
    const AUDIO_VOLUME_KEY = 'barcaAtleticAudioVolume';
    const AUDIO_TIME_KEY = 'barcaAtleticAudioTime';
    const DEFAULT_VOLUME = 0.2; // 20%

    // Canal de comunicación entre pestañas (sincronización en tiempo real)
    let audioChannel = null;
    if (typeof BroadcastChannel !== 'undefined') {
        audioChannel = new BroadcastChannel('barcaAtleticAudioChannel');
        console.log('📡 BroadcastChannel inicializado para sincronización');
    }

    // Función para inicializar el control de audio
    function initAudioControl() {
        const audio = document.getElementById('backgroundMusic');
        const playPauseBtn = document.getElementById('playPauseBtn');
        const volumeControl = document.getElementById('volumeControl');
        const volumeValue = document.getElementById('volumeValue');

        if (!audio || !playPauseBtn || !volumeControl || !volumeValue) {
            console.warn('⚠️ Elementos de control de audio no encontrados');
            return;
        }

        console.log('🎵 Inicializando control de audio');

        // Recuperar configuración guardada
        const savedVolume = localStorage.getItem(AUDIO_VOLUME_KEY);
        const savedTime = localStorage.getItem(AUDIO_TIME_KEY);

        // Configurar volumen
        const volume = savedVolume ? parseFloat(savedVolume) : DEFAULT_VOLUME;
        audio.volume = volume;
        volumeControl.value = Math.round(volume * 100);
        volumeValue.textContent = Math.round(volume * 100) + '%';

        // Restaurar posición de reproducción si existe
        if (savedTime && !isNaN(parseFloat(savedTime))) {
            audio.currentTime = parseFloat(savedTime);
        }

        // AUTOPLAY: Siempre intentar reproducir automáticamente al iniciar
        // El usuario decidirá si pausarlo después
        console.log('▶️ Intentando reproducción automática...');

        // Esperar a que el audio esté listo
        audio.addEventListener('canplay', function startPlayback() {
            const playPromise = audio.play();

                if (playPromise !== undefined) {
                    playPromise
                        .then(function() {
                            console.log('✅ Reproducción automática exitosa');
                            updateButton(playPauseBtn, true);
                            saveState('playing');
                        })
                        .catch(function(error) {
                            console.log('⚠️ Reproducción automática bloqueada:', error.message);
                            console.log('💡 Solución: El usuario debe interactuar con la página primero');
                            updateButton(playPauseBtn, false);
                            saveState('paused');

                            // Intentar reproducir después de cualquier interacción del usuario
                            setupAutoplayOnInteraction(audio, playPauseBtn);
                        });
                }

                // Remover el listener después de intentar
                audio.removeEventListener('canplay', startPlayback);
            }, { once: true });

            // Forzar la carga del audio
            audio.load();

        // Escuchar mensajes de otras pestañas a través de BroadcastChannel
        if (audioChannel) {
            audioChannel.onmessage = function(event) {
                const { action, value } = event.data;
                console.log('📨 Mensaje recibido de otra pestaña:', action, value);

                switch(action) {
                    case 'play':
                        if (audio.paused) {
                            audio.play();
                            updateButton(playPauseBtn, true);
                        }
                        break;
                    case 'pause':
                        if (!audio.paused) {
                            audio.pause();
                            updateButton(playPauseBtn, false);
                        }
                        break;
                    case 'volume':
                        audio.volume = value;
                        volumeControl.value = Math.round(value * 100);
                        volumeValue.textContent = Math.round(value * 100) + '%';
                        break;
                    case 'seek':
                        audio.currentTime = value;
                        break;
                }
            };
        }

        // Event listeners
        playPauseBtn.addEventListener('click', function() {
            if (audio.paused) {
                console.log('▶️ Usuario inició reproducción');
                audio.play();
                updateButton(playPauseBtn, true);
                saveState('playing');
                // Notificar a otras pestañas
                broadcastMessage('play');
            } else {
                console.log('⏸️ Usuario pausó reproducción');
                audio.pause();
                updateButton(playPauseBtn, false);
                saveState('paused');
                // Notificar a otras pestañas
                broadcastMessage('pause');
            }
        });

        volumeControl.addEventListener('input', function() {
            const newVolume = this.value / 100;
            audio.volume = newVolume;
            volumeValue.textContent = this.value + '%';
            saveVolume(newVolume);
            // Notificar a otras pestañas
            broadcastMessage('volume', newVolume);
        });

        // Guardar posición de reproducción periódicamente
        setInterval(function() {
            if (!audio.paused) {
                saveTime(audio.currentTime);
            }
        }, 5000); // Cada 5 segundos

        // Guardar estado al salir de la página
        window.addEventListener('beforeunload', function() {
            saveTime(audio.currentTime);
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

        // Manejar cuando el audio termina (loop automático)
        audio.addEventListener('ended', function() {
            if (localStorage.getItem(AUDIO_STATE_KEY) === 'playing') {
                audio.currentTime = 0;
                audio.play();
            }
        });
    }

    // Configurar reproducción automática después de interacción del usuario
    function setupAutoplayOnInteraction(audio, button) {
        const events = ['click', 'touchstart', 'keydown'];

        function tryPlay() {
            console.log('👆 Usuario interactuó - intentando reproducción...');
            const playPromise = audio.play();

            if (playPromise !== undefined) {
                playPromise
                    .then(function() {
                        console.log('✅ Reproducción iniciada después de interacción');
                        updateButton(button, true);
                        saveState('playing');

                        // Remover listeners después de éxito
                        events.forEach(event => {
                            document.removeEventListener(event, tryPlay);
                        });
                    })
                    .catch(function(error) {
                        console.log('⚠️ Aún no se puede reproducir:', error.message);
                    });
            }
        }

        // Agregar listeners para cualquier interacción
        events.forEach(event => {
            document.addEventListener(event, tryPlay, { once: true });
        });
    }

    // Función para enviar mensajes a otras pestañas
    function broadcastMessage(action, value = null) {
        if (audioChannel) {
            const message = { action, value };
            audioChannel.postMessage(message);
            console.log('📤 Mensaje enviado a otras pestañas:', message);
        }
    }

    // Función para actualizar el botón
    function updateButton(button, isPlaying) {
        if (isPlaying) {
            button.innerHTML = '<i class="fas fa-pause"></i> Pausar';
            button.classList.add('playing');
        } else {
            button.innerHTML = '<i class="fas fa-play"></i> Música';
            button.classList.remove('playing');
        }
    }

    // Función para guardar el estado
    function saveState(state) {
        try {
            localStorage.setItem(AUDIO_STATE_KEY, state);
            console.log('💾 Estado guardado:', state);
        } catch (e) {
            console.warn('⚠️ No se pudo guardar el estado del audio:', e);
        }
    }

    // Función para guardar el volumen
    function saveVolume(volume) {
        try {
            localStorage.setItem(AUDIO_VOLUME_KEY, volume.toString());
        } catch (e) {
            console.warn('⚠️ No se pudo guardar el volumen del audio:', e);
        }
    }

    // Función para guardar la posición de reproducción
    function saveTime(time) {
        try {
            localStorage.setItem(AUDIO_TIME_KEY, time.toString());
        } catch (e) {
            console.warn('⚠️ No se pudo guardar la posición del audio:', e);
        }
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAudioControl);
    } else {
        initAudioControl();
    }

    console.log('🎵 Script de control de audio cargado');
})();