-- ================================================
-- Script de actualización de capacidades del estadio
-- Versión: 3.0
-- Fecha: 15 de Diciembre 2025
-- ================================================
--
-- IMPORTANTE: Este script actualiza las capacidades de las zonas
-- del estadio a la nueva distribución optimizada.
--
-- ⚠️ ADVERTENCIA:
-- - Solo ejecuta este script si sabes lo que estás haciendo
-- - NO lo ejecutes en eventos que ya tengan entradas vendidas
-- - Haz un backup de tu base de datos antes de ejecutar
-- - Verifica las capacidades actuales antes de actualizar
-- ================================================
--
-- NUEVA ORGANIZACIÓN DEL ESTADIO:
-- - Gol Nord (arriba): 400 asientos (10 filas × 40 asientos)
-- - Gol Sud (abajo): 400 asientos (10 filas × 40 asientos)
-- - Grada Lateral: 960 asientos (8 filas × 60 asientos × 2 lados)
-- - Tribuna VIP: 480 asientos (4 filas × 60 asientos × 2 lados)
--   * La Tribuna está INCORPORADA en las gradas laterales
--   * Se ubica en la parte SUPERIOR de cada grada lateral
--   * Asientos VIP con colores dorados distintivos
-- - TOTAL: 2240 asientos
-- ================================================

-- Paso 1: Ver las capacidades actuales (para verificar)
SELECT
    z.id,
    z.nombre,
    z.capacidad_total AS capacidad_actual,
    z.entradas_vendidas,
    e.nombre AS evento
FROM zona z
JOIN evento e ON z.evento_id = e.id
ORDER BY e.fecha DESC, z.nombre;

-- ================================================
-- Paso 2: Actualizar capacidades a la nueva distribución
-- ================================================

-- Comentarios sobre la nueva distribución optimizada:
--
-- ZONA GOL NORD (arriba):
--   Antigua: 800 asientos → Nueva: 400 asientos (reducción 50%)
--   Distribución: 10 filas × 40 asientos
--
-- ZONA GOL SUD (abajo):
--   Antigua: 800 asientos → Nueva: 400 asientos (reducción 50%)
--   Distribución: 10 filas × 40 asientos
--
-- ZONA GRADA LATERAL (laterales - parte baja):
--   Antigua: 1500 asientos → Nueva: 960 asientos (reducción 36%)
--   Distribución: 8 filas × 60 asientos × 2 lados
--
-- ZONA TRIBUNA VIP (incorporada en gradas laterales - parte alta):
--   Antigua: 1000 asientos → Nueva: 480 asientos (reducción 52%)
--   Distribución: 4 filas × 60 asientos × 2 lados
--   Característica: Asientos VIP dorados ubicados en la parte superior
--                   de las gradas laterales
--
-- CAPACIDAD TOTAL:
--   Antigua: 4100 asientos → Nueva: 2240 asientos (reducción 45.4%)
--   Distribución: 400 + 400 + 960 + 480 = 2240 asientos

-- OPCIÓN A: Actualizar TODAS las zonas (usar con precaución)
-- Descomenta las siguientes líneas solo si estás seguro

/*
UPDATE zona SET capacidad_total = 400 WHERE nombre = 'Gol Nord';
UPDATE zona SET capacidad_total = 400 WHERE nombre = 'Gol Sud';
UPDATE zona SET capacidad_total = 960 WHERE nombre = 'Grada Lateral';
UPDATE zona SET capacidad_total = 480 WHERE nombre = 'Tribuna';
*/

-- OPCIÓN B: Actualizar solo zonas de eventos futuros sin ventas (RECOMENDADO)
-- Descomenta las siguientes líneas para usar esta opción

/*
UPDATE zona z
JOIN evento e ON z.evento_id = e.id
SET z.capacidad_total = 400
WHERE z.nombre = 'Gol Nord'
  AND e.fecha > NOW()
  AND (z.entradas_vendidas IS NULL OR z.entradas_vendidas = 0);

UPDATE zona z
JOIN evento e ON z.evento_id = e.id
SET z.capacidad_total = 400
WHERE z.nombre = 'Gol Sud'
  AND e.fecha > NOW()
  AND (z.entradas_vendidas IS NULL OR z.entradas_vendidas = 0);

UPDATE zona z
JOIN evento e ON z.evento_id = e.id
SET z.capacidad_total = 960
WHERE z.nombre = 'Grada Lateral'
  AND e.fecha > NOW()
  AND (z.entradas_vendidas IS NULL OR z.entradas_vendidas = 0);

UPDATE zona z
JOIN evento e ON z.evento_id = e.id
SET z.capacidad_total = 480
WHERE z.nombre = 'Tribuna'
  AND e.fecha > NOW()
  AND (z.entradas_vendidas IS NULL OR z.entradas_vendidas = 0);
*/

-- ================================================
-- Paso 3: Verificar las actualizaciones
-- ================================================

-- Ver las capacidades después de actualizar
SELECT
    z.id,
    z.nombre,
    z.capacidad_total AS nueva_capacidad,
    z.entradas_vendidas,
    e.nombre AS evento,
    e.fecha
FROM zona z
JOIN evento e ON z.evento_id = e.id
ORDER BY e.fecha DESC, z.nombre;

-- ================================================
-- Paso 4: Verificar integridad de datos
-- ================================================

-- Verificar que ninguna zona tenga más entradas vendidas que su capacidad
SELECT
    z.id,
    z.nombre,
    z.capacidad_total,
    z.entradas_vendidas,
    (z.entradas_vendidas - z.capacidad_total) AS exceso,
    e.nombre AS evento
FROM zona z
JOIN evento e ON z.evento_id = e.id
WHERE z.entradas_vendidas > z.capacidad_total
ORDER BY exceso DESC;

-- Si este query devuelve resultados, tienes un problema:
-- Hay más entradas vendidas que la capacidad actual
-- En ese caso, NO actualices esas zonas o ajusta la capacidad manualmente

-- ================================================
-- Resumen de capacidades totales por evento
-- ================================================

SELECT
    e.id,
    e.nombre AS evento,
    e.fecha,
    SUM(z.capacidad_total) AS capacidad_total_estadio,
    SUM(z.entradas_vendidas) AS total_vendidas,
    SUM(z.capacidad_total) - SUM(z.entradas_vendidas) AS disponibles
FROM evento e
JOIN zona z ON z.evento_id = e.id
GROUP BY e.id, e.nombre, e.fecha
ORDER BY e.fecha DESC;

-- Resultado esperado para nuevos eventos:
-- capacidad_total_estadio = 2240 (400 + 400 + 960 + 480)

-- ================================================
-- NOTAS IMPORTANTES:
-- ================================================
--
-- 1. Los nuevos eventos creados después de esta actualización
--    ya tendrán automáticamente las nuevas capacidades (2240 total)
--    gracias a los cambios en ZonaService.java
--
-- 2. Este script es solo para actualizar eventos EXISTENTES
--
-- 3. Si tienes entradas vendidas que exceden la nueva capacidad,
--    considera:
--    - Mantener la capacidad antigua para ese evento específico
--    - Contactar a los usuarios afectados
--    - Realizar reembolsos si es necesario
--
-- 4. Siempre haz un backup antes de ejecutar actualizaciones masivas
--
-- ================================================
-- FIN DEL SCRIPT
-- ================================================

