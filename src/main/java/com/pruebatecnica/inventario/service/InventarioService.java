package com.pruebatecnica.inventario.service;

import com.pruebatecnica.inventario.model.Categoria;
import com.pruebatecnica.inventario.model.Producto;
import com.pruebatecnica.inventario.repository.CategoriaRepository;
import com.pruebatecnica.inventario.repository.ProductoRepository;
import com.pruebatecnica.inventario.web.ProductoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class InventarioService {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public InventarioService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    @Transactional
    public void crearProducto(@Valid ProductoForm form) {
        Categoria categoria = categorias
            .findById(form.getCategoriaId())
            .orElseThrow(() ->
                new IllegalArgumentException("La categoría seleccionada ya no existe.")
            );
        Producto producto = new Producto();
        producto.setNombre(form.getNombre());
        producto.setCategoria(categoria);
        producto.setPrecio(form.getPrecio());
        producto.setStock(form.getStock());
        productos.saveAndFlush(producto);
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = categorias
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("La categoría ya no existe."));
        if (productos.existsByCategoriaId(id)) {
            throw new IllegalArgumentException(
                "No se puede eliminar una categoría que tiene productos asociados."
            );
        }
        // La FK protege también ante inserciones concurrentes.
        categorias.delete(categoria);
        categorias.flush();
    }
}
