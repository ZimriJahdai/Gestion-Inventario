CREATE DATABASE IF NOT EXISTS inventario
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE inventario;

CREATE TABLE categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE productos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    categoria_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_productos_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_productos_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
    CONSTRAINT chk_productos_precio CHECK (precio > 0),
    CONSTRAINT chk_productos_stock CHECK (stock >= 0)
);

INSERT INTO categorias (nombre) VALUES
    ('Electrónica'),
    ('Ropa'),
    ('Alimentos');

INSERT INTO productos (categoria_id, nombre, precio, stock) VALUES
    (1, 'Teclado USB', 125.00, 12),
    (1, 'Mouse inalámbrico', 85.50, 5),
    (2, 'Camiseta básica', 60.00, 8),
    (3, 'Café molido', 45.00, 0);