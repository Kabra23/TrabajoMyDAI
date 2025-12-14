// Variables globales
let scene, camera, renderer, controls;
let raycaster, mouse;
let asientos = [];
let asientoSeleccionado = null;
let asientosOcupados = {};
let zonasData = [];
let eventoId;
let lastMouseMoveTime = 0;
let hoveredAsiento = null;

// Colores
const COLORS = {
    disponible: 0x4CAF50,
    ocupado: 0xf44336,
    seleccionado: 0x2196F3,
    hover: 0xFFC107,
    campo: 0x7CB342,
    tribuna: 0x8B4513
};

/**
 * Inicializar la escena 3D
 */
function init(idEvento) {
    eventoId = idEvento;

    // Actualizar loading
    updateLoadingProgress('Inicializando escena...', 10);

    // Crear escena
    scene = new THREE.Scene();
    scene.background = new THREE.Color(0x87CEEB);
    scene.fog = new THREE.Fog(0x87CEEB, 100, 300);

    // Crear cámara
    const container = document.getElementById('canvas-container');
    camera = new THREE.PerspectiveCamera(
        60,
        container.clientWidth / container.clientHeight,
        0.1,
        1000
    );
    camera.position.set(0, 80, 100);
    camera.lookAt(0, 0, 0);

    updateLoadingProgress('Configurando renderizado...', 30);

    // Crear renderer (optimizado para rendimiento)
    renderer = new THREE.WebGLRenderer({ antialias: false, powerPreference: "high-performance" });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.shadowMap.enabled = false; // Deshabilitado para mejor rendimiento
    container.appendChild(renderer.domElement);

    // Controles de cámara
    controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    controls.minDistance = 30;
    controls.maxDistance = 200;
    controls.maxPolarAngle = Math.PI / 2.1;

    // Raycaster para detección de clicks
    raycaster = new THREE.Raycaster();
    mouse = new THREE.Vector2();

    updateLoadingProgress('Creando iluminación...', 50);

    // Luces (optimizadas)
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.6);
    directionalLight.position.set(50, 100, 50);
    scene.add(directionalLight);

    updateLoadingProgress('Construyendo estadio...', 60);

    // Crear el estadio
    crearEstadio();

    // Eventos
    window.addEventListener('resize', onWindowResize, false);
    renderer.domElement.addEventListener('mousemove', onMouseMove, false);
    renderer.domElement.addEventListener('click', onClick, false);

    updateLoadingProgress('Cargando información...', 80);

    // Cargar datos de la API
    cargarDatosEstadio();

    // Iniciar animación
    animate();
}

/**
 * Actualizar progreso de carga
 */
function updateLoadingProgress(message, percent) {
    const loadingDiv = document.getElementById('loading');
    if (loadingDiv) {
        const progressText = loadingDiv.querySelector('p');
        if (progressText) {
            progressText.textContent = message + ' (' + percent + '%)';
        }
    }
}

/**
 * Crear el modelo del estadio
 */
function crearEstadio() {
    // Campo de fútbol
    const campoGeometry = new THREE.PlaneGeometry(70, 100);
    const campoMaterial = new THREE.MeshLambertMaterial({ color: COLORS.campo });
    const campo = new THREE.Mesh(campoGeometry, campoMaterial);
    campo.rotation.x = -Math.PI / 2;
    scene.add(campo);

    // Líneas del campo
    crearLineasCampo();

    // Crear zonas de asientos (optimizado para mejor rendimiento)
    crearZonaAsientos('Tribuna', { x: 0, y: 0, z: -60 }, 10, 15, 0);
    crearZonaAsientos('Grada Lateral', { x: -45, y: 0, z: 0 }, 12, 10, Math.PI / 2);
    crearZonaAsientos('Grada Lateral', { x: 45, y: 0, z: 0 }, 12, 10, -Math.PI / 2);
    crearZonaAsientos('Gol Nord', { x: 0, y: 0, z: 60 }, 8, 15, Math.PI);
    crearZonaAsientos('Gol Sud', { x: 0, y: 0, z: -60 }, 8, 15, 0);
}

/**
 * Crear líneas del campo de fútbol
 */
function crearLineasCampo() {
    const lineaMaterial = new THREE.LineBasicMaterial({ color: 0xffffff });

    // Líneas perimetrales
    const points = [];
    points.push(new THREE.Vector3(-35, 0.1, -50));
    points.push(new THREE.Vector3(35, 0.1, -50));
    points.push(new THREE.Vector3(35, 0.1, 50));
    points.push(new THREE.Vector3(-35, 0.1, 50));
    points.push(new THREE.Vector3(-35, 0.1, -50));

    const geometry = new THREE.BufferGeometry().setFromPoints(points);
    const line = new THREE.Line(geometry, lineaMaterial);
    scene.add(line);

    // Línea central
    const centralPoints = [
        new THREE.Vector3(-35, 0.1, 0),
        new THREE.Vector3(35, 0.1, 0)
    ];
    const centralGeometry = new THREE.BufferGeometry().setFromPoints(centralPoints);
    const centralLine = new THREE.Line(centralGeometry, lineaMaterial);
    scene.add(centralLine);

    // Círculo central
    const circleGeometry = new THREE.CircleGeometry(9, 32);
    const circleEdges = new THREE.EdgesGeometry(circleGeometry);
    const circleLine = new THREE.LineSegments(circleEdges, lineaMaterial);
    circleLine.rotation.x = -Math.PI / 2;
    circleLine.position.y = 0.1;
    scene.add(circleLine);
}

