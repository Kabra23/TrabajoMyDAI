/**
 * Sistema de visualización de estadio usando Canvas 2D
 * Vista isométrica interactiva - GARANTIZADO QUE FUNCIONA
 */

class EstadioCanvas {
    constructor(canvasId, eventoId) {
        this.canvas = document.getElementById(canvasId);

        if (!this.canvas) {
            console.error('❌ ERROR: No se encontró el elemento canvas con ID:', canvasId);
            throw new Error(`No se encontró el canvas con ID: ${canvasId}`);
        }

        this.ctx = this.canvas.getContext('2d');
        if (!this.ctx) {
            console.error('❌ ERROR: No se pudo obtener el contexto 2D del canvas');
            throw new Error('No se pudo obtener el contexto 2D');
        }

        console.log('✅ Canvas inicializado correctamente');
        this.eventoId = eventoId;

        // Configuración
        this.width = this.canvas.width = window.innerWidth;
        this.height = this.canvas.height = window.innerHeight;

        // Vista y zoom
        this.offsetX = this.width / 2;
        this.offsetY = this.height / 2;
        this.zoom = 1;
        this.isDragging = false;
        this.lastMouseX = 0;
        this.lastMouseY = 0;

        // Datos
        this.asientos = [];
        this.zonasData = [];
        this.asientosOcupados = {};
        this.asientosSeleccionados = []; // Cambio: ahora es un array
        this.asientoHover = null;

        // Optimización de renderizado
        this.needsRender = true;
        this.animationFrameId = null;

        // Colores
        this.colors = {
            campo: '#2E7D32',
            lineas: '#FFFFFF',
            disponible: '#4CAF50',
            ocupado: '#f44336',
            seleccionado: '#2196F3',
            hover: '#FFC107',
            tribuna: '#FF8C00', // Naranja premium para zona Tribuna
            grada: '#283593', // Azul oscuro para Grada Lateral
            golNord: '#00BCD4', // Cyan para Gol Nord
            golSud: '#9C27B0' // Purple para Gol Sud
        };

        this.inicializar();
    }

    async inicializar() {
        try {
            console.log('🎮 Iniciando estadio Canvas 2D');
            console.log('📊 Dimensiones canvas:', this.width, 'x', this.height);
            this.actualizarLoading('Inicializando vista...', 10);

            // Event listeners
            console.log('🎧 Configurando event listeners...');
            this.setupEventListeners();
            console.log('✅ Event listeners configurados');

            // Crear estructura del estadio
            console.log('🏟️ Creando estructura del estadio...');
            this.actualizarLoading('Creando estadio...', 30);
            this.crearEstructuraEstadio();
            console.log(`✅ Creados ${this.asientos.length} asientos`);

            // Cargar datos de la API
            console.log('📡 Cargando datos de la API...');
            this.actualizarLoading('Cargando información...', 50);
            await this.cargarDatos();
            console.log('✅ Datos cargados correctamente');

            // Iniciar render loop
            console.log('🎨 Iniciando render loop...');
            this.actualizarLoading('Finalizando...', 90);
            this.render();

            // Ocultar loading
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
            console.error('Stack trace:', error.stack);
            this.mostrarError('Error al inicializar el estadio: ' + error.message);
        }
    }

