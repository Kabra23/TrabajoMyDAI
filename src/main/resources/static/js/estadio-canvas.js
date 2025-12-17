/**
 * Sistema de visualización de estadio usando Canvas 2D - VERSIÓN OPTIMIZADA
 * Mejoras: rendimiento, visualización, interacción y características
 */

class EstadioCanvas {
    constructor(canvasId, eventoId) {
        this.canvas = document.getElementById(canvasId);

        if (!this.canvas) {
            console.error('❌ ERROR: No se encontró el elemento canvas con ID:', canvasId);
            throw new Error(`No se encontró el canvas con ID: ${canvasId}`);
        }

        this.ctx = this.canvas.getContext('2d', {
            alpha: false, // Mejora rendimiento
            desynchronized: true // Reduce latencia
        });

        if (!this.ctx) {
            console.error('❌ ERROR: No se pudo obtener el contexto 2D del canvas');
            throw new Error('No se pudo obtener el contexto 2D');
        }

        console.log('✅ Canvas inicializado correctamente');
        this.eventoId = eventoId;

        // Configuración de canvas
        this.setupCanvas();

        // Vista y zoom
        this.offsetX = this.width / 2;
        this.offsetY = this.height / 2;
        this.zoom = 1;
        this.minZoom = 0.3;
        this.maxZoom = 3;
        this.isDragging = false;
        this.lastMouseX = 0;
        this.lastMouseY = 0;

        console.log(`📍 Vista inicial - offsetX: ${this.offsetX}, offsetY: ${this.offsetY}, zoom: ${this.zoom}`);

        // Datos
        this.asientos = [];
        this.asientosFiltrados = []; // Cache de asientos válidos
        this.zonasData = [];
        this.asientosOcupados = {};
        this.asientosSeleccionados = [];
        this.asientoHover = null;

        // Optimización de renderizado
        this.needsRender = true;
        this.animationFrameId = null;
        this.lastFrameTime = 0;
        this.fps = 60;
        this.frameInterval = 1000 / this.fps;

        // Cache de dibujo
        this.campoCache = null;
        this.asientosCache = new Map();

        // Colores mejorados con degradados
        this.colors = {
            campo: '#2E7D32',
            campoClaro: '#388E3C',
            lineas: '#FFFFFF',
            disponible: '#4CAF50',
            ocupado: '#f44336',
            seleccionado: '#2196F3',
            hover: '#FFC107',
            tribuna: '#FF8C00',
            tribunaClaro: '#FFB300',
            grada: '#283593',
            gradaClaro: '#3949AB',
            sombra: 'rgba(0, 0, 0, 0.3)'
        };

        // Estado de interacción
        this.touchStartDistance = 0;
        this.isPinching = false;
        this.lastTouchX = 0;
        this.lastTouchY = 0;

        this.inicializar();
    }

    setupCanvas() {
        const dpr = window.devicePixelRatio || 1;

        // Usar dimensiones de la ventana
        this.width = window.innerWidth;
        this.height = window.innerHeight;

        this.canvas.width = this.width * dpr;
        this.canvas.height = this.height * dpr;

        this.ctx.scale(dpr, dpr);

        this.canvas.style.width = this.width + 'px';
        this.canvas.style.height = this.height + 'px';

        console.log(`📐 Canvas configurado: ${this.width}x${this.height} (DPR: ${dpr})`);
    }

    async inicializar() {
        try {
            console.log('🎮 Iniciando estadio Canvas 2D optimizado');
            this.actualizarLoading('Inicializando vista...', 10);

            console.log('🎧 Configurando event listeners...');
            this.setupEventListeners();
            console.log('✅ Event listeners configurados');

            console.log('🏟️ Creando estructura del estadio...');
            this.actualizarLoading('Creando estadio...', 30);
            this.crearEstructuraEstadio();
            console.log(`✅ Creados ${this.asientos.length} asientos`);

            console.log('🔍 Ajustando vista inicial...');
            this.ajustarZoomInicial();
            console.log(`✅ Zoom inicial configurado: ${this.zoom.toFixed(2)}x`);

            console.log('🎨 Creando cache del campo...');
            this.actualizarLoading('Optimizando gráficos...', 40);
            this.crearCacheCampo();

            console.log('📡 Cargando datos de la API...');
            this.actualizarLoading('Cargando información...', 50);
            await this.cargarDatos();
            console.log('✅ Datos cargados correctamente');

            console.log('🎨 Iniciando render loop optimizado...');
            this.actualizarLoading('Finalizando...', 90);
            this.render();

            console.log('🎉 Ocultando pantalla de carga...');
            setTimeout(() => {
                const loading = document.getElementById('loading');
                if (loading) {
                    loading.style.opacity = '0';
                    setTimeout(() => loading.style.display = 'none', 300);
                }
            }, 500);

            console.log('✅ Estadio Canvas listo');
        } catch (error) {
            console.error('❌ ERROR CRÍTICO en inicializar:', error);
            this.mostrarError('Error al inicializar el estadio: ' + error.message);
        }
    }

