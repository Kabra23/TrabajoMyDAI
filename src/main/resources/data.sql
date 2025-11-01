INSERT INTO USUARIO (id_usuario, nombre, email) VALUES (1, 'Luis', 'luis@example.com');
INSERT INTO EVENTO (id_evento, nombre_evento) VALUES (1, 'Seminario');
INSERT INTO RECORDATORIO (id_recordatorio, usuario_id_usuario, evento_id_evento, mensaje, fecha) VALUES (1, 1, 1, 'Recordar inscripción', '2025-11-01');
INSERT INTO TICKET (id_ticket, usuario_id_usuario, evento_id_evento, codigo) VALUES (1, 1, 1, 'ABC123');
