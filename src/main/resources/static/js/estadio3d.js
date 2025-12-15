// Variables globales
let scene, camera, renderer, controls;
let raycaster, mouse;
let asientos = [];
let asientoSeleccionado = null;
let asientosOcupados = {};
let zonasData = [];
let eventoId;

// Colores
const COLORS = {
    disponible: 0x4CAF50,
    ocupado: 0xf44336,
    seleccionado: 0x2196F3,
    hover: 0xFFC107,
    campo: 0x7CB342,
    cesped: 0x2E7D32,
    lineas: 0xFFFFFF,
    tribuna: 0xFF8C00,  // Naranja premium para zona Tribuna
    grada: 0x283593     // Azul para Grada normal
};

/**
 * Inicializar la escena 3D
 */
function init(idEvento) {
    eventoId = idEvento;
    console.log('🎮 Iniciando estadio 3D para evento:', eventoId);

    updateLoadingProgress('Inicializando escena...', 10);

    // Crear escena
    scene = new THREE.Scene();
    scene.background = new THREE.Color(0x87CEEB);
    scene.fog = new THREE.Fog(0x87CEEB, 150, 400);

    // Crear cámara
    const container = document.getElementById('canvas-container');
    camera = new THREE.PerspectiveCamera(
        50,
        container.clientWidth / container.clientHeight,
        0.1,
        1000
    );
    camera.position.set(0, 100, 150);
    camera.lookAt(0, 0, 0);

    updateLoadingProgress('Configurando renderizado...', 20);

    // Crear renderer
    renderer = new THREE.WebGLRenderer({
        antialias: true,
        powerPreference: "high-performance"
    });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.shadowMap.enabled = false; // Deshabilitado para mejor rendimiento
    container.appendChild(renderer.domElement);

    updateLoadingProgress('Configurando controles...', 30);

    // Controles de cámara (implementación simple sin OrbitControls)
    setupSimpleControls();

    // Raycaster para detección de clicks
    raycaster = new THREE.Raycaster();
    mouse = new THREE.Vector2();

    updateLoadingProgress('Creando iluminación...', 40);

    // Luces
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.7);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
    directionalLight.position.set(50, 100, 50);
    scene.add(directionalLight);

    updateLoadingProgress('Construyendo estadio...', 50);

    // Crear el estadio
    crearEstadio();

    updateLoadingProgress('Cargando información...', 70);

    // Eventos
    window.addEventListener('resize', onWindowResize, false);
    renderer.domElement.addEventListener('mousemove', onMouseMove, false);
    renderer.domElement.addEventListener('click', onClick, false);

    // Cargar datos de la API
    cargarDatosEstadio();

    // Iniciar animación
    animate();
}

/**
 * Configurar controles simples sin OrbitControls
 */
