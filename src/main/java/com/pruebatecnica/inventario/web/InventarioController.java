package com.pruebatecnica.inventario.web;

import com.pruebatecnica.inventario.repository.CategoriaRepository;
import com.pruebatecnica.inventario.repository.ProductoRepository;
import com.pruebatecnica.inventario.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InventarioController {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final InventarioService servicio;

    public InventarioController(
        ProductoRepository productos,
        CategoriaRepository categorias,
        InventarioService servicio
    ) {
        this.productos = productos;
        this.categorias = categorias;
        this.servicio = servicio;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/productos";
    }

    @GetMapping("/productos")
    public String listar(
        @RequestParam(defaultValue = "") String nombre,
        @RequestParam(required = false) Long categoriaId,
        Model model
    ) {
        String filtro = nombre.strip();
        String literal = filtro.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        model.addAttribute("productos", productos.buscar(literal, categoriaId));
        model.addAttribute("categorias", categorias.findAllByOrderByNombreAsc());
        model.addAttribute("nombre", nombre);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("total", productos.count());
        model.addAttribute("bajoStock", productos.countByStockLessThanEqual(5));
        return "productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("productoForm", new ProductoForm());
        return formulario(model);
    }

    private String formulario(Model model) {
        model.addAttribute("categorias", categorias.findAllByOrderByNombreAsc());
        return "producto-form";
    }

    @PostMapping("/productos")
    public String guardar(
        @Valid @ModelAttribute ProductoForm productoForm,
        BindingResult errors,
        Model model,
        RedirectAttributes flash
    ) {
        if (errors.hasErrors()) {
            return formulario(model);
        }
        try {
            servicio.crearProducto(productoForm);
        } catch (IllegalArgumentException e) {
            errors.rejectValue("categoriaId", "categoria", e.getMessage());
            return formulario(model);
        } catch (DataIntegrityViolationException e) {
            errors.reject(
                "guardar",
                "No se pudo guardar el producto. Revisa sus datos y la categoría."
            );
            return formulario(model);
        }
        flash.addFlashAttribute("exito", "Producto registrado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/categorias")
    public String categorias(Model model) {
        model.addAttribute("categorias", categorias.findAllByOrderByNombreAsc());
        return "categorias";
    }

    @PostMapping("/categorias/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            servicio.eliminarCategoria(id);
            flash.addFlashAttribute("exito", "Categoría eliminada.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute(
                "error",
                "No se puede eliminar: la categoría tiene productos asociados."
            );
        }
        return "redirect:/categorias";
    }
}
