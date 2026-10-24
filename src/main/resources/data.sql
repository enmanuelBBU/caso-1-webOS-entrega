INSERT INTO clientes (rut, nombre, email, telefono, direccion, fecha_registro) VALUES
    ('12345678-5', 'María González', 'maria.gonzalez@correo.cl', '+56911111111', 'Av. Collao 1202, Concepción', CURRENT_TIMESTAMP),
    ('9876543-3', 'Juan Pérez', 'juan.perez@correo.cl', '+56922222222', 'Av. Brasil 1180, Chillán', CURRENT_TIMESTAMP),
    ('15555555-6', 'Comercial Andes Ltda.', 'compras@andes.cl', '+56412345678', 'O''Higgins 450, Concepción', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO productos (sku, nombre, descripcion, categoria, precio, stock_actual, stock_minimo, activo, fecha_creacion, fecha_actualizacion) VALUES
    ('TEC-001', 'Teclado mecánico', 'Teclado mecánico switch red, layout español', 'Periféricos', 45990, 25, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MOU-002', 'Mouse inalámbrico', 'Mouse óptico 1600 DPI', 'Periféricos', 15990, 40, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MON-003', 'Monitor 27"', 'Monitor IPS 27 pulgadas 2K', 'Monitores', 219990, 4, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('AUD-004', 'Audífonos bluetooth', 'Audífonos over-ear con cancelación de ruido', 'Audio', 79990, 12, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('CAB-005', 'Cable HDMI 2m', 'Cable HDMI 2.1 de 2 metros', 'Accesorios', 6990, 2, 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SSD-006', 'SSD 1TB NVMe', 'Unidad de estado sólido M.2 NVMe 1TB', 'Almacenamiento', 69990, 18, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