function setupSimpleControls() {
    let isDragging = false;
    let previousMousePosition = { x: 0, y: 0 };
    let cameraRotation = { x: 0, y: 0 };
    let cameraDistance = 150;

    renderer.domElement.addEventListener('mousedown', function(e) {
        isDragging = true;
        previousMousePosition = { x: e.clientX, y: e.clientY };
    });

    renderer.domElement.addEventListener('mousemove', function(e) {
        if (isDragging) {
            const deltaX = e.clientX - previousMousePosition.x;
            const deltaY = e.clientY - previousMousePosition.y;

            cameraRotation.y += deltaX * 0.005;
            cameraRotation.x += deltaY * 0.005;

            // Limitar rotación vertical
            cameraRotation.x = Math.max(-Math.PI / 3, Math.min(Math.PI / 3, cameraRotation.x));

            updateCameraPosition();

            previousMousePosition = { x: e.clientX, y: e.clientY };
        }
    });

    renderer.domElement.addEventListener('mouseup', function() {
        isDragging = false;
    });

    renderer.domElement.addEventListener('wheel', function(e) {
        e.preventDefault();
        cameraDistance += e.deltaY * 0.1;
        cameraDistance = Math.max(50, Math.min(250, cameraDistance));
        updateCameraPosition();
    });

    function updateCameraPosition() {
        camera.position.x = cameraDistance * Math.sin(cameraRotation.y) * Math.cos(cameraRotation.x);
        camera.position.y = cameraDistance * Math.sin(cameraRotation.x) + 50;
        camera.position.z = cameraDistance * Math.cos(cameraRotation.y) * Math.cos(cameraRotation.x);
        camera.lookAt(0, 0, 0);
    }
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
    // Campo de fútbol con textura de césped
    const campoGeometry = new THREE.PlaneGeometry(70, 105);
    const campoMaterial = new THREE.MeshLambertMaterial({
        color: COLORS.cesped,
        side: THREE.DoubleSide
    });
    const campo = new THREE.Mesh(campoGeometry, campoMaterial);
    campo.rotation.x = -Math.PI / 2;
    scene.add(campo);

    // Líneas del campo
    crearLineasCampo();

    // Porterías
    crearPorteria({ x: 0, y: 0, z: -52 });
    crearPorteria({ x: 0, y: 0, z: 52 });

    // Crear zonas de asientos (4 lados del estadio)
    // Gol Sud - reducido a 12 filas x 40 asientos = 480
    crearZonaAsientos('Gol Sud', { x: 0, y: 0, z: -65 }, 12, 40, 0);
    // Gol Nord - reducido a 12 filas x 40 asientos = 480
    crearZonaAsientos('Gol Nord', { x: 0, y: 0, z: 65 }, 12, 40, Math.PI);
    // Grada Lateral con Tribuna integrada - 15 filas x 40 asientos = 600 por lado
    crearZonaAsientosMixta({ x: -45, y: 0, z: 0 }, 15, 40, Math.PI / 2);
    crearZonaAsientosMixta({ x: 45, y: 0, z: 0 }, 15, 40, -Math.PI / 2);

    console.log('✅ Estadio creado con', asientos.length, 'asientos');
}

/**
 * Crear líneas del campo
 */
function crearLineasCampo() {
    const lineaMaterial = new THREE.LineBasicMaterial({ color: COLORS.lineas });

    // Perímetro
    const perimetro = [
        new THREE.Vector3(-35, 0.1, -52.5),
        new THREE.Vector3(35, 0.1, -52.5),
        new THREE.Vector3(35, 0.1, 52.5),
        new THREE.Vector3(-35, 0.1, 52.5),
        new THREE.Vector3(-35, 0.1, -52.5)
    ];
    const perimetroGeometry = new THREE.BufferGeometry().setFromPoints(perimetro);
    scene.add(new THREE.Line(perimetroGeometry, lineaMaterial));

    // Línea central
    const central = [
        new THREE.Vector3(-35, 0.1, 0),
        new THREE.Vector3(35, 0.1, 0)
    ];
    const centralGeometry = new THREE.BufferGeometry().setFromPoints(central);
    scene.add(new THREE.Line(centralGeometry, lineaMaterial));

    // Círculo central
    const circleGeometry = new THREE.CircleGeometry(9.15, 32);
    const circleEdges = new THREE.EdgesGeometry(circleGeometry);
    const circleLine = new THREE.LineSegments(circleEdges, lineaMaterial);
    circleLine.rotation.x = -Math.PI / 2;
    circleLine.position.y = 0.1;
    scene.add(circleLine);
}

/**
 * Crear portería
 */
function crearPorteria(posicion) {
    const materialPorteria = new THREE.MeshLambertMaterial({ color: 0xFFFFFF });
    const posteRadius = 0.12;
    const posteHeight = 2.44;
    const ancho = 7.32;

    // Postes
    const posteGeometry = new THREE.CylinderGeometry(posteRadius, posteRadius, posteHeight, 8);

    const posteIzq = new THREE.Mesh(posteGeometry, materialPorteria);
    posteIzq.position.set(posicion.x - ancho/2, posteHeight/2, posicion.z);
    scene.add(posteIzq);

    const posteDer = new THREE.Mesh(posteGeometry, materialPorteria);
    posteDer.position.set(posicion.x + ancho/2, posteHeight/2, posicion.z);
    scene.add(posteDer);

    // Travesaño
    const travesanoGeometry = new THREE.CylinderGeometry(posteRadius, posteRadius, ancho, 8);
    const travesano = new THREE.Mesh(travesanoGeometry, materialPorteria);
    travesano.rotation.z = Math.PI / 2;
    travesano.position.set(posicion.x, posteHeight, posicion.z);
    scene.add(travesano);
}

