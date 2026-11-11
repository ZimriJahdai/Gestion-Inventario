# Inventario de tienda

Aplicación web para consultar y registrar productos, filtrar por nombre y categoría y controlar existencias. Construida con Java 21, Spring Boot 4.1.2, Spring MVC, Thymeleaf, Spring Data JPA, MySQL y CSS propio. No requiere Node.js ni Maven instalado: incluye Maven Wrapper.

## Requisitos

- JDK 21 (no solamente JRE).
- MySQL Server 8.0.16 o posterior, con soporte de restricciones CHECK. El entorno del candidato utiliza MySQL 9.7.1.
- Acceso a Internet para descargar dependencias la primera vez.

## 1. Preparar la base de datos

En MySQL Workbench, abre `database.sql` y ejecuta el script completo una sola vez en una base nueva. Crea la base `inventario`, las dos tablas y 3 categorías con 4 productos iniciales. Si ya ejecutaste el script, no lo repitas: los CREATE TABLE y los INSERT no son idempotentes.

Alternativa desde una terminal con el cliente MySQL disponible:

```text
mysql -u root -p
source C:/ruta/al/proyecto/inventario/database.sql
```

La conexión utiliza `localhost:3306` y la base `inventario`. No hay contraseña predeterminada ni credenciales guardadas en el repositorio. Usa un usuario MySQL con permisos SELECT, INSERT y DELETE sobre esa base; para importar el script necesitas además permisos de creación. Para una instalación local de prueba puedes usar tu usuario existente.

## 2. Ejecutar en Windows / PowerShell

Desde la carpeta que contiene `pom.xml`:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:DB_USER = Read-Host "Usuario MySQL"
$credencial = Get-Credential -UserName $env:DB_USER -Message "Contraseña MySQL"
$env:DB_PASSWORD = $credencial.GetNetworkCredential().Password
Remove-Variable credencial
.\mvnw.cmd spring-boot:run
```

En Linux/macOS, define JAVA_HOME apuntando al JDK 21 y las variables DB_USER y DB_PASSWORD, y ejecuta `sh mvnw spring-boot:run`.

Abre http://localhost:8080/productos. Detén el servidor con Ctrl+C. Las variables de PowerShell solamente duran en esa terminal. Si el puerto o el nombre de la base son diferentes, ajusta `spring.datasource.url` en `src/main/resources/application.properties`.

Hibernate usa `ddl-auto=validate`: comprueba el esquema y no crea ni elimina tus tablas. Debes importar el SQL antes del primer arranque.

## Estructura

```text
src/main/java/com/pruebatecnica/inventario/
  model/        Entidades que representan categorias y productos.
  repository/   Consultas y acceso a datos mediante JPA.
  service/      Operaciones transaccionales y reglas de negocio.
  web/          Controlador MVC y objeto de formulario validado.
src/main/resources/
  templates/    Vistas HTML Thymeleaf.
  static/css/   Tema oscuro con Grid, Flexbox y adaptación móvil.
src/test/       Pruebas automáticas con H2 aislada de MySQL.
database.sql    Esquema y datos iniciales para MySQL.
```

Esta separación mantiene el controlador centrado en solicitudes y respuestas, el servicio en reglas y transacciones, y los repositorios en persistencia. El formulario usa un DTO para evitar que el cliente modifique campos como el ID o la fecha de creación. No se añaden capas de API ni una aplicación frontend independiente por el alcance de la prueba.

## Funcionalidades y reglas

- Listado con nombre, categoría, precio, stock y estado.
- Búsqueda parcial por nombre y filtro por categoría, combinables. `%` y `_` se buscan como caracteres literales.
- Alta de productos con categoría existente; nombre obligatorio de hasta 150 caracteres, precio positivo de hasta 8 enteros y 2 decimales, stock entero no negativo.
- Validación en navegador y servidor; las restricciones SQL refuerzan la integridad.
- Etiqueta **Bajo Stock** para stock <= 5, incluido cero.
- Eliminación de categorías con confirmación. Si tienen productos, se presenta un mensaje y se conserva la categoría. La FK RESTRICT también protege ante operaciones concurrentes.
- Creación de fecha en el servidor y precios con BigDecimal, sin cálculos de coma flotante.
- HTML escapado por Thymeleaf y consultas parametrizadas por JPA.

Los precios no llevan símbolo de moneda porque la prueba no define una moneda. No incluye autenticación ni pretende ser un despliegue público de producción; el alcance es una demostración local de inventario.

## Estilo del código

Java, HTML, CSS y XML utilizan indentación de cuatro espacios. `.editorconfig` mantiene UTF-8 y reglas comunes de edición. Los estilos se organizan por componentes y las adaptaciones móviles están al final de `styles.css`; el tema oscuro está integrado, sin conservar una segunda capa del diseño claro.

## Pruebas

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Las pruebas usan H2 en memoria, nunca tus tablas MySQL. Comprueban renderizado, búsqueda, creación, validaciones, límites de stock y eliminación de categorías. H2 no sustituye una comprobación de arranque contra MySQL real.

Comprobación manual con MySQL:

1. Importar el SQL e iniciar: se muestran 4 productos y 3 categorías.
2. Mouse (stock 5) y Café (stock 0) muestran Bajo Stock; Teclado (stock 12) no.
3. Buscar un nombre y combinarlo con una categoría; limpiar filtros.
4. Crear un producto válido y confirmar que persiste tras reiniciar.
5. Intentar nombre vacío, precio cero y stock negativo; comprobar errores.
6. Intentar eliminar Electrónica: se bloquea con mensaje y se conserva.
7. Para probar una categoría vacía, ejecutar en Workbench `INSERT INTO categorias (nombre) VALUES ('Temporal');` y eliminarla desde la interfaz.
8. Revisar a 375 px de ancho: tabla en formato de filas adaptables, formulario y botones accesibles.
