package com.pruebatecnica.inventario.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductoForm {

    @NotBlank(message = "Escribe el nombre del producto.")
    @Size(max = 150, message = "El nombre admite hasta 150 caracteres.")
    private String nombre;

    @NotNull(message = "Selecciona una categoría.")
    @Positive(message = "Selecciona una categoría válida.")
    private Long categoriaId;

    @NotNull(message = "Escribe el precio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero.")
    @Digits(integer = 8, fraction = 2, message = "Usa hasta 8 enteros y 2 decimales.")
    private BigDecimal precio;

    @NotNull(message = "Escribe el stock.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.strip();
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