/**
 * Crear zona de asientos optimizada
 */
function crearZonaAsientos(nombreZona, posicion, filas, asientosPorFila, rotacion) {
    const asientoSize = 0.8;
    const asientoGap = 0.15;
    const filaHeight = 0.5;
    const filaDepth = 0.8;

    let asientoNumero = 1;

    for (let fila = 0; fila < filas; fila++) {
        for (let asiento = 0; asiento < asientosPorFila; asiento++) {
            // Usar geometría más simple para mejor rendimiento
            const geometry = new THREE.BoxGeometry(asientoSize, asientoSize * 0.8, asientoSize * 0.6);
            const material = new THREE.MeshLambertMaterial({ color: COLORS.disponible });
            const asientoMesh = new THREE.Mesh(geometry, material);

            // Posición local del asiento
            const x = (asiento - asientosPorFila / 2 + 0.5) * (asientoSize + asientoGap);
            const y = fila * filaHeight + 1;
            const z = fila * filaDepth;

            // Rotar según la zona
            const pos = new THREE.Vector3(x, y, z);
            pos.applyAxisAngle(new THREE.Vector3(0, 1, 0), rotacion);

            asientoMesh.position.set(
                posicion.x + pos.x,
                posicion.y + pos.y,
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
 * Crear zona de asientos mixta (Grada Lateral con Tribuna integrada)
 * Las primeras 5 filas son Tribuna (premium), las últimas 10 son Grada Lateral
 */
function crearZonaAsientosMixta(posicion, filas, asientosPorFila, rotacion) {
    const asientoSize = 0.8;
    const asientoGap = 0.15;
    const filaHeight = 0.5;
    const filaDepth = 0.8;

    let asientoNumeroTribuna = 1;
    let asientoNumeroGrada = 1;

    for (let fila = 0; fila < filas; fila++) {
        // Las primeras 5 filas son TRIBUNA (zona premium)
        const esTribuna = fila < 5;
        
        for (let asiento = 0; asiento < asientosPorFila; asiento++) {
            // Usar geometría más simple para mejor rendimiento
            const geometry = new THREE.BoxGeometry(asientoSize, asientoSize * 0.8, asientoSize * 0.6);
            const material = new THREE.MeshLambertMaterial({ color: COLORS.disponible });
            const asientoMesh = new THREE.Mesh(geometry, material);

            // Posición local del asiento
            const x = (asiento - asientosPorFila / 2 + 0.5) * (asientoSize + asientoGap);
            const y = fila * filaHeight + 1;
            const z = fila * filaDepth;

            // Rotar según la zona
            const pos = new THREE.Vector3(x, y, z);
            pos.applyAxisAngle(new THREE.Vector3(0, 1, 0), rotacion);

            asientoMesh.position.set(
                posicion.x + pos.x,
                posicion.y + pos.y,
                posicion.z + pos.z
            );
            asientoMesh.rotation.y = rotacion;

            // Metadata del asiento
            asientoMesh.userData = {
                zona: esTribuna ? 'Tribuna' : 'Grada Lateral',
                numero: esTribuna ? asientoNumeroTribuna : asientoNumeroGrada,
                disponible: true,
                precio: 0
            };

            asientos.push(asientoMesh);
            scene.add(asientoMesh);
            
            if (esTribuna) {
                asientoNumeroTribuna++;
            } else {
                asientoNumeroGrada++;
            }
        }
    }
}

/**
 * Cargar datos del estadio desde la API
 */
async function cargarDatosEstadio() {
    try {
        updateLoadingProgress('Cargando zonas...', 80);

        // Cargar zonas
        const zonasResponse = await fetch(`/api/eventos/${eventoId}/estadio/zonas`);
        if (!zonasResponse.ok) throw new Error('Error al cargar zonas');

        const zonasResult = await zonasResponse.json();
        zonasData = zonasResult.zonas || [];

        updateLoadingProgress('Cargando asientos ocupados...', 90);

        // Cargar asientos ocupados
        const ocupadosResponse = await fetch(`/api/eventos/${eventoId}/estadio/asientos-ocupados`);
        if (ocupadosResponse.ok) {
            asientosOcupados = await ocupadosResponse.json();
        }

        // Actualizar asientos
        actualizarAsientos();
        actualizarPanelInfo();

        updateLoadingProgress('¡Listo!', 100);

        // Ocultar loading
        setTimeout(() => {
            const loading = document.getElementById('loading');
            loading.style.opacity = '0';
            setTimeout(() => loading.style.display = 'none', 300);
        }, 500);

        console.log('✅ Datos cargados correctamente');

    } catch (error) {
        console.error('❌ Error al cargar datos:', error);
        mostrarErrorCarga(error.message);
    }
}

/**
 * Mostrar error de carga
 */
function mostrarErrorCarga(mensaje) {
    const loading = document.getElementById('loading');
    loading.style.background = 'rgba(26, 35, 126, 0.98)';
    loading.innerHTML = `
        <div style="color: white; text-align: center; padding: 40px; max-width: 600px;">
            <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #FFC107; margin-bottom: 20px;"></i>
            <h3>⚠️ Error al cargar el estadio 3D</h3>
            <p style="margin: 15px 0; font-size: 1.1rem;">${mensaje}</p>
            <button onclick="location.reload()" class="btn btn-primary mt-3">
                <i class="fas fa-sync-alt me-2"></i>Reintentar
            </button>
            <br><br>
            <a href="/eventos/${eventoId}/comprar" style="color: #FFC107;">
                <i class="fas fa-arrow-left me-2"></i>Volver a la vista 2D
            </a>
        </div>
    `;
}

/**
 * Actualizar estado de asientos
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
 */
function onMouseMove(event) {
    const rect = renderer.domElement.getBoundingClientRect();
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(asientos);

    // Resetear colores
    asientos.forEach(asiento => {
        if (asiento !== asientoSeleccionado) {
            asiento.material.color.setHex(
                asiento.userData.disponible ? COLORS.disponible : COLORS.ocupado
            );
        }
    });

    const tooltip = document.getElementById('tooltip');

    if (intersects.length > 0) {
        const asiento = intersects[0].object;

        if (asiento.userData.disponible && asiento !== asientoSeleccionado) {
            asiento.material.color.setHex(COLORS.hover);
        }

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
 * Manejar clicks
 */
function onClick(event) {
    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(asientos);

    if (intersects.length > 0) {
        const asiento = intersects[0].object;

        if (asiento.userData.disponible) {
            // Deseleccionar anterior
            if (asientoSeleccionado) {
                asientoSeleccionado.material.color.setHex(COLORS.disponible);
            }

            // Seleccionar nuevo
            asientoSeleccionado = asiento;
            asiento.material.color.setHex(COLORS.seleccionado);

            mostrarInfoAsiento(asiento);
        }
    }
}

/**
 * Mostrar información del asiento
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
        window.location.href = `/eventos/${eventoId}/comprar?zona=${encodeURIComponent(zona)}&asiento=${numero}`;
    }
}

/**
 * Resetear vista
 */
function resetearVista() {
    camera.position.set(0, 100, 150);
    camera.lookAt(0, 0, 0);
}

/**
 * Redimensionar ventana
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
    renderer.render(scene, camera);
}

// Exponer funciones globales
window.initEstadio3D = init;
window.confirmarCompra = confirmarCompra;
window.resetearVista = resetearVista;