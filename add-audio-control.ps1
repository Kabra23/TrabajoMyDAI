# Script para añadir control de audio a todos los archivos HTML
$audioControl = @"

<!-- Control de música de fondo flotante -->
<div style="position: fixed; bottom: 20px; right: 20px; z-index: 9998; background: rgba(0,0,0,0.8); padding: 12px 18px; border-radius: 30px; backdrop-filter: blur(10px); box-shadow: 0 4px 20px rgba(0,0,0,0.4);">
    <audio id="backgroundMusic" loop>
        <source src="/Ser-del-Barca-es.mp3" type="audio/mpeg">
    </audio>
    <div class="d-flex justify-content-center align-items-center gap-2">
        <button id="playPauseBtn" class="btn btn-sm btn-outline-light" style="border-radius: 20px;">
            <i class="fas fa-play"></i> Música
        </button>
        <input type="range" id="volumeControl" min="0" max="100" value="20"
               class="form-range" style="width: 100px;">
        <span id="volumeValue" class="small text-white">20%</span>
    </div>
</div>

<script>
// Control de música de fondo
(function() {
    var audio = document.getElementById('backgroundMusic');
    var playPauseBtn = document.getElementById('playPauseBtn');
    var volumeControl = document.getElementById('volumeControl');
    var volumeValue = document.getElementById('volumeValue');

    if (audio && playPauseBtn && volumeControl) {
        audio.volume = 0.2;

        // Intentar reproducir automáticamente
        var playPromise = audio.play();
        if (playPromise !== undefined) {
            playPromise.then(function() {
                playPauseBtn.innerHTML = '<i class="fas fa-pause"></i> Pausar';
            }).catch(function() {
                // Si falla la reproducción automática, mantener el botón de play
                playPauseBtn.innerHTML = '<i class="fas fa-play"></i> Música';
            });
        }

        playPauseBtn.addEventListener('click', function() {
            if (audio.paused) {
                audio.play();
                playPauseBtn.innerHTML = '<i class="fas fa-pause"></i> Pausar';
            } else {
                audio.pause();
                playPauseBtn.innerHTML = '<i class="fas fa-play"></i> Música';
            }
        });

        volumeControl.addEventListener('input', function() {
            audio.volume = this.value / 100;
            volumeValue.textContent = this.value + '%';
        });
    }
})();
</script>

</body>
"@

# Lista de archivos a procesar (excluyendo los que ya procesamos)
$archivos = @(
    "src\main\resources\templates\tickets.html",
    "src\main\resources\templates\mi-cuenta.html",
    "src\main\resources\templates\chatbot.html",
    "src\main\resources\templates\comprar-ticket.html",
    "src\main\resources\templates\editar-perfil.html",
    "src\main\resources\templates\eliminar-cuenta.html",
    "src\main\resources\templates\recordatorios.html",
    "src\main\resources\templates\recordatorios\list.html",
    "src\main\resources\templates\recordatorios\crear.html",
    "src\main\resources\templates\recordatorios\editar.html",
    "src\main\resources\templates\admin-users.html",
    "src\main\resources\templates\admin-user-form.html",
    "src\main\resources\templates\admin-eventos.html",
    "src\main\resources\templates\admin-editar-evento.html",
    "src\main\resources\templates\admin-crear-evento.html"
)

foreach ($archivo in $archivos) {
    $rutaCompleta = Join-Path $PSScriptRoot $archivo

    if (Test-Path $rutaCompleta) {
        Write-Host "Procesando: $archivo"

        $contenido = Get-Content $rutaCompleta -Raw -Encoding UTF8

        # Verificar si ya tiene el control de audio
        if ($contenido -match 'id="backgroundMusic"') {
            Write-Host "  - Ya tiene control de audio, omitiendo..." -ForegroundColor Yellow
            continue
        }

        # Reemplazar </body> con el control de audio + </body>
        $nuevoContenido = $contenido -replace '</body>', $audioControl

        # Guardar el archivo
        Set-Content -Path $rutaCompleta -Value $nuevoContenido -Encoding UTF8 -NoNewline

        Write-Host "  - Agregado control de audio" -ForegroundColor Green
    } else {
        Write-Host "  - No encontrado: $archivo" -ForegroundColor Red
    }
}

Write-Host "`nProceso completado!" -ForegroundColor Cyan

