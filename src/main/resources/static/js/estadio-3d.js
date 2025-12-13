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
    tribuna: 0x8B4513
};

/**
 * Inicializar la escena 3D
 */
function init(idEvento) {
    eventoId = idEvento;
    
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

    // Crear renderer
    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
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

    // Luces
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
    scene.add(ambientLight);

    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
    directionalLight.position.set(50, 100, 50);
    directionalLight.castShadow = true;
    directionalLight.shadow.mapSize.width = 2048;
    directionalLight.shadow.mapSize.height = 2048;
    scene.add(directionalLight);

    // Crear el estadio
    crearEstadio();

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
 * Crear el modelo del estadio
 */
function crearEstadio() {
    // Campo de fútbol
    const campoGeometry = new THREE.PlaneGeometry(70, 100);
    const campoMaterial = new THREE.MeshLambertMaterial({ color: COLORS.campo });
    const campo = new THREE.Mesh(campoGeometry, campoMaterial);
    campo.rotation.x = -Math.PI / 2;
    campo.receiveShadow = true;
    scene.add(campo);

    // Líneas del campo
    crearLineasCampo();

    // Crear zonas de asientos
    crearZonaAsientos('Tribuna', { x: 0, y: 0, z: -60 }, 25, 40, 0);
    crearZonaAsientos('Grada Lateral', { x: -45, y: 0, z: 0 }, 30, 25, Math.PI / 2);
    crearZonaAsientos('Grada Lateral', { x: 45, y: 0, z: 0 }, 30, 25, -Math.PI / 2);
    crearZonaAsientos('Gol Nord', { x: 0, y: 0, z: 60 }, 20, 40, Math.PI);
    crearZonaAsientos('Gol Sud', { x: 0, y: 0, z: -60 }, 20, 40, 0);
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
 */
function crearZonaAsientos(nombreZona, posicion, filas, asientosPorFila, rotacion) {
    const asientoSize = 0.5;
    const asientoGap = 0.1;
    const filaHeight = 0.3;

    let asientoNumero = 1;
    
    for (let fila = 0; fila < filas; fila++) {
        for (let asiento = 0; asiento < asientosPorFila; asiento++) {
            const geometry = new THREE.BoxGeometry(asientoSize, asientoSize, asientoSize);
            const material = new THREE.MeshPhongMaterial({ color: COLORS.disponible });
            const asientoMesh = new THREE.Mesh(geometry, material);

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

            asientoMesh.castShadow = true;
            asientoMesh.receiveShadow = true;

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

        // Cargar zonas
        const zonasResponse = await fetch(`/api/eventos/${eventoId}/estadio/zonas`);
        const zonasResult = await zonasResponse.json();
        zonasData = zonasResult.zonas;

        // Cargar asientos ocupados
        const ocupadosResponse = await fetch(`/api/eventos/${eventoId}/estadio/asientos-ocupados`);
        asientosOcupados = await ocupadosResponse.json();

        // Actualizar precios y disponibilidad
        actualizarAsientos();

        // Ocultar loading
        document.getElementById('loading').style.display = 'none';

        // Actualizar panel de información
        actualizarPanelInfo();

    } catch (error) {
        console.error('Error al cargar datos del estadio:', error);
        document.getElementById('loading').innerHTML = 
            '<div style="color: white; text-align: center;">' +
            '<h3>Error al cargar el estadio</h3>' +
            '<p>' + error.message + '</p>' +
            '<button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; cursor: pointer;">Reintentar</button>' +
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
 */
function onMouseMove(event) {
    const rect = renderer.domElement.getBoundingClientRect();
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(asientos);

    // Resetear colores de hover
    asientos.forEach(asiento => {
        if (asiento !== asientoSeleccionado) {
            asiento.material.color.setHex(
                asiento.userData.disponible ? COLORS.disponible : COLORS.ocupado
            );
        }
    });

    // Tooltip
    const tooltip = document.getElementById('tooltip');
    
    if (intersects.length > 0) {
        const asiento = intersects[0].object;
        
        if (asiento.userData.disponible && asiento !== asientoSeleccionado) {
            asiento.material.color.setHex(COLORS.hover);
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