/**
 * Crear zona de asientos
 * Optimizado: Usa geometría y material compartidos para mejor rendimiento
 */
function crearZonaAsientos(nombreZona, posicion, filas, asientosPorFila, rotacion) {
    const asientoSize = 0.5;
    const asientoGap = 0.1;
    const filaHeight = 0.3;

    let asientoNumero = 1;

    // Compartir geometría y materiales para mejor rendimiento
    const geometry = new THREE.BoxGeometry(asientoSize, asientoSize, asientoSize);
    const materialDisponible = new THREE.MeshLambertMaterial({ color: COLORS.disponible });

    for (let fila = 0; fila < filas; fila++) {
        for (let asiento = 0; asiento < asientosPorFila; asiento++) {
            // Reutilizar geometría (no crear una nueva cada vez)
            const asientoMesh = new THREE.Mesh(geometry, materialDisponible.clone());

            // Posición local del asiento
            const x = (asiento - asientosPorFila / 2) * (asientoSize + asientoGap);
            const y = fila * filaHeight;
            const z = fila * (asientoSize + asientoGap);

            // Rotar y posicionar según la zona
            const pos = new THREE.Vector3(x, y, z);
            pos.applyAxisAngle(new THREE.Vector3(0, 1, 0), rotacion);

            asientoMesh.position.set(
                posicion.x + pos.x,
                posicion.y + pos.y + 1,
                posicion.z + pos.z
            );
            asientoMesh.rotation.y = rotacion;

            // Metadata del asiento
            asientoMesh.userData = {
                zona: nombreZona,
                numero: asientoNumero,
                disponible: true,
                precio: 0
            };

            asientos.push(asientoMesh);
            scene.add(asientoMesh);

            asientoNumero++;
        }
    }
}

/**
 * Cargar datos del estadio desde la API
 */
async function cargarDatosEstadio() {
    try {
        // Mostrar loading
        document.getElementById('loading').style.display = 'flex';

        // Crear promesa con timeout
        const timeoutPromise = new Promise((_, reject) =>
            setTimeout(() => reject(new Error('Timeout: La carga tardó demasiado')), 10000)
        );

        // Cargar zonas con timeout
        const zonasResponse = await Promise.race([
            fetch(`/api/eventos/${eventoId}/estadio/zonas`),
            timeoutPromise
        ]);

        if (!zonasResponse.ok) {
            throw new Error(`Error HTTP: ${zonasResponse.status}`);
        }

        const zonasResult = await zonasResponse.json();
        zonasData = zonasResult.zonas;

        // Cargar asientos ocupados con timeout
        const ocupadosResponse = await Promise.race([
            fetch(`/api/eventos/${eventoId}/estadio/asientos-ocupados`),
            timeoutPromise
        ]);

        if (!ocupadosResponse.ok) {
            console.warn('No se pudieron cargar asientos ocupados, usando valores por defecto');
            asientosOcupados = {};
        } else {
            asientosOcupados = await ocupadosResponse.json();
        }

        // Actualizar precios y disponibilidad
        actualizarAsientos();

        // Ocultar loading
        document.getElementById('loading').style.display = 'none';

        // Actualizar panel de información
        actualizarPanelInfo();

    } catch (error) {
        console.error('Error al cargar datos del estadio:', error);
        document.getElementById('loading').innerHTML =
            '<div style="color: white; text-align: center; padding: 20px;">' +
            '<h3>⚠️ Error al cargar el estadio</h3>' +
            '<p style="margin: 15px 0;">' + error.message + '</p>' +
            '<button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; cursor: pointer; background: #2196F3; color: white; border: none; border-radius: 5px; font-size: 16px;">🔄 Reintentar</button>' +
            '<br><br>' +
            '<a href="/eventos/' + eventoId + '/comprar" style="color: #FFC107; text-decoration: underline;">← Volver a la vista 2D</a>' +
            '</div>';
    }
}

/**
 * Actualizar estado de asientos según datos de la API
 */
function actualizarAsientos() {
    asientos.forEach(asiento => {
        const zona = asiento.userData.zona;
        const numero = asiento.userData.numero;

        // Buscar información de la zona
        const zonaInfo = zonasData.find(z => z.nombre === zona);
        if (zonaInfo) {
            asiento.userData.precio = zonaInfo.precio;
        }

        // Verificar si está ocupado
        const ocupados = asientosOcupados[zona] || [];
        const estaOcupado = ocupados.includes(numero);

        asiento.userData.disponible = !estaOcupado;
        asiento.material.color.setHex(estaOcupado ? COLORS.ocupado : COLORS.disponible);
    });
}