    crearEstructuraEstadio() {
        const asientoSize = 14;
        const gap = 3;
        let asientoIdGlobal = 1;

        // Configuration constants for stadium structure
        const GOL_NORD_EXTERIOR_FILAS = 6;
        const GOL_NORD_INTERIOR_FILAS = 6;
        const GOL_NORD_ASIENTOS_POR_FILA = 40;
        
        const GOL_SUD_INTERIOR_FILAS = 6;
        const GOL_SUD_EXTERIOR_FILAS = 6;
        const GOL_SUD_ASIENTOS_POR_FILA = 40;
        
        const LATERAL_EXTERIOR_FILAS = 8;
        const LATERAL_ASIENTOS_POR_FILA = 35;
        const TRIBUNA_INTERIOR_FILAS = 5;

        // GOL NORD (Arriba) - Exterior (6 filas más alejadas)
        let golNordNumero = 1;

        for (let fila = 0; fila < GOL_NORD_EXTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < GOL_NORD_ASIENTOS_POR_FILA; asiento++) {
                const x = (asiento - GOL_NORD_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);
                const y = -420 - (fila * (asientoSize + gap));

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Gol Nord',
                    numero: golNordNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.grada
                });
                asientoIdGlobal++;
                golNordNumero++;
            }
        }

        // GOL NORD (Arriba) - Interior (6 filas más cercanas)
        for (let fila = 0; fila < GOL_NORD_INTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < GOL_NORD_ASIENTOS_POR_FILA; asiento++) {
                const x = (asiento - GOL_NORD_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);
                const y = -320 - (fila * (asientoSize + gap));

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Gol Nord',
                    numero: golNordNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.grada
                });
                asientoIdGlobal++;
                golNordNumero++;
            }
        }

        // GOL SUD (Abajo) - Interior (6 filas más cercanas)
        let golSudNumero = 1;

        for (let fila = 0; fila < GOL_SUD_INTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < GOL_SUD_ASIENTOS_POR_FILA; asiento++) {
                const x = (asiento - GOL_SUD_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);
                const y = 320 + (fila * (asientoSize + gap));

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Gol Sud',
                    numero: golSudNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.grada
                });
                asientoIdGlobal++;
                golSudNumero++;
            }
        }

        // GOL SUD (Abajo) - Exterior (6 filas más alejadas)
        for (let fila = 0; fila < GOL_SUD_EXTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < GOL_SUD_ASIENTOS_POR_FILA; asiento++) {
                const x = (asiento - GOL_SUD_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);
                const y = 420 + (fila * (asientoSize + gap));

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Gol Sud',
                    numero: golSudNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.grada
                });
                asientoIdGlobal++;
                golSudNumero++;
            }
        }

        // GRADA LATERAL - Contadores separados para izquierda y derecha
        let gradaLateralNumero = 1;

        // GRADA LATERAL IZQUIERDA - Exterior (8 filas más alejadas)
        for (let fila = 0; fila < LATERAL_EXTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < LATERAL_ASIENTOS_POR_FILA; asiento++) {
                const x = -380 - (fila * (asientoSize + gap));
                const y = (asiento - LATERAL_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Grada lateral',
                    numero: gradaLateralNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.grada
                });
                asientoIdGlobal++;
                gradaLateralNumero++;
            }
        }

        // GRADA LATERAL DERECHA - Exterior (8 filas más alejadas)
        // Continuar la numeración desde donde terminó la izquierda
        for (let fila = 0; fila < LATERAL_EXTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < LATERAL_ASIENTOS_POR_FILA; asiento++) {
                const x = 380 + (fila * (asientoSize + gap));
                const y = (asiento - LATERAL_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Grada lateral',
                    numero: gradaLateralNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.grada
                });
                asientoIdGlobal++;
                gradaLateralNumero++;
            }
        }

        // TRIBUNA - Contadores para secciones izquierda y derecha
        let tribunaNumero = 1;

        // TRIBUNA IZQUIERDA - Interior (5 filas más cercanas)
        for (let fila = 0; fila < TRIBUNA_INTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < LATERAL_ASIENTOS_POR_FILA; asiento++) {
                const x = -280 - (fila * (asientoSize + gap));
                const y = (asiento - LATERAL_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'tribuna',
                    numero: tribunaNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.tribuna
                });
                asientoIdGlobal++;
                tribunaNumero++;
            }
        }

        // TRIBUNA DERECHA - Interior (5 filas más cercanas)
        // Continuar la numeración desde donde terminó la izquierda
        for (let fila = 0; fila < TRIBUNA_INTERIOR_FILAS; fila++) {
            for (let asiento = 0; asiento < LATERAL_ASIENTOS_POR_FILA; asiento++) {
                const x = 280 + (fila * (asientoSize + gap));
                const y = (asiento - LATERAL_ASIENTOS_POR_FILA / 2) * (asientoSize + gap);

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'tribuna',
                    numero: tribunaNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.tribuna
                });
                asientoIdGlobal++;
                tribunaNumero++;
            }
        }

        const totalGolNord = golNordNumero - 1;
        const totalGolSud = golSudNumero - 1;
        const totalGradaLateral = gradaLateralNumero - 1;
        const totalTribuna = tribunaNumero - 1;

        console.log(`✅ Estadio creado: ${this.asientos.length} asientos`);
        console.log(`   - Gol Nord (exterior + interior): ${totalGolNord} asientos`);
        console.log(`   - Gol Sud (interior + exterior): ${totalGolSud} asientos`);
        console.log(`   - Grada lateral (izq + der): ${totalGradaLateral} asientos`);
        console.log(`   - Tribuna (izq + der interior): ${totalTribuna} asientos`);
    }

    ajustarZoomInicial() {
        // Calcular dimensiones del estadio
        let minX = Infinity, maxX = -Infinity;
        let minY = Infinity, maxY = -Infinity;

        this.asientos.forEach(asiento => {
            minX = Math.min(minX, asiento.x - asiento.size);
            maxX = Math.max(maxX, asiento.x + asiento.size);
            minY = Math.min(minY, asiento.y - asiento.size);
            maxY = Math.max(maxY, asiento.y + asiento.size);
        });

        const estadioWidth = maxX - minX;
        const estadioHeight = maxY - minY;

        // Agregar margen del 10%
        const marginFactor = 1.1;
        const estadioWidthMargin = estadioWidth * marginFactor;
        const estadioHeightMargin = estadioHeight * marginFactor;

        // Calcular zoom para que quepa todo
        const zoomX = this.width / estadioWidthMargin;
        const zoomY = this.height / estadioHeightMargin;

        // Usar el zoom más pequeño para que todo quepa
        this.zoom = Math.min(zoomX, zoomY);
        this.zoom = Math.max(this.minZoom, Math.min(this.zoom, this.maxZoom));

        // Centrar el estadio
        this.offsetX = this.width / 2;
        this.offsetY = this.height / 2;

        console.log(`📊 Dimensiones estadio: ${estadioWidth.toFixed(0)}x${estadioHeight.toFixed(0)}`);
        console.log(`📏 Canvas: ${this.width}x${this.height}`);
        console.log(`🔍 Zoom calculado: ${this.zoom.toFixed(2)}x`);

        this.needsRender = true;
    }

    crearCacheCampo() {
        // Crear un canvas off-screen para el campo
        const cacheCanvas = document.createElement('canvas');
        cacheCanvas.width = 500;
        cacheCanvas.height = 650;
        const cacheCtx = cacheCanvas.getContext('2d');

        const campoX = -200;
        const campoY = -280;
        const campoAncho = 400;
        const campoAlto = 560;

        // Transformar al centro del canvas cache
        cacheCtx.translate(250, 325);

        // Fondo del campo con degradado
        const gradient = cacheCtx.createLinearGradient(campoX, campoY, campoX, campoY + campoAlto);
        gradient.addColorStop(0, this.colors.campo);
        gradient.addColorStop(0.5, this.colors.campoClaro);
        gradient.addColorStop(1, this.colors.campo);
        cacheCtx.fillStyle = gradient;
        cacheCtx.fillRect(campoX, campoY, campoAncho, campoAlto);

        // Franjas de césped
        cacheCtx.fillStyle = 'rgba(46, 125, 50, 0.3)';
        for (let i = 0; i < 14; i++) {
            if (i % 2 === 0) {
                cacheCtx.fillRect(campoX, campoY + (i * 40), campoAncho, 40);
            }
        }

        // Líneas del campo
        cacheCtx.strokeStyle = this.colors.lineas;
        cacheCtx.lineWidth = 3;
        cacheCtx.lineCap = 'round';
        cacheCtx.shadowColor = 'rgba(0, 0, 0, 0.3)';
        cacheCtx.shadowBlur = 3;

        // Perímetro
        cacheCtx.strokeRect(campoX + 5, campoY + 5, campoAncho - 10, campoAlto - 10);

        // Línea central
        cacheCtx.beginPath();
        cacheCtx.moveTo(campoX + 5, 0);
        cacheCtx.lineTo(campoX + campoAncho - 5, 0);
        cacheCtx.stroke();

        // Círculo central
        cacheCtx.beginPath();
        cacheCtx.arc(0, 0, 45, 0, Math.PI * 2);
        cacheCtx.stroke();

        // Punto central
        cacheCtx.beginPath();
        cacheCtx.arc(0, 0, 3, 0, Math.PI * 2);
        cacheCtx.fillStyle = this.colors.lineas;
        cacheCtx.fill();

        // Áreas y porterías
        this.dibujarAreaCache(cacheCtx, -275);
        this.dibujarAreaCache(cacheCtx, 275);

        // Esquinas
        this.dibujarEsquinasCache(cacheCtx);

        cacheCtx.shadowColor = 'transparent';

        this.campoCache = cacheCanvas;
        console.log('✅ Cache del campo creado');
    }

    dibujarAreaCache(ctx, y) {
        const esArriba = y < 0;

        ctx.strokeStyle = this.colors.lineas;
        ctx.lineWidth = 3;

        // Área grande
        ctx.strokeRect(-110, esArriba ? y : y - 90, 220, 90);

        // Área pequeña
        ctx.strokeRect(-50, esArriba ? y : y - 35, 100, 35);

        // Punto de penalti
        const penaltiY = esArriba ? y + 60 : y - 60;
        ctx.beginPath();
        ctx.arc(0, penaltiY, 3, 0, Math.PI * 2);
        ctx.fillStyle = this.colors.lineas;
        ctx.fill();

        // Arco del área
        ctx.beginPath();
        if (esArriba) {
            ctx.arc(0, penaltiY, 50, 0.3, Math.PI - 0.3);
        } else {
            ctx.arc(0, penaltiY, 50, -Math.PI + 0.3, -0.3);
        }
        ctx.stroke();

        // Portería
        const porteriaY = esArriba ? y - 2 : y + 2;
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(-40, porteriaY - 4, 8, 8);
        ctx.fillRect(32, porteriaY - 4, 8, 8);
        ctx.fillRect(-40, porteriaY - 4, 80, 6);

        // Red
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
        ctx.lineWidth = 1;
        for (let i = -35; i <= 35; i += 10) {
            ctx.beginPath();
            ctx.moveTo(i, porteriaY);
            ctx.lineTo(i, porteriaY + (esArriba ? -20 : 20));
            ctx.stroke();
        }
        for (let j = 0; j <= 20; j += 5) {
            ctx.beginPath();
            ctx.moveTo(-35, porteriaY + (esArriba ? -j : j));
            ctx.lineTo(35, porteriaY + (esArriba ? -j : j));
            ctx.stroke();
        }
    }

    dibujarEsquinasCache(ctx) {
        ctx.strokeStyle = this.colors.lineas;
        ctx.lineWidth = 3;

        const esquinas = [
            {x: -195, y: -275, inicio: 0, fin: Math.PI / 2},
            {x: 195, y: -275, inicio: Math.PI / 2, fin: Math.PI},
            {x: 195, y: 275, inicio: Math.PI, fin: Math.PI * 1.5},
            {x: -195, y: 275, inicio: Math.PI * 1.5, fin: Math.PI * 2}
        ];

        esquinas.forEach(esquina => {
            ctx.beginPath();
            ctx.arc(esquina.x, esquina.y, 8, esquina.inicio, esquina.fin);
            ctx.stroke();
        });
    }

    async cargarDatos() {
        try {
            const zonasResponse = await fetch(`/api/eventos/${this.eventoId}/estadio/zonas`);
            if (!zonasResponse.ok) throw new Error('Error al cargar zonas');

            const zonasResult = await zonasResponse.json();
            this.zonasData = zonasResult.zonas || [];

            this.actualizarLoading('Cargando asientos ocupados...', 70);

            const ocupadosResponse = await fetch(`/api/eventos/${this.eventoId}/estadio/asientos-ocupados`);
            if (ocupadosResponse.ok) {
                this.asientosOcupados = await ocupadosResponse.json();
            }

            this.actualizarAsientos();
            this.actualizarPanelInfo();

            console.log('✅ Datos cargados correctamente');
        } catch (error) {
            console.error('❌ Error al cargar datos:', error);
            this.mostrarError(error.message);
        }
    }

    actualizarAsientos() {
        const asientosFiltrados = [];

        this.asientos.forEach(asiento => {
            const zonaInfo = this.zonasData.find(z => z.nombre === asiento.zona);
            if (zonaInfo) {
                asiento.precio = zonaInfo.precio;

                if (asiento.numero <= zonaInfo.capacidadTotal) {
                    asientosFiltrados.push(asiento);
                }
            } else {
                asientosFiltrados.push(asiento);
            }

            if (asiento.numero <= (zonaInfo?.capacidadTotal || Infinity)) {
                const ocupados = this.asientosOcupados[asiento.zona] || [];
                asiento.disponible = !ocupados.includes(asiento.numero);
            }
        });

        this.asientosFiltrados = asientosFiltrados;
        console.log(`✅ Asientos validados: ${this.asientosFiltrados.length} asientos`);
    }

    setupEventListeners() {
        // Redimensionar
        let resizeTimeout;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(() => {
                this.setupCanvas();
                this.offsetX = this.width / 2;
                this.offsetY = this.height / 2;
                this.needsRender = true;
            }, 100);
        });

        // Mouse events
        this.canvas.addEventListener('mousedown', (e) => this.onMouseDown(e));
        this.canvas.addEventListener('mousemove', (e) => this.onMouseMove(e));
        this.canvas.addEventListener('mouseup', () => this.onMouseUp());
        this.canvas.addEventListener('mouseleave', () => this.onMouseUp());
        this.canvas.addEventListener('click', (e) => this.onClick(e));

        // Zoom con scroll (mejorado)
        this.canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const delta = e.deltaY > 0 ? 0.9 : 1.1;
            const newZoom = this.zoom * delta;

            if (newZoom >= this.minZoom && newZoom <= this.maxZoom) {
                // Zoom hacia el cursor
                const mouseX = e.clientX - this.canvas.offsetLeft;
                const mouseY = e.clientY - this.canvas.offsetTop;

                const worldX = (mouseX - this.offsetX) / this.zoom;
                const worldY = (mouseY - this.offsetY) / this.zoom;

                this.zoom = newZoom;

                this.offsetX = mouseX - worldX * this.zoom;
                this.offsetY = mouseY - worldY * this.zoom;

                this.needsRender = true;
            }
        }, { passive: false });

        // Touch events mejorados
        this.canvas.addEventListener('touchstart', (e) => this.onTouchStart(e), { passive: false });
        this.canvas.addEventListener('touchmove', (e) => this.onTouchMove(e), { passive: false });
        this.canvas.addEventListener('touchend', () => this.onTouchEnd());

        // Prevenir scroll en móvil
        this.canvas.addEventListener('touchmove', (e) => {
            if (e.touches.length > 1) e.preventDefault();
        }, { passive: false });
    }

    onMouseDown(e) {
        this.isDragging = true;
        this.lastMouseX = e.clientX;
        this.lastMouseY = e.clientY;
        this.canvas.style.cursor = 'grabbing';
    }

    onMouseMove(e) {
        if (this.isDragging) {
            const dx = e.clientX - this.lastMouseX;
            const dy = e.clientY - this.lastMouseY;
            this.offsetX += dx;
            this.offsetY += dy;
            this.lastMouseX = e.clientX;
            this.lastMouseY = e.clientY;
            this.needsRender = true;
        } else {
            // Detectar hover optimizado
            const rect = this.canvas.getBoundingClientRect();
            const mouseX = (e.clientX - rect.left - this.offsetX) / this.zoom;
            const mouseY = (e.clientY - rect.top - this.offsetY) / this.zoom;

            const prevHover = this.asientoHover;
            this.asientoHover = null;

            // Solo buscar en asientos visibles
            const asientosVisibles = this.getAsientosVisibles();
            for (let asiento of asientosVisibles) {
                if (this.puntoEnAsiento(mouseX, mouseY, asiento)) {
                    this.asientoHover = asiento;
                    this.canvas.style.cursor = asiento.disponible ? 'pointer' : 'not-allowed';
                    this.mostrarTooltip(e, asiento);
                    if (prevHover !== asiento) {
                        this.needsRender = true;
                    }
                    return;
                }
            }

            this.canvas.style.cursor = this.isDragging ? 'grabbing' : 'grab';
            this.ocultarTooltip();
            if (prevHover !== null) {
                this.needsRender = true;
            }
        }
    }

    onMouseUp() {
        this.isDragging = false;
        this.canvas.style.cursor = 'grab';
    }

    onClick(e) {
        if (this.isDragging) return;

        const rect = this.canvas.getBoundingClientRect();
        const mouseX = (e.clientX - rect.left - this.offsetX) / this.zoom;
        const mouseY = (e.clientY - rect.top - this.offsetY) / this.zoom;

        const asientosVisibles = this.getAsientosVisibles();
        for (let asiento of asientosVisibles) {
            if (this.puntoEnAsiento(mouseX, mouseY, asiento)) {
                if (asiento.disponible) {
                    const index = this.asientosSeleccionados.findIndex(a =>
                        a.numero === asiento.numero && a.zona === asiento.zona
                    );

                    if (index !== -1) {
                        this.asientosSeleccionados.splice(index, 1);
                        console.log('❌ Asiento deseleccionado:', asiento.numero);
                    } else {
                        if (this.asientosSeleccionados.length < 10) {
                            this.asientosSeleccionados.push(asiento);
                            console.log('✅ Asiento seleccionado:', asiento.numero);
                        } else {
                            this.mostrarAlerta('Máximo 10 asientos por compra');
                            return;
                        }
                    }

                    this.mostrarInfoAsientos();
                    this.needsRender = true;
                }
                return;
            }
        }
    }

    onTouchStart(e) {
        e.preventDefault();

        if (e.touches.length === 1) {
            this.isDragging = true;
            this.lastTouchX = e.touches[0].clientX;
            this.lastTouchY = e.touches[0].clientY;
        } else if (e.touches.length === 2) {
            this.isPinching = true;
            this.isDragging = false;

            const dx = e.touches[0].clientX - e.touches[1].clientX;
            const dy = e.touches[0].clientY - e.touches[1].clientY;
            this.touchStartDistance = Math.sqrt(dx * dx + dy * dy);
        }
    }

    onTouchMove(e) {
        e.preventDefault();

        if (e.touches.length === 1 && this.isDragging) {
            const dx = e.touches[0].clientX - this.lastTouchX;
            const dy = e.touches[0].clientY - this.lastTouchY;
            this.offsetX += dx;
            this.offsetY += dy;
            this.lastTouchX = e.touches[0].clientX;
            this.lastTouchY = e.touches[0].clientY;
            this.needsRender = true;
        } else if (e.touches.length === 2 && this.isPinching) {
            const dx = e.touches[0].clientX - e.touches[1].clientX;
            const dy = e.touches[0].clientY - e.touches[1].clientY;
            const distance = Math.sqrt(dx * dx + dy * dy);

            const delta = distance / this.touchStartDistance;
            const newZoom = this.zoom * delta;

            if (newZoom >= this.minZoom && newZoom <= this.maxZoom) {
                this.zoom = newZoom;
                this.touchStartDistance = distance;
                this.needsRender = true;
            }
        }
    }

    onTouchEnd() {
        this.isDragging = false;
        this.isPinching = false;
    }

    puntoEnAsiento(x, y, asiento) {
        return Math.abs(x - asiento.x) < asiento.size / 2 &&
            Math.abs(y - asiento.y) < asiento.size / 2;
    }

    getAsientosVisibles() {
        // Calcular bounds visibles
        const left = -this.offsetX / this.zoom - 100;
        const right = (this.width - this.offsetX) / this.zoom + 100;
        const top = -this.offsetY / this.zoom - 100;
        const bottom = (this.height - this.offsetY) / this.zoom + 100;

        return this.asientosFiltrados.filter(asiento =>
            asiento.x >= left && asiento.x <= right &&
            asiento.y >= top && asiento.y <= bottom
        );
    }

    render(currentTime = 0) {
        this.animationFrameId = requestAnimationFrame((time) => this.render(time));

        // Limitar FPS
        const elapsed = currentTime - this.lastFrameTime;
        if (elapsed < this.frameInterval) return;

        this.lastFrameTime = currentTime - (elapsed % this.frameInterval);

        // Solo renderizar si hay cambios
        if (!this.needsRender) return;
        this.needsRender = false;

        // Limpiar canvas
        const bgGradient = this.ctx.createLinearGradient(0, 0, 0, this.height);
        bgGradient.addColorStop(0, '#87CEEB');
        bgGradient.addColorStop(1, '#B0E0E6');
        this.ctx.fillStyle = bgGradient;
        this.ctx.fillRect(0, 0, this.width, this.height);

        // Guardar estado
        this.ctx.save();

        // Aplicar transformaciones
        this.ctx.translate(this.offsetX, this.offsetY);
        this.ctx.scale(this.zoom, this.zoom);

        // Dibujar plataforma del estadio con sombra
        this.ctx.shadowColor = 'rgba(0, 0, 0, 0.4)';
        this.ctx.shadowBlur = 30;
        this.ctx.shadowOffsetX = 10;
        this.ctx.shadowOffsetY = 10;
        this.ctx.fillStyle = '#1a237e';
        this.ctx.fillRect(-210, -290, 420, 580);
        this.ctx.shadowColor = 'transparent';
        this.ctx.shadowBlur = 0;

        // Dibujar campo desde cache
        if (this.campoCache) {
            this.ctx.drawImage(this.campoCache, -250, -325);
        }

        // Dibujar etiquetas de zonas
        this.dibujarEtiquetasZonas();

        // Dibujar asientos (solo visibles)
        this.dibujarAsientos();

        // Restaurar contexto
        this.ctx.restore();
    }

    dibujarEtiquetasZonas() {
        // Solo dibujar si el zoom es mayor a 0.5
        if (this.zoom < 0.5) return;

        // Font size constants
        const EXTRA_LARGE_FONT_SIZE = '22px';
        const LARGE_FONT_SIZE = '20px';
        const MEDIUM_FONT_SIZE = '16px';
        const SMALL_FONT_SIZE = '14px';

        this.ctx.font = `bold ${EXTRA_LARGE_FONT_SIZE} Arial`;
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';
        this.ctx.shadowColor = 'rgba(0, 0, 0, 0.8)';
        this.ctx.shadowBlur = 4;
        this.ctx.shadowOffsetX = 2;
        this.ctx.shadowOffsetY = 2;

        const etiquetas = [
            // Gol Nord sections
            { texto: 'Gol Nord', x: 0, y: -470, color: '#FFD700', fontSize: LARGE_FONT_SIZE },
            { texto: 'Gol Nord', x: 0, y: -370, color: '#FFD700', fontSize: MEDIUM_FONT_SIZE },
            
            // Gol Sud sections
            { texto: 'Gol Sud', x: 0, y: 370, color: '#FFD700', fontSize: MEDIUM_FONT_SIZE },
            { texto: 'Gol Sud', x: 0, y: 470, color: '#FFD700', fontSize: LARGE_FONT_SIZE },
            
            // Grada total (external labels on sides)
            { texto: 'Grada total', x: -520, y: 0, rotacion: -Math.PI / 2, color: '#FFD700', fontSize: LARGE_FONT_SIZE },
            { texto: 'Grada total', x: 520, y: 0, rotacion: Math.PI / 2, color: '#FFD700', fontSize: LARGE_FONT_SIZE },
            
            // Grada lateral (middle sections)
            { texto: 'Grada lateral', x: -430, y: 0, rotacion: -Math.PI / 2, color: '#FFFFFF', fontSize: MEDIUM_FONT_SIZE },
            { texto: 'Grada lateral', x: 430, y: 0, rotacion: Math.PI / 2, color: '#FFFFFF', fontSize: MEDIUM_FONT_SIZE },
            
            // Tribuna (inner sections)
            { texto: 'tribuna', x: -320, y: 0, rotacion: -Math.PI / 2, color: '#FF8C00', fontSize: SMALL_FONT_SIZE },
            { texto: 'tribuna', x: 320, y: 0, rotacion: Math.PI / 2, color: '#FF8C00', fontSize: SMALL_FONT_SIZE }
        ];

        etiquetas.forEach(etiqueta => {
            this.ctx.save();
            this.ctx.translate(etiqueta.x, etiqueta.y);
            if (etiqueta.rotacion) {
                this.ctx.rotate(etiqueta.rotacion);
            }

            this.ctx.font = `bold ${etiqueta.fontSize || EXTRA_LARGE_FONT_SIZE} Arial`;
            const metrics = this.ctx.measureText(etiqueta.texto);
            
            // Background box
            this.ctx.fillStyle = 'rgba(26, 35, 126, 0.85)';
            this.ctx.fillRect(-metrics.width / 2 - 12, -15, metrics.width + 24, 30);

            this.ctx.strokeStyle = etiqueta.color;
            this.ctx.lineWidth = 2;
            this.ctx.strokeRect(-metrics.width / 2 - 12, -15, metrics.width + 24, 30);

            this.ctx.fillStyle = etiqueta.color;
            this.ctx.fillText(etiqueta.texto, 0, 0);

            this.ctx.restore();
        });

        this.ctx.shadowColor = 'transparent';
        this.ctx.shadowBlur = 0;
    }

    dibujarAsientos() {
        const asientosVisibles = this.getAsientosVisibles();

        asientosVisibles.forEach(asiento => {
            let color;

            const estaSeleccionado = this.asientosSeleccionados.some(a =>
                a.numero === asiento.numero && a.zona === asiento.zona
            );

            if (estaSeleccionado) {
                color = this.colors.seleccionado;
            } else if (this.asientoHover && this.asientoHover.id === asiento.id && asiento.disponible) {
                color = this.colors.hover;
            } else if (asiento.disponible) {
                color = this.colors.disponible;
            } else {
                color = this.colors.ocupado;
            }

            const x = asiento.x - asiento.size / 2;
            const y = asiento.y - asiento.size / 2;
            const size = asiento.size;

            // Fondo de zona premium (tribuna)
            if (asiento.zona === 'tribuna') {
                this.ctx.fillStyle = asiento.color;
                this.ctx.fillRect(x - 1, y - 1, size + 2, size + 2);
            } else {
                this.ctx.fillStyle = 'rgba(40, 53, 147, 0.3)';
                this.ctx.fillRect(x - 1, y - 1, size + 2, size + 2);
            }

            // Asiento con efecto 3D
            this.ctx.fillStyle = color;
            this.ctx.fillRect(x, y, size, size);

            // Highlight superior
            const gradientHighlight = this.ctx.createLinearGradient(x, y, x, y + size / 3);
            gradientHighlight.addColorStop(0, this.lightenColor(color, 30));
            gradientHighlight.addColorStop(1, color);
            this.ctx.fillStyle = gradientHighlight;
            this.ctx.fillRect(x, y, size, size / 3);

            // Borde
            this.ctx.strokeStyle = this.darkenColor(color, 20);
            this.ctx.lineWidth = 1.5;
            this.ctx.strokeRect(x, y, size, size);

            // Indicador premium para tribuna
            if (asiento.zona === 'tribuna' && asiento.disponible && this.zoom > 0.8) {
                this.ctx.fillStyle = '#FFD700';
                this.ctx.beginPath();
                this.ctx.arc(asiento.x, asiento.y, 2, 0, Math.PI * 2);
                this.ctx.fill();
            }

            // Indicador de selección mejorado
            if (estaSeleccionado) {
                this.ctx.strokeStyle = '#FFF';
                this.ctx.lineWidth = 2;
                this.ctx.strokeRect(x - 2, y - 2, size + 4, size + 4);
            }
        });
    }

    lightenColor(color, percent) {
        const num = parseInt(color.replace('#', ''), 16);
        const amt = Math.round(2.55 * percent);
        const R = Math.min(255, (num >> 16) + amt);
        const G = Math.min(255, (num >> 8 & 0x00FF) + amt);
        const B = Math.min(255, (num & 0x0000FF) + amt);
        return '#' + (0x1000000 + R * 0x10000 + G * 0x100 + B).toString(16).slice(1);
    }

    darkenColor(color, percent) {
        const num = parseInt(color.replace('#', ''), 16);
        const amt = Math.round(2.55 * percent);
        const R = Math.max(0, (num >> 16) - amt);
        const G = Math.max(0, (num >> 8 & 0x00FF) - amt);
        const B = Math.max(0, (num & 0x0000FF) - amt);
        return '#' + (0x1000000 + R * 0x10000 + G * 0x100 + B).toString(16).slice(1);
    }

    mostrarTooltip(e, asiento) {
        const tooltip = document.getElementById('tooltip');
        if (!tooltip) return;

        tooltip.style.display = 'block';
        tooltip.style.left = e.clientX + 10 + 'px';
        tooltip.style.top = e.clientY + 10 + 'px';

        const iconoZona = asiento.zona === 'tribuna' ? '⭐' : '🎫';
        const colorEstado = asiento.disponible ? '#4CAF50' : '#f44336';
        const textoEstado = asiento.disponible ? 'Disponible' : 'Ocupado';

        tooltip.innerHTML = `
            <div style="font-size: 0.95rem;">
                <div style="font-weight: 700; color: #FFD700; margin-bottom: 5px;">
                    ${iconoZona} ${asiento.zona}
                </div>
                <div style="margin: 3px 0;">
                    Asiento <strong>#${asiento.numero}</strong>
                </div>
                <div style="margin: 3px 0; font-weight: 700; color: #4CAF50;">
                    ${asiento.precio.toFixed(2)}€
                </div>
                <div style="margin-top: 5px; padding-top: 5px; border-top: 1px solid rgba(255,255,255,0.3);">
                    <span style="color: ${colorEstado}; font-weight: 600;">● ${textoEstado}</span>
                </div>
            </div>
        `;
    }

    ocultarTooltip() {
        const tooltip = document.getElementById('tooltip');
        if (tooltip) tooltip.style.display = 'none';
    }

    mostrarInfoAsientos() {
        const infoDiv = document.getElementById('asiento-info');
        const btnConfirmar = document.getElementById('confirmar-btn');

        if (!infoDiv || !btnConfirmar) return;

        if (this.asientosSeleccionados.length === 0) {
            infoDiv.style.display = 'none';
            btnConfirmar.style.display = 'none';
            return;
        }

        const asientosPorZona = {};
        let precioTotal = 0;

        this.asientosSeleccionados.forEach(asiento => {
            if (!asientosPorZona[asiento.zona]) {
                asientosPorZona[asiento.zona] = {
                    asientos: [],
                    precio: asiento.precio,
                    icono: asiento.zona === 'tribuna' ? '⭐' : '🎟️'
                };
            }
            asientosPorZona[asiento.zona].asientos.push(asiento.numero);
            precioTotal += asiento.precio;
        });

        let html = `
            <h4 style="display: flex; align-items: center; gap: 8px; margin: 0 0 15px 0;">
                <i class="fas fa-chair"></i>
                Selección (${this.asientosSeleccionados.length}/10)
            </h4>
        `;

        for (const [zona, data] of Object.entries(asientosPorZona)) {
            const asientosOrdenados = data.asientos.sort((a, b) => a - b);
            const subtotal = data.asientos.length * data.precio;

            html += `
                <div style="background: linear-gradient(135deg, #f8f9fa, #e9ecef); padding: 12px; border-radius: 10px; margin: 10px 0; border-left: 4px solid ${zona === 'tribuna' ? '#FF8C00' : '#283593'};">
                    <div style="font-weight: 700; color: #1a237e; margin-bottom: 6px; font-size: 0.95rem;">
                        ${data.icono} ${zona}
                    </div>
                    <div style="color: #666; font-size: 0.85rem; margin-bottom: 4px;">
                        ${asientosOrdenados.length} asiento${asientosOrdenados.length > 1 ? 's' : ''}: ${asientosOrdenados.join(', ')}
                    </div>
                    <div style="color: #4CAF50; font-weight: 600; font-size: 0.9rem;">
                        ${data.asientos.length} × ${data.precio.toFixed(2)}€ = ${subtotal.toFixed(2)}€
                    </div>
                </div>
            `;
        }

        html += `
            <div style="margin-top: 15px; padding: 18px; background: linear-gradient(135deg, #fff9c4, #fff59d); border-radius: 14px; text-align: center; border: 3px dashed #ffd700; box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);">
                <div style="font-size: 0.85rem; color: #666; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 5px;">
                    Total a Pagar
                </div>
                <div style="font-size: 2.2rem; font-weight: 900; color: #1a237e; text-shadow: 2px 2px 4px rgba(0,0,0,0.1);">
                    ${precioTotal.toFixed(2)}€
                </div>
                <div style="font-size: 0.8rem; color: #666; margin-top: 5px;">
                    ${this.asientosSeleccionados.length} entrada${this.asientosSeleccionados.length !== 1 ? 's' : ''}
                </div>
            </div>
            <button onclick="window.estadioInstance.limpiarSeleccion()" style="
                width: 100%;
                margin-top: 12px;
                padding: 11px;
                background: #f5f5f5;
                color: #666;
                border: 2px solid #ddd;
                border-radius: 10px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
                font-size: 0.9rem;
            " onmouseover="this.style.background='#e0e0e0'; this.style.borderColor='#bbb'" onmouseout="this.style.background='#f5f5f5'; this.style.borderColor='#ddd'">
                <i class="fas fa-times" style="margin-right: 6px;"></i>Limpiar Selección
            </button>
        `;

        infoDiv.style.display = 'block';
        infoDiv.innerHTML = html;
        btnConfirmar.style.display = 'block';
    }

    limpiarSeleccion() {
        this.asientosSeleccionados = [];
        this.mostrarInfoAsientos();
        this.needsRender = true;
    }

    actualizarPanelInfo() {
        const panelInfo = document.getElementById('zona-info');
        if (!panelInfo) return;

        let html = '';

        this.zonasData.forEach((zona, index) => {
            const porcentajeDisponible = (zona.disponibles / zona.capacidadTotal * 100).toFixed(0);
            const porcentajeOcupado = ((zona.capacidadTotal - zona.disponibles) / zona.capacidadTotal * 100).toFixed(0);
            const colorBarra = '#4CAF50'; // Verde para disponibles
            const iconoZona = zona.nombre === 'tribuna' ? '⭐' : '🎟️';

            html += `
                <div style="margin: 15px 0; padding: 15px; background: linear-gradient(135deg, #ffffff, #f8f9fa); border-radius: 12px; border: 2px solid #e0e0e0; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                        <span style="font-weight: 700; color: #1a237e; font-size: 0.95rem;">
                            ${iconoZona} ${zona.nombre}
                        </span>
                        <span style="font-weight: 700; color: #4CAF50; font-size: 1.1rem;">
                            ${zona.precio.toFixed(2)}€
                        </span>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 0.85rem;">
                        <span style="color: #666;">Disponibles</span>
                        <span style="color: #1a237e; font-weight: 600;">${zona.disponibles}/${zona.capacidadTotal}</span>
                    </div>
                    <div style="background: #e0e0e0; height: 8px; border-radius: 4px; overflow: hidden; position: relative;">
                        <div style="background: ${colorBarra}; height: 100%; width: ${porcentajeDisponible}%; transition: width 0.3s ease; border-radius: 4px;"></div>
                    </div>
                    <div style="text-align: right; font-size: 0.75rem; color: #999; margin-top: 4px;">
                        ${porcentajeOcupado}% ocupado
                    </div>
                </div>
            `;
        });

        panelInfo.innerHTML = html;
    }

    actualizarLoading(mensaje, porcentaje) {
        const loading = document.getElementById('loading');
        if (loading) {
            const p = loading.querySelector('p');
            if (p) p.textContent = `${mensaje} (${porcentaje}%)`;
        }
    }

    mostrarError(mensaje) {
        const loading = document.getElementById('loading');
        if (!loading) return;

        loading.style.background = 'rgba(26, 35, 126, 0.98)';
        loading.innerHTML = `
            <div style="color: white; text-align: center; padding: 40px; max-width: 600px;">
                <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #FFC107; margin-bottom: 20px;"></i>
                <h3 style="margin: 15px 0;">Error al cargar el estadio</h3>
                <p style="font-size: 1.1rem; margin: 15px 0;">${mensaje}</p>
                <button onclick="location.reload()" style="
                    background: linear-gradient(135deg, #ffd700, #ffed4e);
                    color: #1a237e;
                    border: none;
                    padding: 12px 24px;
                    border-radius: 8px;
                    font-weight: 700;
                    cursor: pointer;
                    margin: 20px 10px;
                    font-size: 1rem;
                ">
                    <i class="fas fa-sync-alt" style="margin-right: 8px;"></i>Reintentar
                </button>
                <br>
                <a href="/eventos" style="color: #ffd700; text-decoration: underline; font-size: 0.95rem;">
                    <i class="fas fa-arrow-left" style="margin-right: 5px;"></i>Volver a Eventos
                </a>
            </div>
        `;
    }

    mostrarAlerta(mensaje) {
        // Crear alerta temporal
        const alerta = document.createElement('div');
        alerta.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: linear-gradient(135deg, #ff5252, #f44336);
            color: white;
            padding: 20px 30px;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.3);
            z-index: 10000;
            font-weight: 600;
            font-size: 1.1rem;
            animation: fadeIn 0.3s ease;
        `;
        alerta.textContent = mensaje;
        document.body.appendChild(alerta);

        setTimeout(() => {
            alerta.style.animation = 'fadeOut 0.3s ease';
            setTimeout(() => alerta.remove(), 300);
        }, 2000);
    }

    resetearVista() {
        this.offsetX = this.width / 2;
        this.offsetY = this.height / 2;
        this.zoom = 1;
        this.needsRender = true;
    }

    ajustarZoom(delta) {
        const newZoom = this.zoom + delta;
        if (newZoom >= this.minZoom && newZoom <= this.maxZoom) {
            this.zoom = newZoom;
            this.needsRender = true;
        }
    }

    zoomAZona(nombreZona) {
        // Encontrar centro de la zona
        const asientosZona = this.asientosFiltrados.filter(a => a.zona === nombreZona);
        if (asientosZona.length === 0) return;

        const sumX = asientosZona.reduce((sum, a) => sum + a.x, 0);
        const sumY = asientosZona.reduce((sum, a) => sum + a.y, 0);
        const centroX = sumX / asientosZona.length;
        const centroY = sumY / asientosZona.length;

        // Animar zoom a la zona
        const targetZoom = 1.5;
        const targetOffsetX = this.width / 2 - centroX * targetZoom;
        const targetOffsetY = this.height / 2 - centroY * targetZoom;

        this.animarCamara(targetOffsetX, targetOffsetY, targetZoom);
    }

    animarCamara(targetX, targetY, targetZoom, duration = 500) {
        const startX = this.offsetX;
        const startY = this.offsetY;
        const startZoom = this.zoom;
        const startTime = performance.now();

        const animate = (currentTime) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);

            // Easing function (ease-out)
            const eased = 1 - Math.pow(1 - progress, 3);

            this.offsetX = startX + (targetX - startX) * eased;
            this.offsetY = startY + (targetY - startY) * eased;
            this.zoom = startZoom + (targetZoom - startZoom) * eased;
            this.needsRender = true;

            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };

        requestAnimationFrame(animate);
    }

    confirmarCompra() {
        if (this.asientosSeleccionados.length === 0) {
            this.mostrarAlerta('Por favor, selecciona al menos un asiento');
            return;
        }

        // Calcular precio total
        let precioTotal = 0;
        this.asientosSeleccionados.forEach(asiento => {
            precioTotal += asiento.precio;
        });

        // Verificar saldo
        fetch('/api/usuario/saldo')
            .then(response => response.json())
            .then(data => {
                const saldo = data.saldo || 0;

                if (saldo < precioTotal) {
                    this.mostrarErrorSaldo(precioTotal, saldo);
                    return;
                }

                this.realizarCompraMultiple();
            })
            .catch(error => {
                console.error('Error al verificar saldo:', error);
                this.realizarCompraMultiple();
            });
    }

    async realizarCompraMultiple() {
        const datosCompra = this.asientosSeleccionados.map(asiento => ({
            zona: asiento.zona,
            asiento: asiento.numero
        }));

        const btnConfirmar = document.getElementById('confirmar-btn');
        if (btnConfirmar) {
            btnConfirmar.disabled = true;
            btnConfirmar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Procesando...';
        }

        try {
            const formData = new URLSearchParams();
            formData.append('asientos', JSON.stringify(datosCompra));

            const response = await fetch(`/eventos/${this.eventoId}/comprar-asientos-individuales`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData.toString()
            });

            const data = await response.json();

            if (data.success) {
                this.mostrarExitoCompra(data.mensaje);
                setTimeout(() => {
                    window.location.href = data.redirect || '/tickets';
                }, 2000);
            } else {
                this.mostrarAlerta(data.error || 'Error al procesar la compra');

                if (data.redirect) {
                    setTimeout(() => window.location.href = data.redirect, 2000);
                } else if (btnConfirmar) {
                    btnConfirmar.disabled = false;
                    btnConfirmar.innerHTML = '<i class="fas fa-check"></i> Confirmar Compra';
                }
            }
        } catch (error) {
            console.error('Error al realizar compra:', error);
            this.mostrarAlerta('Error de conexión. Inténtalo de nuevo.');

            if (btnConfirmar) {
                btnConfirmar.disabled = false;
                btnConfirmar.innerHTML = '<i class="fas fa-check"></i> Confirmar Compra';
            }
        }
    }

    mostrarExitoCompra(mensaje) {
        const modal = document.createElement('div');
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.8);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 10000;
            animation: fadeIn 0.3s ease;
        `;

        modal.innerHTML = `
            <div style="
                background: white;
                padding: 40px;
                border-radius: 20px;
                text-align: center;
                max-width: 500px;
                box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
                animation: scaleIn 0.3s ease;
            ">
                <div style="
                    width: 80px;
                    height: 80px;
                    background: linear-gradient(135deg, #4CAF50, #45a049);
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin: 0 auto 20px;
                ">
                    <i class="fas fa-check" style="font-size: 40px; color: white;"></i>
                </div>
                <h2 style="color: #1a237e; margin-bottom: 15px; font-size: 1.8rem;">
                    ¡Compra Exitosa!
                </h2>
                <p style="color: #666; font-size: 1.1rem; margin-bottom: 20px;">
                    ${mensaje}
                </p>
                <div style="color: #999; font-size: 0.9rem;">
                    Redirigiendo a tus tickets...
                </div>
            </div>
        `;

        document.body.appendChild(modal);
    }

    mostrarErrorSaldo(precio, saldo) {
        const faltante = (precio - saldo).toFixed(2);
        const modal = document.createElement('div');
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.8);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 10000;
            animation: fadeIn 0.3s ease;
        `;

        modal.innerHTML = `
            <div style="
                background: white;
                padding: 40px;
                border-radius: 20px;
                max-width: 500px;
                text-align: center;
                box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
            ">
                <div style="
                    width: 80px;
                    height: 80px;
                    background: linear-gradient(135deg, #ff5252, #f44336);
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin: 0 auto 20px;
                ">
                    <i class="fas fa-wallet" style="font-size: 40px; color: white;"></i>
                </div>
                <h2 style="color: #1a237e; margin-bottom: 15px;">Saldo Insuficiente</h2>
                <p style="color: #666; margin-bottom: 20px;">
                    No tienes suficiente saldo para esta compra.
                </p>
                <div style="background: #f5f5f5; padding: 20px; border-radius: 12px; margin: 20px 0;">
                    <div style="display: flex; justify-content: space-between; margin: 10px 0;">
                        <span style="color: #666; font-weight: 600;">Precio:</span>
                        <strong style="color: #1a237e; font-size: 1.2rem;">${precio.toFixed(2)}€</strong>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin: 10px 0;">
                        <span style="color: #666; font-weight: 600;">Tu saldo:</span>
                        <strong style="color: #f44336; font-size: 1.2rem;">${saldo.toFixed(2)}€</strong>
                    </div>
                    <hr style="border: none; border-top: 2px dashed #ddd; margin: 15px 0;">
                    <div style="display: flex; justify-content: space-between; margin: 10px 0;">
                        <span style="color: #666; font-weight: 700;">Te faltan:</span>
                        <strong style="color: #ff5252; font-size: 1.4rem;">${faltante}€</strong>
                    </div>
                </div>
                <button onclick="window.location.href='/cuenta/perfil'" style="
                    background: linear-gradient(135deg, #ffd700, #ffed4e);
                    color: #1a237e;
                    border: none;
                    padding: 15px 30px;
                    border-radius: 50px;
                    font-weight: 700;
                    font-size: 1.1rem;
                    cursor: pointer;
                    margin: 10px;
                    box-shadow: 0 4px 15px rgba(255, 215, 0, 0.4);
                ">
                    <i class="fas fa-plus-circle" style="margin-right: 8px;"></i>Recargar Saldo
                </button>
                <button onclick="this.parentElement.parentElement.remove()" style="
                    background: #f5f5f5;
                    color: #666;
                    border: none;
                    padding: 15px 30px;
                    border-radius: 50px;
                    font-weight: 700;
                    cursor: pointer;
                    margin: 10px;
                ">Cerrar</button>
            </div>
        `;

        document.body.appendChild(modal);
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.remove();
        });
    }

    destruir() {
        if (this.animationFrameId) {
            cancelAnimationFrame(this.animationFrameId);
        }

        console.log('🧹 Destruyendo instancia del estadio...');
    }
}

// Inicialización global
let estadioInstance = null;

function inicializarEstadioCanvas(eventoIdParam) {
    console.log('🚀 Iniciando EstadioCanvas...');

    const canvas = document.getElementById('estadio-canvas');
    if (!canvas) {
        console.error('❌ No se encontró el elemento canvas');
        return;
    }

    // Usar el parámetro si se proporciona, sino buscar en el data-attribute
    let eventoId = eventoIdParam || canvas.dataset.eventoId;

    if (!eventoId) {
        console.error('❌ No se encontró el ID del evento');
        return;
    }

    console.log('✅ Evento ID:', eventoId);

    try {
        estadioInstance = new EstadioCanvas('estadio-canvas', eventoId);
        window.estadioInstance = estadioInstance;
        console.log('✅ EstadioCanvas inicializado correctamente');
    } catch (error) {
        console.error('❌ Error fatal al inicializar:', error);
    }
}

// Funciones globales para controles
function resetView() {
    if (window.estadioInstance) {
        window.estadioInstance.resetearVista();
    }
}

function resetearVista() {
    resetView();
}

function zoomIn() {
    if (window.estadioInstance) {
        window.estadioInstance.ajustarZoom(0.2);
    }
}

function zoomOut() {
    if (window.estadioInstance) {
        window.estadioInstance.ajustarZoom(-0.2);
    }
}

function ajustarZoom(cantidad) {
    if (window.estadioInstance) {
        window.estadioInstance.ajustarZoom(cantidad);
    }
}

function confirmarCompra() {
    if (window.estadioInstance) {
        window.estadioInstance.confirmarCompra();
    }
}

// Auto-inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarEstadioCanvas);
} else {
    inicializarEstadioCanvas();
}

// Estilos CSS para animaciones
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }
    
    @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
    }
    
    @keyframes scaleIn {
        from { transform: scale(0.8); opacity: 0; }
        to { transform: scale(1); opacity: 1; }
    }
    
    #estadio-canvas {
        cursor: grab;
        touch-action: none;
    }
    
    #estadio-canvas:active {
        cursor: grabbing;
    }
`;
document.head.appendChild(style);