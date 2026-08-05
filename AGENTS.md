# AGENTS.md - Contexto y Directrices del Proyecto "Mis Gastos"

## Contexto del Proyecto
"Mis Gastos" es una aplicación de finanzas personales diseñada para Android, enfocada en la simplicidad, la estética minimalista y el funcionamiento 100% offline. Permite a los usuarios registrar, visualizar y gestionar sus ingresos y gastos a través de una interfaz moderna y colorida, distribuida en tres pantallas principales navegables mediante scroll horizontal (Status, Gastos, Ingresos).

**Paquete:** `com.example.misgastos`
**Lenguaje principal:** Kotlin
**UI Framework:** Jetpack Compose
**Configuración de Build:** Kotlin DSL
**SDK Mínimo:** Android 14 (API Level 34)

## Stack Tecnológico y Arquitectura
*   **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture básica. Esto separará la lógica de la base de datos, la lógica de negocio y la interfaz de usuario.
*   **Almacenamiento Offline:** Room Database. Es el estándar de la industria, optimizado para Android y funciona perfectamente con corrutinas y Flow.
*   **Navegación:** `HorizontalPager` (de la librería Foundation de Compose) para deslizar entre las tres pantallas principales de forma fluida.
*   **Reactividad:** Kotlin Coroutines y `StateFlow` para observar los cambios en la base de datos en tiempo real y actualizar la UI de Compose sin bloqueos.

## Mejores Prácticas a Implementar

### UX/UI (Material Design 3)
*   **Diseño Minimalista y Colorido:** Utiliza la paleta de colores de `MaterialTheme` (Primary, Secondary, Tertiary, Surface, etc.) para crear una interfaz limpia pero con acentos de color que diferencien ingresos (ej. tonos verdes/azules) de gastos (ej. tonos rojos/naranjas).
*   **Feedback Táctil y Visual:** Asegúrate de que todos los componentes clickeables tengan el efecto `ripple` de Compose por defecto. Utiliza `HapticFeedback` para acciones importantes (como guardar o borrar un registro).
*   **Consistencia:** Las pantallas de "Gastos" e "Ingresos" deben reutilizar los mismos componentes de UI (Listas, Cards, Dialogs) para mantener la coherencia visual y reducir el código duplicado.
*   **Gestión de Formularios:** Utiliza `ModalBottomSheet` o `AlertDialog` estilizados para la entrada de nuevos datos de forma no intrusiva.

### Almacenamiento (Room)
*   **Modelo de Datos Único:** En lugar de crear tablas separadas para Ingresos y Gastos, utiliza una única entidad llamada `Transaction` con un campo `Type` (Enum: INGRESO, GASTO). Esto facilitará los cálculos en la pantalla de Status.
*   **Índices:** Crea índices en la base de datos por fecha para que las consultas por mes (necesarias para la navegación superior) sean extremadamente rápidas.

## Errores Comunes a Evitar
1.  **Bloquear el Hilo Principal (Main Thread):** Todas las operaciones de base de datos (Room) deben hacerse usando corrutinas en el `Dispatchers.IO`.
2.  **Estado Global Mutado:** No manejes el estado directamente en las funciones Composable. Utiliza *State Hoisting* delegando el estado y los eventos al `ViewModel`.
3.  **Falta de Confirmaciones en Acciones Destructivas:** Borrar o editar datos financieros requiere siempre un paso de seguridad. El popup de confirmación (`AlertDialog`) es estrictamente necesario.
4.  **Sobrecargar el `HorizontalPager`:** Asegúrate de que las listas dentro de las páginas (Gastos/Ingresos) usen `LazyColumn` para reciclar las vistas y evitar que la app consuma demasiada memoria al tener historiales largos.
5.  **Hardcodear Strings y Colores:** Todos los textos deben ir en `strings.xml` (o en un objeto de constantes de UI) y todos los colores deben derivar de `MaterialTheme.colorScheme`.