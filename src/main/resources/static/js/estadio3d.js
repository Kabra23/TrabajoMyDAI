// Variables globales
let scene, camera, renderer, controls;
let raycaster, mouse;
let asientos = [];
let asientoSeleccionado = null;
let asientosOcupados = {};
let zonasData = [];
let eventoId;
let hoveredAsiento = null;
let isMouseMoving = false;

// Constantes de rendimiento mejoradas
const MOUSE_THROTTLE_MS = 100; // Aumentado para mejor rendimiento
const MAX_RAYCAST_DISTANCE = 150; // Limitar distancia de raycasting

// Colores optimizados
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

    // Crear renderer (optimizado para rendimiento mejorado)
    renderer = new THREE.WebGLRenderer({ 
        antialias: false, 
        powerPreference: "high-performance",
        precision: "lowp", // Precisión baja para mejor rendimiento
        alpha: false // No necesitamos transparencia en el fondo
    });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2)); // Limitar pixel ratio
    renderer.shadowMap.enabled = false; // Deshabilitado para mejor rendimiento
    container.appendChild(renderer.domElement);

    // Controles de cámara optimizados
    controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.08; // Más fluido
    controls.minDistance = 30;
    controls.maxDistance = 200;
    controls.maxPolarAngle = Math.PI / 2.1;
    controls.enablePan = true; // Permitir paneo
    controls.panSpeed = 0.5; // Velocidad de paneo reducida

    // Raycaster para detección de clicks (optimizado)
    raycaster = new THREE.Raycaster();
    raycaster.params.Points.threshold = 0.5; // Reducir área de detección
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

    // Eventos con throttling mejorado
    window.addEventListener('resize', onWindowResize, false);
    
    // Throttle para mousemove para mejor rendimiento
    let mouseMoveTimeout;
    renderer.domElement.addEventListener('mousemove', (event) => {
        if (!mouseMoveTimeout) {
            mouseMoveTimeout = setTimeout(() => {
                onMouseMove(event);
                mouseMoveTimeout = null;
            }, MOUSE_THROTTLE_MS);
        }
    }, false);
    
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
    const campoMaterial = new THREE.MeshLambertMaterial({
        color: COLORS.campo,
        side: THREE.DoubleSide
    });
    const campo = new THREE.Mesh(campoGeometry, campoMaterial);
    campo.rotation.x = -Math.PI / 2;
    campo.receiveShadow = true;
    scene.add(campo);

    const pistaGeometry = new THREE.PlaneGeometry(85, 115);
    const pistaMaterial = new THREE.MeshLambertMaterial({
        color: 0x8B6914,
        side: THREE.DoubleSide
    });
    const pista = new THREE.Mesh(pistaGeometry, pistaMaterial);
    pista.rotation.x = -Math.PI / 2;
    pista.position.y = -0.1;
    scene.add(pista);


    // Líneas del campo
    crearLineasCampo();

    // Crear zonas de asientos (reducido para mejor rendimiento)
    crearPorteria({ x: 0, y: 0, z: -50 });
    crearPorteria({ x: 0, y: 0, z: 50 });

    // Crear estructura del estadio (muros exteriores)
    crearEstructuraEstadio();

    // Crear zonas de asientos con nombres reales
    // Crear zonas de asientos (optimizado para mejor rendimiento)
    crearZonaAsientos('Tribuna', { x: 0, y: 0, z: -60 }, 8, 12, 0);
    crearZonaAsientos('Grada Lateral', { x: -45, y: 0, z: 0 }, 10, 8, Math.PI / 2);
    crearZonaAsientos('Grada Lateral', { x: 45, y: 0, z: 0 }, 10, 8, -Math.PI / 2);
    crearZonaAsientos('Gol Nord', { x: 0, y: 0, z: 60 }, 6, 12, Math.PI);
    crearZonaAsientos('Gol Sud', { x: 0, y: 0, z: -60 }, 6, 12, 0);}

/**
 * Crear líneas del campo de fútbol
 */
