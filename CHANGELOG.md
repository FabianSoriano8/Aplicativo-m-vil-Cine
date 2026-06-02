
# Changelog

## [1.1.0] - 2026-04-27

### Creado
- Login y Register usando NavController
- Encabezado visual en pantalla de login
- Página de selección de perfil
- Página de inicio para perfil de reseñas

### Modificado
- Eliminada LoginActivity redundante

### Terminado
- Login
- Página de inicio Reseñas

### Actualización 04/05/2026
- Se creó y configuró el Firebase para este proyecto.
- Se agregó el fragment home.
- Se corrigieron aspectos visuales en interfaz de selección de usuarios.

### Actualización Reciente (20/05/2026)
- **Cambio de Perfil Dinámico**: Implementado botón en la pestaña de perfil para alternar entre "Espectador" y "Cinéfilo" en tiempo real.
- **Navegación Inteligente**: El botón "HOME" ahora redirige automáticamente a la Cartelera (Home) o a Reseñas (Reviews) según el rol activo del usuario.
- **Persistencia y Sincronización**: Los cambios de rol se sincronizan inmediatamente entre Firebase Firestore y el almacenamiento local (DataStore).
- **Correcciones de Estabilidad**: Se solucionaron cierres inesperados (crashes) al cerrar sesión y errores de referencias en los archivos de diseño.

### Actualización 24/05/2026
- Se agregó el panel de Administrador que sólo está disponible para los usuarios con el tipo de user "admin"
- Se agregó la funcionalidad de gestionar (agregar, editar y borrar) películas, reseñas y usuarios.
- 