/**
 * Actualizar panel de información
 */
function actualizarPanelInfo() {
    let html = '<h3>Información del Estadio</h3>';

    zonasData.forEach(zona => {
        html += `
            <div class="info-row">
                <span class="info-label">${zona.nombre}</span>
                <span class="info-value">${zona.disponibles}/${zona.capacidadTotal}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Precio</span>
                <span class="info-value">${zona.precio.toFixed(2)}€</span>
            </div>
        `;
    });

    document.getElementById('zona-info').innerHTML = html;
}

/**
 * Manejar movimiento del mouse
 * Optimizado: throttling para evitar llamadas excesivas
 */
function onMouseMove(event) {
    // Throttling: solo procesar cada 50ms
    const now = Date.now();
    if (now - lastMouseMoveTime < 50) {
        return;
    }
    lastMouseMoveTime = now;

    const rect = renderer.domElement.getBoundingClientRect();
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(asientos);

    // Resetear color del asiento previamente en hover
    if (hoveredAsiento && hoveredAsiento !== asientoSeleccionado) {
        hoveredAsiento.material.color.setHex(
            hoveredAsiento.userData.disponible ? COLORS.disponible : COLORS.ocupado
        );
        hoveredAsiento = null;
    }

    // Tooltip
    const tooltip = document.getElementById('tooltip');

    if (intersects.length > 0) {
        const asiento = intersects[0].object;

        if (asiento.userData.disponible && asiento !== asientoSeleccionado) {
            asiento.material.color.setHex(COLORS.hover);
            hoveredAsiento = asiento;
        }

        // Mostrar tooltip
        tooltip.style.display = 'block';
        tooltip.style.left = event.clientX + 10 + 'px';
        tooltip.style.top = event.clientY + 10 + 'px';
        tooltip.innerHTML = `
            <strong>${asiento.userData.zona}</strong><br>
            Asiento #${asiento.userData.numero}<br>
            Precio: ${asiento.userData.precio.toFixed(2)}€<br>
            ${asiento.userData.disponible ? '<span style="color: #4CAF50;">Disponible</span>' : '<span style="color: #f44336;">Ocupado</span>'}
        `;

        renderer.domElement.style.cursor = asiento.userData.disponible ? 'pointer' : 'not-allowed';
    } else {
        tooltip.style.display = 'none';
        renderer.domElement.style.cursor = 'default';
    }
}

/**
 * Manejar clicks en asientos
 */
function onClick(event) {
    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(asientos);

    if (intersects.length > 0) {
        const asiento = intersects[0].object;

        if (asiento.userData.disponible) {
            // Deseleccionar asiento anterior
            if (asientoSeleccionado) {
                asientoSeleccionado.material.color.setHex(COLORS.disponible);
            }

            // Seleccionar nuevo asiento
            asientoSeleccionado = asiento;
            asiento.material.color.setHex(COLORS.seleccionado);

            // Mostrar información del asiento
            mostrarInfoAsiento(asiento);
        }
    }
}

/**
 * Mostrar información del asiento seleccionado
 */
function mostrarInfoAsiento(asiento) {
    const infoDiv = document.getElementById('asiento-info');
    infoDiv.style.display = 'block';
    infoDiv.innerHTML = `
        <h4>Asiento Seleccionado</h4>
        <div class="asiento-detail"><strong>Zona:</strong> ${asiento.userData.zona}</div>
        <div class="asiento-detail"><strong>Número:</strong> ${asiento.userData.numero}</div>
        <div class="asiento-detail"><strong>Precio:</strong> ${asiento.userData.precio.toFixed(2)}€</div>
    `;

    document.getElementById('confirmar-btn').style.display = 'block';
}

/**
 * Confirmar compra
 */
function confirmarCompra() {
    if (asientoSeleccionado) {
        const zona = asientoSeleccionado.userData.zona;
        const numero = asientoSeleccionado.userData.numero;

        // Redirigir al formulario de compra con los datos pre-seleccionados
        window.location.href = `/eventos/${eventoId}/comprar?zona=${encodeURIComponent(zona)}&asiento=${numero}`;
    }
}

/**
 * Resetear vista de cámara
 */
function resetearVista() {
    camera.position.set(0, 80, 100);
    controls.target.set(0, 0, 0);
    controls.update();
}

/**
 * Manejar redimensionamiento de ventana
 */
function onWindowResize() {
    const container = document.getElementById('canvas-container');
    camera.aspect = container.clientWidth / container.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
}

/**
 * Loop de animación
 */
function animate() {
    requestAnimationFrame(animate);
    controls.update();
    renderer.render(scene, camera);
}

// Exponer funciones globales
window.initEstadio3D = init;
window.confirmarCompra = confirmarCompra;
window.resetearVista = resetearVista;