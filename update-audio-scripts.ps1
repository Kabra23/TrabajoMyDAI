# Script para actualizar los archivos HTML con el script de audio compartido
$scriptTag = '<script src="/js/audio-control.js"></script>'

# Lista de archivos que tienen el control de audio inline y necesitan actualización
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

$patronScript = '(?s)<script>\s*\/\/ Control de música de fondo.*?<\/script>'

foreach ($archivo in $archivos) {
    $rutaCompleta = Join-Path $PSScriptRoot $archivo

    if (Test-Path $rutaCompleta) {
        Write-Host "Procesando: $archivo"

        $contenido = Get-Content $rutaCompleta -Raw -Encoding UTF8

        # Verificar si ya tiene el script compartido
        if ($contenido -match '/js/audio-control\.js') {
            Write-Host "  - Ya tiene el script compartido, omitiendo..." -ForegroundColor Yellow
            continue
        }

        # Buscar y reemplazar el script inline
        if ($contenido -match $patronScript) {
            $nuevoContenido = $contenido -replace $patronScript, $scriptTag

            # Guardar el archivo
            Set-Content -Path $rutaCompleta -Value $nuevoContenido -Encoding UTF8 -NoNewline

            Write-Host "  - Script actualizado" -ForegroundColor Green
        } else {
            # Si no tiene script inline pero tiene el div de control, añadir el script antes de </body>
            if ($contenido -match 'id="backgroundMusic"') {
                $nuevoContenido = $contenido -replace '</body>', "$scriptTag`n</body>"
                Set-Content -Path $rutaCompleta -Value $nuevoContenido -Encoding UTF8 -NoNewline
                Write-Host "  - Script añadido" -ForegroundColor Green
            } else {
                Write-Host "  - No tiene control de audio" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "  - No encontrado: $archivo" -ForegroundColor Red
    }
}

Write-Host "`nProceso completado!" -ForegroundColor Cyan

