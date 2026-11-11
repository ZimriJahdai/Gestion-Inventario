package com.pruebatecnica.inventario.repository;

import com.pruebatecnica.inventario.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @EntityGraph(attributePaths = "categoria")
    @Query(
        """
        select p from Producto p
        where (:categoriaId is null or p.categoria.id = :categoriaId)
          and lower(p.nombre) like lower(concat('%', :nombre, '%')) escape '!'
        order by p.id desc
        """
    )
    List<Producto> buscar(@Param("nombre") String nombre, @Param("categoriaId") Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);
    long countByStockLessThanEqual(Integer stock);
}