function crearLineasCampo() {
    const lineaMaterial = new THREE.LineBasicMaterial({ color: 0xffffff, linewidth: 2 });
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
    const circleGeometry = new THREE.CircleGeometry(9.15, 32);
    const circleEdges = new THREE.EdgesGeometry(circleGeometry);
    const circleLine = new THREE.LineSegments(circleEdges, lineaMaterial);
    circleLine.rotation.x = -Math.PI / 2;
    circleLine.position.y = 0.1;
    scene.add(circleLine);
    crearAreaPenalti(50);
    crearAreaPenalti(-50);

}
function crearAreaPenalti(zPosition) {
    const lineaMaterial = new THREE.LineBasicMaterial({ color: 0xffffff, linewidth: 2 });

    // Área grande
    const areaGrande = [
        new THREE.Vector3(-20, 0.1, zPosition),
        new THREE.Vector3(-20, 0.1, zPosition > 0 ? zPosition - 16.5 : zPosition + 16.5),
        new THREE.Vector3(20, 0.1, zPosition > 0 ? zPosition - 16.5 : zPosition + 16.5),
        new THREE.Vector3(20, 0.1, zPosition)
    ];
    const areaGrandeGeometry = new THREE.BufferGeometry().setFromPoints(areaGrande);
    const areaGrandeLine = new THREE.Line(areaGrandeGeometry, lineaMaterial);
    scene.add(areaGrandeLine);

    // Área pequeña
    const areaPequena = [
        new THREE.Vector3(-9, 0.1, zPosition),
        new THREE.Vector3(-9, 0.1, zPosition > 0 ? zPosition - 5.5 : zPosition + 5.5),
        new THREE.Vector3(9, 0.1, zPosition > 0 ? zPosition - 5.5 : zPosition + 5.5),
        new THREE.Vector3(9, 0.1, zPosition)
    ];
    const areaPequenaGeometry = new THREE.BufferGeometry().setFromPoints(areaPequena);
    const areaPequenaLine = new THREE.Line(areaPequenaGeometry, lineaMaterial);
    scene.add(areaPequenaLine);
}

/**
 * Crear portería
 */
function crearPorteria(posicion) {
    const materialPorteria = new THREE.MeshLambertMaterial({ color: 0xeeeeee });
    const posteRadius = 0.12;
    const posteHeight = 2.44;
    const ancho = 7.32;

    // Poste izquierdo
    const posteIzq = new THREE.Mesh(
        new THREE.CylinderGeometry(posteRadius, posteRadius, posteHeight, 8),
        materialPorteria
    );
    posteIzq.position.set(posicion.x - ancho/2, posteHeight/2, posicion.z);
    scene.add(posteIzq);

    // Poste derecho
    const posteDer = new THREE.Mesh(
        new THREE.CylinderGeometry(posteRadius, posteRadius, posteHeight, 8),
        materialPorteria
    );
    posteDer.position.set(posicion.x + ancho/2, posteHeight/2, posicion.z);
    scene.add(posteDer);

    // Travesaño
    const travesano = new THREE.Mesh(
        new THREE.CylinderGeometry(posteRadius, posteRadius, ancho, 8),
        materialPorteria
    );
    travesano.rotation.z = Math.PI / 2;
    travesano.position.set(posicion.x, posteHeight, posicion.z);
    scene.add(travesano);

    // Red (simplificada)
    const redGeometry = new THREE.PlaneGeometry(ancho, posteHeight);
    const redMaterial = new THREE.MeshBasicMaterial({
        color: 0xffffff,
        transparent: true,
        opacity: 0.3,
        side: THREE.DoubleSide,
        wireframe: true
    });
    const red = new THREE.Mesh(redGeometry, redMaterial);
    red.position.set(posicion.x, posteHeight/2, posicion.z);
    scene.add(red);
}

/**
 * Crear estructura exterior del estadio
 */