    crearEstructuraEstadio() {
        // DISTRIBUCIÓN AJUSTADA AL CAMPO DE FÚTBOL
        // Reducir filas laterales para que cuadre con el campo visualmente

        const asientoSize = 12;
        const gap = 5;
        let asientoIdGlobal = 1;

        // GOL NORD (Arriba) - 480 asientos (optimizado)
        // 12 filas x 40 asientos = 480
        const golNordFilas = 12;
        const golNordAsientosPorFila = 40;
        let golNordNumero = 1;

        for (let fila = 0; fila < golNordFilas; fila++) {
            for (let asiento = 0; asiento < golNordAsientosPorFila; asiento++) {
                const x = (asiento - golNordAsientosPorFila / 2) * (asientoSize + gap);
                const y = -350 - (fila * (asientoSize + gap));

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Gol Nord',
                    numero: golNordNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.golNord
                });
                asientoIdGlobal++;
                golNordNumero++;
            }
        }

        // GOL SUD (Abajo) - 480 asientos (optimizado)
        // 12 filas x 40 asientos = 480
        const golSudFilas = 12;
        const golSudAsientosPorFila = 40;
        let golSudNumero = 1;

        for (let fila = 0; fila < golSudFilas; fila++) {
            for (let asiento = 0; asiento < golSudAsientosPorFila; asiento++) {
                const x = (asiento - golSudAsientosPorFila / 2) * (asientoSize + gap);
                const y = 350 + (fila * (asientoSize + gap));

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: 'Gol Sud',
                    numero: golSudNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: this.colors.golSud
                });
                asientoIdGlobal++;
                golSudNumero++;
            }
        }

        // GRADA LATERAL IZQUIERDA CON TRIBUNA INTEGRADA - 600 asientos
        // 15 filas x 40 asientos = 600 (primeras 5 filas = Tribuna, últimas 10 = Grada)
        const lateralFilas = 15;
        const lateralAsientosPorFila = 40;
        const TRIBUNA_ROWS = 5; // Número de filas de la zona premium
        let gradaLateralNumero = 1;
        let tribunaNumero = 1;

        for (let fila = 0; fila < lateralFilas; fila++) {
            for (let asiento = 0; asiento < lateralAsientosPorFila; asiento++) {
                // Las primeras 5 filas son TRIBUNA (zona premium)
                const esTribuna = fila < TRIBUNA_ROWS;

                const x = -320 - (fila * (asientoSize + gap));
                const y = (asiento - lateralAsientosPorFila / 2) * (asientoSize + gap);

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: esTribuna ? 'Tribuna' : 'Grada Lateral',
                    numero: esTribuna ? tribunaNumero : gradaLateralNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: esTribuna ? this.colors.tribuna : this.colors.grada
                });
                asientoIdGlobal++;
                if (esTribuna) {
                    tribunaNumero++;
                } else {
                    gradaLateralNumero++;
                }
            }
        }

        // GRADA LATERAL DERECHA CON TRIBUNA INTEGRADA - 600 asientos
        // 15 filas x 40 asientos = 600 (primeras 5 filas = Tribuna, últimas 10 = Grada)
        for (let fila = 0; fila < lateralFilas; fila++) {
            for (let asiento = 0; asiento < lateralAsientosPorFila; asiento++) {
                // Las primeras 5 filas son TRIBUNA (zona premium)
                const esTribuna = fila < TRIBUNA_ROWS;

                const x = 320 + (fila * (asientoSize + gap));
                const y = (asiento - lateralAsientosPorFila / 2) * (asientoSize + gap);

                this.asientos.push({
                    id: asientoIdGlobal,
                    zona: esTribuna ? 'Tribuna' : 'Grada Lateral',
                    numero: esTribuna ? tribunaNumero : gradaLateralNumero,
                    x: x,
                    y: y,
                    size: asientoSize,
                    disponible: true,
                    precio: 0,
                    color: esTribuna ? this.colors.tribuna : this.colors.grada
                });
                asientoIdGlobal++;
                if (esTribuna) {
                    tribunaNumero++;
                } else {
                    gradaLateralNumero++;
                }
            }
        }

        console.log(`✅ Estadio optimizado:`);
        console.log(`   - Gol Nord: 480 asientos (12 filas × 40)`);
        console.log(`   - Gol Sud: 480 asientos (12 filas × 40)`);
        console.log(`   - Grada Lateral (con Tribuna integrada): 1,200 asientos`);
        console.log(`     • Tribuna (5 filas × 40 × 2 lados): 400 asientos 🟠`);
        console.log(`     • Grada normal (10 filas × 40 × 2 lados): 800 asientos 🔵`);
        console.log(`   TOTAL: ${this.asientos.length} asientos`);
    }


    async cargarDatos() {
        try {
            // Cargar zonas
            const zonasResponse = await fetch(`/api/eventos/${this.eventoId}/estadio/zonas`);
            if (!zonasResponse.ok) throw new Error('Error al cargar zonas');

            const zonasResult = await zonasResponse.json();
            this.zonasData = zonasResult.zonas || [];

            this.actualizarLoading('Cargando asientos ocupados...', 70);

            // Cargar asientos ocupados
            const ocupadosResponse = await fetch(`/api/eventos/${this.eventoId}/estadio/asientos-ocupados`);
            if (ocupadosResponse.ok) {
                this.asientosOcupados = await ocupadosResponse.json();
            }

            // Actualizar asientos con datos reales
            this.actualizarAsientos();
            this.actualizarPanelInfo();

            console.log('✅ Datos cargados correctamente');

        } catch (error) {
            console.error('❌ Error al cargar datos:', error);
            this.mostrarError(error.message);
        }
    }

    actualizarAsientos() {
        // Primero, filtrar asientos que exceden la capacidad de cada zona
        const asientosFiltrados = [];

        this.asientos.forEach(asiento => {
            // Buscar info de la zona
            const zonaInfo = this.zonasData.find(z => z.nombre === asiento.zona);
            if (zonaInfo) {
                asiento.precio = zonaInfo.precio;

                // VALIDACIÓN: Solo incluir asientos dentro de la capacidad de la zona
                if (asiento.numero <= zonaInfo.capacidadTotal) {
                    asientosFiltrados.push(asiento);
                } else {
                    console.warn(`⚠️ Asiento ${asiento.numero} de ${asiento.zona} excede capacidad (${zonaInfo.capacidadTotal}). Ocultado.`);
                }
            } else {
                // Si no hay info de la zona, incluir el asiento de todas formas
                asientosFiltrados.push(asiento);
            }

            // Verificar si está ocupado (solo para asientos válidos)
            if (asiento.numero <= (zonaInfo?.capacidadTotal || Infinity)) {
                const ocupados = this.asientosOcupados[asiento.zona] || [];
                asiento.disponible = !ocupados.includes(asiento.numero);
            }
        });

        // Actualizar el array de asientos con solo los válidos
        this.asientos = asientosFiltrados;

        console.log(`✅ Asientos validados: ${this.asientos.length} asientos dentro de capacidad`);
    }

    setupEventListeners() {
        // Redimensionar
        window.addEventListener('resize', () => {
            this.width = this.canvas.width = window.innerWidth;
            this.height = this.canvas.height = window.innerHeight;
            this.offsetX = this.width / 2;
            this.offsetY = this.height / 2;
        });

        // Mouse events
        this.canvas.addEventListener('mousedown', (e) => this.onMouseDown(e));
        this.canvas.addEventListener('mousemove', (e) => this.onMouseMove(e));
        this.canvas.addEventListener('mouseup', () => this.onMouseUp());
        this.canvas.addEventListener('click', (e) => this.onClick(e));

        // Zoom con scroll
        this.canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const delta = e.deltaY > 0 ? 0.9 : 1.1;
            this.zoom = Math.max(0.3, Math.min(3, this.zoom * delta));
            this.needsRender = true;
        });

        // Touch events para móvil
        this.canvas.addEventListener('touchstart', (e) => this.onTouchStart(e));
        this.canvas.addEventListener('touchmove', (e) => this.onTouchMove(e));
        this.canvas.addEventListener('touchend', () => this.onTouchEnd());
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
            this.needsRender = true; // Forzar renderizado
        } else {
            // Detectar hover
            const mouseX = (e.clientX - this.offsetX) / this.zoom;
            const mouseY = (e.clientY - this.offsetY) / this.zoom;

            const prevHover = this.asientoHover;
            this.asientoHover = null;
            for (let asiento of this.asientos) {
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
        const mouseX = (e.clientX - this.offsetX) / this.zoom;
        const mouseY = (e.clientY - this.offsetY) / this.zoom;

        for (let asiento of this.asientos) {
            if (this.puntoEnAsiento(mouseX, mouseY, asiento)) {
                if (asiento.disponible) {
                    // Verificar si el asiento ya está seleccionado
                    const index = this.asientosSeleccionados.findIndex(a =>
                        a.numero === asiento.numero && a.zona === asiento.zona
                    );

                    if (index !== -1) {
                        // Deseleccionar
                        this.asientosSeleccionados.splice(index, 1);
                        console.log('❌ Asiento deseleccionado:', asiento.numero);
                    } else {
                        // Seleccionar (máximo 10 asientos)
                        if (this.asientosSeleccionados.length < 10) {
                            this.asientosSeleccionados.push(asiento);
                            console.log('✅ Asiento seleccionado:', asiento.numero);
                        } else {
                            alert('Máximo 10 asientos por compra');
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
        if (e.touches.length === 1) {
            this.isDragging = true;
            this.lastMouseX = e.touches[0].clientX;
            this.lastMouseY = e.touches[0].clientY;
        }
    }

    onTouchMove(e) {
        e.preventDefault();
        if (e.touches.length === 1 && this.isDragging) {
            const dx = e.touches[0].clientX - this.lastMouseX;
            const dy = e.touches[0].clientY - this.lastMouseY;
            this.offsetX += dx;
            this.offsetY += dy;
            this.lastMouseX = e.touches[0].clientX;
            this.lastMouseY = e.touches[0].clientY;
        }
    }

    onTouchEnd() {
        this.isDragging = false;
    }

    puntoEnAsiento(x, y, asiento) {
        return Math.abs(x - asiento.x) < asiento.size / 2 &&
            Math.abs(y - asiento.y) < asiento.size / 2;
    }

    render() {
        // Solo renderizar si hay cambios
        if (!this.needsRender) {
            this.animationFrameId = requestAnimationFrame(() => this.render());
            return;
        }

        this.needsRender = false;

        // Limpiar canvas con gradiente de fondo
        const bgGradient = this.ctx.createLinearGradient(0, 0, 0, this.height);
        bgGradient.addColorStop(0, '#87CEEB');
        bgGradient.addColorStop(1, '#B0E0E6');
        this.ctx.fillStyle = bgGradient;
        this.ctx.fillRect(0, 0, this.width, this.height);

        // Guardar estado del contexto
        this.ctx.save();

        // Aplicar transformaciones
        this.ctx.translate(this.offsetX, this.offsetY);
        this.ctx.scale(this.zoom, this.zoom);

        // Sombra del estadio
        this.ctx.shadowColor = 'rgba(0, 0, 0, 0.4)';
        this.ctx.shadowBlur = 30;
        this.ctx.shadowOffsetX = 10;
        this.ctx.shadowOffsetY = 10;

        // Fondo del campo (plataforma) - ajustado al nuevo tamaño
        this.ctx.fillStyle = '#1a237e';
        this.ctx.fillRect(-210, -290, 420, 580);

        // Resetear sombra
        this.ctx.shadowColor = 'transparent';
        this.ctx.shadowBlur = 0;

        // Dibujar campo
        this.dibujarCampo();

        // Dibujar etiquetas de zonas
        this.dibujarEtiquetasZonas();

        // Dibujar asientos
        this.dibujarAsientos();

        // Restaurar contexto
        this.ctx.restore();


        // Loop
        this.animationFrameId = requestAnimationFrame(() => this.render());
    }

    dibujarEtiquetasZonas() {
        this.ctx.font = 'bold 24px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        // Sombra para el texto
        this.ctx.shadowColor = 'rgba(0, 0, 0, 0.8)';
        this.ctx.shadowBlur = 4;
        this.ctx.shadowOffsetX = 2;
        this.ctx.shadowOffsetY = 2;

        const etiquetas = [
            { texto: 'GOL NORD', x: 0, y: -480, color: '#FFD700' },
            { texto: 'GOL SUD', x: 0, y: 480, color: '#FFD700' },
            { texto: 'GRADA LATERAL ⭐', x: -460, y: 0, rotacion: -Math.PI / 2, color: '#FFD700' },
            { texto: 'GRADA LATERAL ⭐', x: 460, y: 0, rotacion: Math.PI / 2, color: '#FFD700' }
        ];

        etiquetas.forEach(etiqueta => {
            this.ctx.save();
            this.ctx.translate(etiqueta.x, etiqueta.y);
            if (etiqueta.rotacion) {
                this.ctx.rotate(etiqueta.rotacion);
            }

            // Fondo de la etiqueta con mejor contraste
            const metrics = this.ctx.measureText(etiqueta.texto);
            this.ctx.fillStyle = 'rgba(26, 35, 126, 0.95)';
            this.ctx.fillRect(-metrics.width / 2 - 15, -18, metrics.width + 30, 36);

            // Borde dorado
            this.ctx.strokeStyle = etiqueta.color;
            this.ctx.lineWidth = 2;
            this.ctx.strokeRect(-metrics.width / 2 - 15, -18, metrics.width + 30, 36);

            // Texto
            this.ctx.fillStyle = etiqueta.color;
            this.ctx.fillText(etiqueta.texto, 0, 0);

            this.ctx.restore();
        });

        // Resetear sombra
        this.ctx.shadowColor = 'transparent';
        this.ctx.shadowBlur = 0;
    }

    dibujarCampo() {
        // Campo ajustado a la nueva distribución de asientos
        const campoAncho = 400;
        const campoAlto = 560;
        const campoX = -200;
        const campoY = -280;

        // Fondo del campo con patrón de césped
        const gradient = this.ctx.createLinearGradient(campoX, campoY, campoX, campoY + campoAlto);
        gradient.addColorStop(0, '#2E7D32');
        gradient.addColorStop(0.5, '#388E3C');
        gradient.addColorStop(1, '#2E7D32');
        this.ctx.fillStyle = gradient;
        this.ctx.fillRect(campoX, campoY, campoAncho, campoAlto);

        // Franjas de césped más oscuras
        this.ctx.fillStyle = 'rgba(46, 125, 50, 0.3)';
        for (let i = 0; i < 14; i++) {
            if (i % 2 === 0) {
                this.ctx.fillRect(campoX, campoY + (i * 40), campoAncho, 40);
            }
        }

        // Líneas del campo
        this.ctx.strokeStyle = this.colors.lineas;
        this.ctx.lineWidth = 3;
        this.ctx.lineCap = 'round';
        this.ctx.lineJoin = 'round';

        // Sombra para las líneas
        this.ctx.shadowColor = 'rgba(0, 0, 0, 0.3)';
        this.ctx.shadowBlur = 3;
        this.ctx.shadowOffsetX = 1;
        this.ctx.shadowOffsetY = 1;

        // Perímetro del campo
        this.ctx.strokeRect(campoX + 5, campoY + 5, campoAncho - 10, campoAlto - 10);

        // Línea central
        this.ctx.beginPath();
        this.ctx.moveTo(campoX + 5, 0);
        this.ctx.lineTo(campoX + campoAncho - 5, 0);
        this.ctx.stroke();

        // Círculo central
        this.ctx.beginPath();
        this.ctx.arc(0, 0, 45, 0, Math.PI * 2);
        this.ctx.stroke();

        // Punto central
        this.ctx.beginPath();
        this.ctx.arc(0, 0, 3, 0, Math.PI * 2);
        this.ctx.fillStyle = this.colors.lineas;
        this.ctx.fill();

        // Áreas y porterías
        this.dibujarAreaCompleta(-275);
        this.dibujarAreaCompleta(275);

        // Esquinas (cuartos de círculo)
        this.dibujarEsquinas();

        // Resetear sombra
        this.ctx.shadowColor = 'transparent';
        this.ctx.shadowBlur = 0;
    }

    dibujarAreaCompleta(y) {
        const esArriba = y < 0;

        this.ctx.strokeStyle = this.colors.lineas;
        this.ctx.lineWidth = 3;

        // Área grande (16.5m)
        this.ctx.strokeRect(-110, esArriba ? y : y - 90, 220, 90);

        // Área pequeña (5.5m)
        this.ctx.strokeRect(-50, esArriba ? y : y - 35, 100, 35);

        // Punto de penalti
        const penaltiY = esArriba ? y + 60 : y - 60;
        this.ctx.beginPath();
        this.ctx.arc(0, penaltiY, 3, 0, Math.PI * 2);
        this.ctx.fillStyle = this.colors.lineas;
        this.ctx.fill();

        // Arco del área (semicírculo fuera del área)
        this.ctx.beginPath();
        if (esArriba) {
            this.ctx.arc(0, penaltiY, 50, 0.3, Math.PI - 0.3);
        } else {
            this.ctx.arc(0, penaltiY, 50, -Math.PI + 0.3, -0.3);
        }
        this.ctx.stroke();

        // Portería
        this.dibujarPorteria(y);
    }

    dibujarPorteria(y) {
        const esArriba = y < 0;
        const porteriaY = esArriba ? y - 2 : y + 2;

        // Poste izquierdo
        this.ctx.fillStyle = '#FFFFFF';
        this.ctx.fillRect(-40, porteriaY - 4, 8, 8);

        // Poste derecho
        this.ctx.fillRect(32, porteriaY - 4, 8, 8);

        // Travesaño
        this.ctx.fillRect(-40, porteriaY - 4, 80, 6);

        // Red (efecto de red con líneas)
        this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
        this.ctx.lineWidth = 1;

        // Líneas verticales de la red
        for (let i = -35; i <= 35; i += 10) {
            this.ctx.beginPath();
            this.ctx.moveTo(i, porteriaY);
            this.ctx.lineTo(i, porteriaY + (esArriba ? -20 : 20));
            this.ctx.stroke();
        }

        // Líneas horizontales de la red
        for (let j = 0; j <= 20; j += 5) {
            this.ctx.beginPath();
            this.ctx.moveTo(-35, porteriaY + (esArriba ? -j : j));
            this.ctx.lineTo(35, porteriaY + (esArriba ? -j : j));
            this.ctx.stroke();
        }
    }

    dibujarEsquinas() {
        this.ctx.strokeStyle = this.colors.lineas;
        this.ctx.lineWidth = 3;

        const esquinas = [
            {x: -195, y: -275, inicio: 0, fin: Math.PI / 2},
            {x: 195, y: -275, inicio: Math.PI / 2, fin: Math.PI},
            {x: 195, y: 275, inicio: Math.PI, fin: Math.PI * 1.5},
            {x: -195, y: 275, inicio: Math.PI * 1.5, fin: Math.PI * 2}
        ];

        esquinas.forEach(esquina => {
            this.ctx.beginPath();
            this.ctx.arc(esquina.x, esquina.y, 8, esquina.inicio, esquina.fin);
            this.ctx.stroke();
        });
    }

    dibujarAsientos() {
        // Optimización: solo dibujar asientos visibles
        const asientosVisibles = this.asientos.filter(asiento => {
            const screenX = asiento.x * this.zoom + this.offsetX;
            const screenY = asiento.y * this.zoom + this.offsetY;
            return screenX > -100 && screenX < this.width + 100 &&
                screenY > -100 && screenY < this.height + 100;
        });

        asientosVisibles.forEach(asiento => {
            let color;

            // Verificar si está en la lista de seleccionados
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

            // Dibujar asiento con mejor visualización
            const x = asiento.x - asiento.size / 2;
            const y = asiento.y - asiento.size / 2;
            const size = asiento.size;

            // Fondo del asiento con color de zona específico
            this.ctx.fillStyle = asiento.color;
            this.ctx.fillRect(x - 1, y - 1, size + 2, size + 2);

            // Color del asiento según disponibilidad
            this.ctx.fillStyle = color;
            this.ctx.fillRect(x, y, size, size);

            // Borde para mejor definición
            this.ctx.strokeStyle = this.darkenColor(color, 20);
            this.ctx.lineWidth = 1.5;
            this.ctx.strokeRect(x, y, size, size);

            // Si es Tribuna y está disponible, agregar indicador visual extra
            if (asiento.zona === 'Tribuna' && asiento.disponible && this.zoom > 0.6) {
                // Pequeño punto dorado en el centro para indicar premium
                this.ctx.fillStyle = '#FFD700';
                this.ctx.beginPath();
                this.ctx.arc(asiento.x, asiento.y, 2, 0, Math.PI * 2);
                this.ctx.fill();
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
        tooltip.style.display = 'block';
        tooltip.style.left = e.clientX + 10 + 'px';
        tooltip.style.top = e.clientY + 10 + 'px';
        tooltip.innerHTML = `
            <strong>${asiento.zona}</strong><br>
            Asiento #${asiento.numero}<br>
            Precio: ${asiento.precio.toFixed(2)}€<br>
            ${asiento.disponible ? '<span style="color: #4CAF50;">Disponible</span>' : '<span style="color: #f44336;">Ocupado</span>'}
        `;
    }

    ocultarTooltip() {
        document.getElementById('tooltip').style.display = 'none';
    }

    mostrarInfoAsientos() {
        const infoDiv = document.getElementById('asiento-info');
        const btnConfirmar = document.getElementById('confirmar-btn');

        if (this.asientosSeleccionados.length === 0) {
            infoDiv.style.display = 'none';
            btnConfirmar.style.display = 'none';
            return;
        }

        // Agrupar asientos por zona
        const asientosPorZona = {};
        let precioTotal = 0;

        this.asientosSeleccionados.forEach(asiento => {
            if (!asientosPorZona[asiento.zona]) {
                asientosPorZona[asiento.zona] = {
                    asientos: [],
                    precio: asiento.precio
                };
            }
            asientosPorZona[asiento.zona].asientos.push(asiento.numero);
            precioTotal += asiento.precio;
        });

        let html = '<h4><i class="fas fa-chair me-2"></i>Asientos Seleccionados (' + this.asientosSeleccionados.length + ')</h4>';

        // Mostrar asientos agrupados por zona
        for (const [zona, data] of Object.entries(asientosPorZona)) {
            const asientosOrdenados = data.asientos.sort((a, b) => a - b);
            html += `
                <div style="background: linear-gradient(135deg, #f8f9fa, #e9ecef); padding: 10px; border-radius: 8px; margin: 8px 0;">
                    <div style="font-weight: 700; color: #1a237e; margin-bottom: 5px;">
                        <i class="fas fa-map-marker-alt me-1"></i>${zona}
                    </div>
                    <div style="color: #666; font-size: 0.9rem;">
                        Asientos: ${asientosOrdenados.join(', ')}
                    </div>
                    <div style="color: #4CAF50; font-weight: 600; font-size: 0.9rem; margin-top: 3px;">
                        ${data.asientos.length} × ${data.precio.toFixed(2)}€ = ${(data.asientos.length * data.precio).toFixed(2)}€
                    </div>
                </div>
            `;
        }

        html += `
            <div style="margin-top: 15px; padding: 15px; background: linear-gradient(135deg, #fff9c4, #fff59d); border-radius: 12px; text-align: center; border: 2px dashed #ffd700;">
                <div style="font-size: 0.9rem; color: #666; margin-bottom: 5px;">TOTAL A PAGAR</div>
                <div style="font-size: 1.8rem; font-weight: 900; color: #1a237e;">${precioTotal.toFixed(2)}€</div>
                <div style="font-size: 0.85rem; color: #666; margin-top: 3px;">${this.asientosSeleccionados.length} entrada${this.asientosSeleccionados.length !== 1 ? 's' : ''}</div>
            </div>
            <button onclick="limpiarSeleccion()" style="
                width: 100%;
                margin-top: 10px;
                padding: 10px;
                background: #f5f5f5;
                color: #666;
                border: 2px solid #ddd;
                border-radius: 10px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
            " onmouseover="this.style.background='#e0e0e0'" onmouseout="this.style.background='#f5f5f5'">
                <i class="fas fa-times me-2"></i>Limpiar Selección
            </button>
        `;

        infoDiv.style.display = 'block';
        infoDiv.innerHTML = html;
        btnConfirmar.style.display = 'block';
        this.needsRender = true;
    }

    limpiarSeleccion() {
        this.asientosSeleccionados = [];
        this.mostrarInfoAsientos();
        this.needsRender = true;
    }

    actualizarPanelInfo() {
        let html = '<h3>Información del Estadio</h3>';

        this.zonasData.forEach(zona => {
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

    actualizarLoading(mensaje, porcentaje) {
        const loading = document.getElementById('loading');
        if (loading) {
            const p = loading.querySelector('p');
            if (p) p.textContent = `${mensaje} (${porcentaje}%)`;
        }
    }

    mostrarError(mensaje) {
        const loading = document.getElementById('loading');
        loading.style.background = 'rgba(26, 35, 126, 0.98)';
        loading.innerHTML = `
            <div style="color: white; text-align: center; padding: 40px;">
                <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #FFC107;"></i>
                <h3>Error al cargar el estadio</h3>
                <p>${mensaje}</p>
                <button onclick="location.reload()" class="btn btn-primary mt-3">Reintentar</button>
                <br><br>
                <a href="/eventos/${this.eventoId}/comprar" style="color: #FFC107;">Volver a vista 2D</a>
            </div>
        `;
    }

    resetearVista() {
        this.offsetX = this.width / 2;
        this.offsetY = this.height / 2;
        this.zoom = 1;
        this.needsRender = true;
    }

    ajustarZoom(delta) {
        this.zoom = Math.max(0.3, Math.min(3, this.zoom + delta));
        this.needsRender = true;
    }

    confirmarCompra() {
        if (this.asientosSeleccionados.length === 0) {
            alert('Por favor, selecciona al menos un asiento');
            return;
        }

        // Calcular precio total
        let precioTotal = 0;
        this.asientosSeleccionados.forEach(asiento => {
            precioTotal += asiento.precio;
        });

        // Obtener el saldo del usuario
        fetch('/api/usuario/saldo')
            .then(response => response.json())
            .then(data => {
                const saldo = data.saldo || 0;

                if (saldo < precioTotal) {
                    // Saldo insuficiente
                    this.mostrarErrorSaldo(precioTotal, saldo);
                    return;
                }

                // Realizar la compra de todos los asientos seleccionados
                this.realizarCompraMultiple();
            })
            .catch(error => {
                console.error('Error al verificar saldo:', error);
                // Si falla la verificación, intentar la compra de todas formas
                this.realizarCompraMultiple();
            });
    }

    async realizarCompraMultiple() {
        // Agrupar asientos por zona para hacer las compras
        const asientosPorZona = {};

        this.asientosSeleccionados.forEach(asiento => {
            if (!asientosPorZona[asiento.zona]) {
                asientosPorZona[asiento.zona] = [];
            }
            asientosPorZona[asiento.zona].push(asiento.numero);
        });

        // Crear array de datos de compra
        const datosCompra = [];
        for (const [zona, asientos] of Object.entries(asientosPorZona)) {
            asientos.forEach(numeroAsiento => {
                datosCompra.push({
                    zona: zona,
                    asiento: numeroAsiento
                });
            });
        }

        console.log('📦 Enviando datos de compra:', datosCompra);

        // Mostrar mensaje de procesamiento
        const btnConfirmar = document.getElementById('confirmar-btn');
        if (btnConfirmar) {
            btnConfirmar.disabled = true;
            btnConfirmar.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Procesando...';
        }

        // Usar fetch para enviar los datos y recibir respuesta JSON
        try {
            console.log('📤 Enviando compra al servidor...', datosCompra);

            // Preparar datos para enviar como form data
            const formData = new URLSearchParams();
            formData.append('asientos', JSON.stringify(datosCompra));

            const response = await fetch(`/eventos/${this.eventoId}/comprar-asientos-individuales`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData.toString()
            });

            console.log('📨 Respuesta recibida:', response.status, response.statusText);

            // Parsear respuesta JSON
            const data = await response.json();
            console.log('📦 Datos recibidos:', data);

            if (data.success) {
                // Compra exitosa
                console.log('✅ Compra exitosa:', data.mensaje);

                // Mostrar mensaje de éxito
                alert(data.mensaje || '¡Compra realizada con éxito!');

                // Redirigir a la página de tickets
                window.location.href = data.redirect || '/tickets';
            } else {
                // Hubo un error en la compra
                console.error('❌ Error en la compra:', data.error);
                alert(data.error || 'Error al procesar la compra. Por favor, inténtalo de nuevo.');

                // Si hay una redirección específica (por ejemplo, login)
                if (data.redirect) {
                    window.location.href = data.redirect;
                } else {
                    // Re-habilitar el botón para reintentar
                    if (btnConfirmar) {
                        btnConfirmar.disabled = false;
                        btnConfirmar.innerHTML = '<i class="fas fa-check me-2"></i>Confirmar Compra';
                    }
                }
            }
        } catch (error) {
            console.error('❌ Error al realizar la compra:', error);
            alert('Error de conexión. Por favor, inténtalo de nuevo.');

            if (btnConfirmar) {
                btnConfirmar.disabled = false;
                btnConfirmar.innerHTML = '<i class="fas fa-check me-2"></i>Confirmar Compra';
            }
        }
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
                <h2 style="color: #1a237e; margin-bottom: 15px; font-size: 1.8rem;">
                    Saldo Insuficiente
                </h2>
                <p style="color: #666; font-size: 1.1rem; margin-bottom: 20px;">
                    No tienes suficiente saldo para comprar esta entrada.
                </p>
                <div style="
                    background: #f5f5f5;
                    padding: 20px;
                    border-radius: 12px;
                    margin: 20px 0;
                ">
                    <div style="display: flex; justify-content: space-between; margin: 10px 0;">
                        <span style="color: #666; font-weight: 600;">Precio de la entrada:</span>
                        <strong style="color: #1a237e; font-size: 1.2rem;">${precio.toFixed(2)}€</strong>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin: 10px 0;">
                        <span style="color: #666; font-weight: 600;">Tu saldo actual:</span>
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
                    transition: all 0.3s ease;
                " onmouseover="this.style.transform='translateY(-2px)'" onmouseout="this.style.transform='translateY(0)'">
                    <i class="fas fa-plus-circle" style="margin-right: 8px;"></i>
                    Recargar Saldo
                </button>
                <button onclick="this.parentElement.parentElement.remove()" style="
                    background: #f5f5f5;
                    color: #666;
                    border: none;
                    padding: 15px 30px;
                    border-radius: 50px;
                    font-weight: 700;
                    font-size: 1.1rem;
                    cursor: pointer;
                    margin: 10px;
                    transition: all 0.3s ease;
                " onmouseover="this.style.background='#e0e0e0'" onmouseout="this.style.background='#f5f5f5'">
                    Cerrar
                </button>
            </div>
        `;

        document.body.appendChild(modal);
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.remove();
            }
        });
    }
}

// Variables globales
let estadioInstance = null;

// Función para inicializar
function initEstadio3D(eventoId) {
    console.log('🎮 Iniciando estadio con Canvas 2D para evento:', eventoId);

    try {
        estadioInstance = new EstadioCanvas('estadio-canvas', eventoId);
        window.estadioInstance = estadioInstance; // También asignarlo a window
        console.log('✅ Instancia de EstadioCanvas creada');
    } catch (error) {
        console.error('❌ ERROR FATAL al crear EstadioCanvas:', error);
        console.error('Stack:', error.stack);

        // Mostrar error en la pantalla de carga
        const loading = document.getElementById('loading');
        if (loading) {
            loading.innerHTML = `
                <div style="color: white; text-align: center; padding: 40px;">
                    <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #FFC107;"></i>
                    <h3>Error al Inicializar el Estadio</h3>
                    <p style="color: #ffcdd2;">${error.message}</p>
                    <details style="margin: 20px; text-align: left; background: rgba(0,0,0,0.3); padding: 15px; border-radius: 8px;">
                        <summary style="cursor: pointer; color: #ffd700;">Ver detalles técnicos</summary>
                        <pre style="color: #fff; font-size: 12px; overflow: auto;">${error.stack}</pre>
                    </details>
                    <button onclick="location.reload()" style="
                        background: #ffd700;
                        color: #1a237e;
                        border: none;
                        padding: 12px 24px;
                        border-radius: 8px;
                        font-weight: 700;
                        cursor: pointer;
                        margin: 10px;
                    ">Reintentar</button>
                    <br><br>
                    <a href="/eventos" style="color: #ffd700; text-decoration: underline;">Volver a Eventos</a>
                </div>
            `;
        }
    }
}

// Funciones globales
function resetearVista() {
    if (estadioInstance) {
        estadioInstance.resetearVista();
    }
}

function ajustarZoom(delta) {
    if (estadioInstance) {
        estadioInstance.ajustarZoom(delta);
    }
}

function confirmarCompra() {
    if (estadioInstance) {
        estadioInstance.confirmarCompra();
    }
}

function limpiarSeleccion() {
    if (estadioInstance) {
        estadioInstance.limpiarSeleccion();
    }
}

// Exponer funciones
window.initEstadio3D = initEstadio3D;
window.resetearVista = resetearVista;
window.ajustarZoom = ajustarZoom;
window.confirmarCompra = confirmarCompra;
window.limpiarSeleccion = limpiarSeleccion;
window.estadioInstance = null; // Hacer la instancia accesible