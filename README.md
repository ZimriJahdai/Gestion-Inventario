# Gestión de inventario

Aplicación web para registrar productos, consultar existencias y gestionar categorías. Desarrollada con Java, Spring Boot, Thymeleaf, MySQL y CSS propio.

## Requisitos

- JDK 21.
- MySQL Server 8.0.16 o superior.
- Internet para descargar las dependencias al iniciar por primera vez. No necesitas instalar Maven.

## Ejecutar el proyecto

1. Descarga o clona este repositorio y abre la carpeta del proyecto.
2. En MySQL Workbench, abre `database.sql` y ejecuta todo el script **una sola vez**. Crea la base `inventario`, las tablas y los datos de ejemplo.
3. Abre PowerShell en la carpeta que contiene `pom.xml` y ejecuta:

```powershell
# Ajusta esta ruta si instalaste el JDK en otra carpeta.
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$env:DB_USER = Read-Host "Usuario de MySQL (por ejemplo, root)"
$credencial = Get-Credential -UserName $env:DB_USER -Message "Ingresa tu contraseña de MySQL"
$env:DB_PASSWORD = $credencial.GetNetworkCredential().Password
Remove-Variable credencial

.\mvnw.cmd spring-boot:run
```

Escribe tu contraseña en la ventana que aparece. Usa las credenciales de tu servidor MySQL; el proyecto no incluye una contraseña predeterminada.

4. Abre [http://localhost:8080/productos](http://localhost:8080/productos).

La conexión apunta a `localhost:3306/inventario`. Si necesitas cambiarla, edita `src/main/resources/application.properties`. Para detener la aplicación, pulsa **Ctrl + C**. Al abrir otra terminal, vuelve a configurar las variables.

## Cómo usarlo

- **Productos:** consulta el listado y busca por nombre o filtra por categoría. Usa **Limpiar** para ver todos.
- **Nuevo producto:** completa nombre, categoría, precio mayor a cero y stock entero no negativo; pulsa **Guardar producto**.
- **Bajo Stock:** identifica los productos con 5 unidades o menos.
- **Categorías:** permite eliminar una categoría después de confirmar, siempre que no tenga productos asociados.

## Organización

`model` contiene las entidades; `repository`, las consultas; `service`, las reglas de negocio; y `web`, el controlador y el formulario. Las vistas están en `templates` y los estilos en `static/css`. Esta separación facilita mantener los datos, la lógica y la presentación de forma independiente.

## Pruebas

```powershell
.\mvnw.cmd test
```

Se ejecutan con una base temporal en memoria y no modifican los datos de MySQL.