function crearEstructuraEstadio() {
    const wallMaterial = new THREE.MeshLambertMaterial({
        color: 0x555555,
        side: THREE.DoubleSide
    });

    // Muro trasero tribuna
    const muroTribuna = new THREE.Mesh(
        new THREE.BoxGeometry(80, 15, 2),
        wallMaterial
    );
    muroTribuna.position.set(0, 7.5, -72);
    scene.add(muroTribuna);

    // Muro gol nord
    const muroGolNord = new THREE.Mesh(
        new THREE.BoxGeometry(80, 12, 2),
        wallMaterial
    );
    muroGolNord.position.set(0, 6, 72);
    scene.add(muroGolNord);

    // Muros laterales
    const muroLateralIzq = new THREE.Mesh(
        new THREE.BoxGeometry(2, 12, 144),
        wallMaterial
    );
    muroLateralIzq.position.set(-57, 6, 0);
    scene.add(muroLateralIzq);

    const muroLateralDer = new THREE.Mesh(
        new THREE.BoxGeometry(2, 12, 144),
        wallMaterial
    );
    muroLateralDer.position.set(57, 6, 0);
    scene.add(muroLateralDer);
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
            const material = new THREE.MeshLambertMaterial({ color: COLORS.disponible });
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
        updateLoadingProgress('Cargando información de zonas...', 85);
        // Crear promesa con timeout mejorado
        const timeoutPromise = new Promise((_, reject) =>
            setTimeout(() => reject(new Error('Tiempo de espera agotado. El servidor no responde.')), 10000)
        );

        // Cargar zonas con timeout
        const zonasResponse = await Promise.race([
            fetch(`/api/eventos/${eventoId}/estadio/zonas`),
            timeoutPromise
        ]);

        if (!zonasResponse.ok) {
            throw new Error(`Error HTTP ${zonasResponse.status}: No se pudieron cargar las zonas`);
        }

        const zonasResult = await zonasResponse.json();
        zonasData = zonasResult.zonas || [];

        updateLoadingProgress('Cargando asientos ocupados...', 90);

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
        updateLoadingProgress('Finalizando...', 95);

        // Actualizar precios y disponibilidad
        actualizarAsientos();

        // Ocultar loading
        updateLoadingProgress('¡Listo!', 100);

        // Ocultar loading con animación
        setTimeout(() => {
            document.getElementById('loading').style.opacity = '0';
            setTimeout(() => {
                document.getElementById('loading').style.display = 'none';
            }, 300);
        }, 200);
        // Actualizar panel de información
        actualizarPanelInfo();

    } catch (error) {
        console.error('Error al cargar datos del estadio:', error);
        const loadingDiv = document.getElementById('loading');
        loadingDiv.style.background = 'rgba(26, 35, 126, 0.98)';
        loadingDiv.innerHTML =
            '<div style="color: white; text-align: center; padding: 40px; max-width: 600px;">' +
            '<i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #FFC107; margin-bottom: 20px;"></i>' +
            '<h3 style="margin-bottom: 20px;">⚠️ Error al cargar el estadio 3D</h3>' +
            '<p style="margin: 15px 0; font-size: 1.1rem; line-height: 1.5; color: #fff;">' + error.message + '</p>' +
            '<p style="margin: 15px 0; color: #ccc; font-size: 0.95rem;">Posibles causas:</p>' +
            '<ul style="text-align: left; color: #ccc; max-width: 400px; margin: 15px auto; font-size: 0.9rem;">' +
            '<li>El servidor está temporalmente no disponible</li>' +
            '<li>Problemas de conexión a Internet</li>' +
            '<li>El evento no tiene zonas configuradas</li>' +
            '</ul>' +
            '<div style="margin-top: 30px; display: flex; gap: 15px; justify-content: center; flex-wrap: wrap;">' +
            '<button onclick="location.reload()" style="padding: 12px 30px; cursor: pointer; background: #2196F3; color: white; border: none; border-radius: 25px; font-size: 16px; font-weight: 600; transition: all 0.3s;">' +
            '<i class="fas fa-sync-alt me-2"></i>Reintentar' +
            '</button>' +
            '<a href="/eventos/' + eventoId + '/comprar" style="padding: 12px 30px; background: #FFC107; color: #1a237e; text-decoration: none; border-radius: 25px; font-size: 16px; font-weight: 600; display: inline-block;">' +
            '<i class="fas fa-arrow-left me-2"></i>Vista 2D' +
            '</a>' +
            '</div>' +
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
 * Manejar movimiento del mouse (optimizado)
 */
function onMouseMove(event) {
    const rect = renderer.domElement.getBoundingClientRect();
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

    raycaster.setFromCamera(mouse, camera);
    raycaster.far = MAX_RAYCAST_DISTANCE; // Limitar distancia de raycasting
    const intersects = raycaster.intersectObjects(asientos);

    // Tooltip
    const tooltip = document.getElementById('tooltip');

    if (intersects.length > 0) {
        const asiento = intersects[0].object;
        
        // Solo actualizar si cambió el asiento hover
        if (hoveredAsiento !== asiento) {
            // Resetear asiento anterior
            if (hoveredAsiento && hoveredAsiento !== asientoSeleccionado) {
                hoveredAsiento.material.color.setHex(
                    hoveredAsiento.userData.disponible ? COLORS.disponible : COLORS.ocupado
                );
            }
            
            hoveredAsiento = asiento;
            
            if (asiento.userData.disponible && asiento !== asientoSeleccionado) {
                asiento.material.color.setHex(COLORS.hover);
            }
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
        // Resetear hover cuando no hay intersección
        if (hoveredAsiento && hoveredAsiento !== asientoSeleccionado) {
            hoveredAsiento.material.color.setHex(
                hoveredAsiento.userData.disponible ? COLORS.disponible : COLORS.ocupado
            );
            hoveredAsiento = null;
        }
        
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