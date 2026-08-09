# CHECKLIST.md - Plan de Acción "Mis Gastos"

## Fase 1: Configuración Inicial
- [x] Configurar el archivo `build.gradle.kts` (app y project) con las versiones correctas.
- [x] Agregar dependencias de Jetpack Compose (Material 3, Foundation, UI Tooling).
- [x] Agregar dependencias de Room Database y KSP (Kotlin Symbol Processing).
- [x] Agregar dependencias de ViewModel, Lifecycle y Coroutines.
- [x] Configurar el `MaterialTheme` básico (Colores, Tipografía y Formas).

## Fase 2: Capa de Datos (Room)
- [x] Crear la entidad `Transaction` (id, titulo, monto, categoria, fecha, tipo).
- [x] Crear el DAO `TransactionDao` con consultas para:
    - [x] Obtener todas las transacciones de un mes específico.
    - [x] Sumar ingresos de un mes.
    - [x] Sumar gastos de un mes.
    - [x] Sumar ahorros históricos (Ingresos totales - Gastos totales).
    - [x] Insertar, actualizar y eliminar una transacción.
- [x] Configurar `AppDatabase` (RoomDatabase).
- [x] Crear la clase `TransactionRepository` para abstraer el DAO.

## Fase 3: Lógica de Negocio (ViewModels)
- [x] Crear `MainViewModel`.
- [x] Implementar `StateFlow` para observar las transacciones por mes.
- [x] Implementar la lógica para cambiar el mes seleccionado (Navegación superior).
- [x] Implementar funciones para manejar eventos (Agregar, Editar, Borrar transacciones).
- [x] Calcular y exponer los datos para la pantalla de Status (Ingresos, Gastos, Ahorro mes, Ahorro lifetime, Ahorro objetivo).

## Fase 4: Componentes de UI Reutilizables
- [x] Crear componente `TransactionCard` (Muestra el item individual con nombre, monto, categoría y fecha).
- [x] Crear componente `MonthSelector` (Flechas laterales y texto del mes actual).
- [x] Crear componente `TransactionForm` (Formulario para agregar/editar nombre, monto y categoría).
- [x] Crear componente `ConfirmDeleteDialog` (Popup de confirmación para eliminar).

## Fase 5: Estructura Principal
- [x] Configurar la `MainActivity`.
- [x] Implementar `HorizontalPager` para las 3 páginas.
- [x] Crear un indicador visual (TabRow o PagerIndicator personalizado) para saber en qué pantalla se encuentra el usuario.

## Fase 6: Pantalla de Gastos e Ingresos
- [x] Implementar `GastosScreen`:
    - [x] Posicionar el `MonthSelector` en la parte superior.
    - [x] Implementar `LazyColumn` en el centro para renderizar las `TransactionCard`.
    - [x] Agregar `FloatingActionButton` (FAB) en la parte inferior derecha.
    - [x] Conectar el FAB para abrir el `TransactionForm` (como ModalBottomSheet o Dialog).
    - [x] Implementar menú contextual (o swipe-to-dismiss/long press) en `TransactionCard` para Editar/Borrar.
- [x] Implementar `IngresosScreen` (Reutilizar la lógica y estructura de `GastosScreen` filtrando por tipo de transacción).

## Fase 7: Pantalla de Status
- [x] Crear Card resumen del mes actual (Total Ingresos vs Total Gastos).
- [x] Crear Card de progreso de Ahorro del Mes (Barra de progreso hacia el Ahorro Objetivo).
- [x] Crear Card de Ahorro Histórico (Lifetime).

## Fase 8: Refinamiento de UI/UX y QA
- [x] Aplicar colores distintivos (ej. verde para ingresos, rojo para gastos) usando Material 3.
- [x] Agregar `HapticFeedback` a los botones flotantes y al momento de confirmar un guardado o eliminación.
- [x] Comprobar que los teclados mostrados sean los correctos (Numérico para monto, Texto para nombre).
- [ ] Probar la aplicación sin conexión a internet (Modo Avión) para asegurar 100% de funcionalidad offline. Pendiente de validación manual en un dispositivo; el manifest no declara permiso de Internet.
- [x] Ajustar la UI para evitar superposiciones con la barra de navegación del sistema y el notch de la pantalla.

## Fase 9: Ajustes y Categorias
- [x] Persistir cantidad de decimales, objetivo mensual y modo de tema en Room.
- [x] Agregar pantalla de ajustes accesible desde la pantalla principal.
- [x] Permitir agregar y quitar categorias de gastos e ingresos por separado.
- [x] Mostrar las categorias configuradas en un dropdown editable al crear o editar transacciones.
- [x] Guardar automaticamente una categoria nueva escrita desde el formulario.
- [x] Agregar migracion de Room y pruebas para la nueva configuracion.
