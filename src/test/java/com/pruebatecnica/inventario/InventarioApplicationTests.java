package com.pruebatecnica.inventario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pruebatecnica.inventario.repository.CategoriaRepository;
import com.pruebatecnica.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:inventario;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    }
)
@AutoConfigureMockMvc
class InventarioApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ProductoRepository productos;

    @Autowired
    CategoriaRepository categorias;

    @BeforeEach
    void preparar() {
        jdbc.update("delete from productos");
        jdbc.update("delete from categorias");
        jdbc.update("insert into categorias(id,nombre) values(10,'Tecnología'),(20,'Vacía')");
        jdbc.update(
            "insert into productos(categoria_id,nombre,precio,stock,fecha_creacion) values(10,'Mouse',85.50,5,CURRENT_TIMESTAMP),(10,'Teclado',125,6,CURRENT_TIMESTAMP),(10,'Cable',10,0,CURRENT_TIMESTAMP)"
        );
    }

    @Test
    void listadoRenderizaCategoriasYLimitesDeStock() throws Exception {
        mvc.perform(get("/productos"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Tecnología")))
            .andExpect(content().string(containsString("Bajo Stock")));
        var lista = productos.buscar("", null);
        assertThat(
            lista
                .stream()
                .filter(p -> p.isBajoStock())
                .count()
        ).isEqualTo(2);
    }

    @Test
    void filtraPorNombreYCategoria() throws Exception {
        mvc.perform(get("/productos").param("nombre", "Mouse").param("categoriaId", "10"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Mouse")))
            .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Teclado"))));
        assertThat(productos.buscar("!%", null)).isEmpty();
    }

    @Test
    void creaProductoYRedirige() throws Exception {
        mvc.perform(
            post("/productos")
                .param("nombre", "  Monitor  ")
                .param("categoriaId", "10")
                .param("precio", "199.90")
                .param("stock", "0")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/productos"));
        assertThat(productos.buscar("Monitor", null)).hasSize(1);
        assertThat(productos.buscar("Monitor", null).getFirst().getNombre()).isEqualTo("Monitor");
    }

    @Test
    void rechazaDatosInvalidosSinGuardar() throws Exception {
        mvc.perform(
            post("/productos")
                .param("nombre", "   ")
                .param("categoriaId", "10")
                .param("precio", "0")
                .param("stock", "-1")
        )
            .andExpect(status().isOk())
            .andExpect(
                model().attributeHasFieldErrors("productoForm", "nombre", "precio", "stock")
            );
        assertThat(productos.count()).isEqualTo(3);
    }

    @Test
    void rechazaCategoriaInexistenteYStockFraccionario() throws Exception {
        mvc.perform(
            post("/productos")
                .param("nombre", "Prueba")
                .param("categoriaId", "999")
                .param("precio", "10")
                .param("stock", "1")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("productoForm", "categoriaId"));
        mvc.perform(
            post("/productos")
                .param("nombre", "Prueba")
                .param("categoriaId", "10")
                .param("precio", "10")
                .param("stock", "1.5")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("productoForm", "stock"));
        assertThat(productos.count()).isEqualTo(3);
    }

    @Test
    void protegeCategoriaConProductosYPermiteEliminarVacia() throws Exception {
        mvc.perform(post("/categorias/10/eliminar"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("error"));
        assertThat(categorias.existsById(10L)).isTrue();
        mvc.perform(post("/categorias/20/eliminar"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("exito"));
        assertThat(categorias.existsById(20L)).isFalse();
    }

    @Test
    void renderizaFormularioYCategorias() throws Exception {
        mvc.perform(get("/productos/nuevo"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Guardar producto")));
        mvc.perform(get("/categorias"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Confirmar eliminación")));
    }
}
