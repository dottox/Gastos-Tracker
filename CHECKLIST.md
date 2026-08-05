# CHECKLIST.md - Plan de Acción "Mis Gastos"

## Fase 1: Configuración Inicial
- [ ] Configurar el archivo `build.gradle.kts` (app y project) con las versiones correctas.
- [ ] Agregar dependencias de Jetpack Compose (Material 3, Foundation, UI Tooling).
- [ ] Agregar dependencias de Room Database y KSP (Kotlin Symbol Processing).
- [ ] Agregar dependencias de ViewModel, Lifecycle y Coroutines.
- [ ] Configurar el `MaterialTheme` básico (Colores, Tipografía y Formas).

## Fase 2: Capa de Datos (Room)
- [ ] Crear la entidad `Transaction` (id, titulo, monto, categoria, fecha, tipo).
- [ ] Crear el DAO `TransactionDao` con consultas para:
    - [ ] Obtener todas las transacciones de un mes específico.
    - [ ] Sumar ingresos de un mes.
    - [ ] Sumar gastos de un mes.
    - [ ] Sumar ahorros históricos (Ingresos totales - Gastos totales).
    - [ ] Insertar, actualizar y eliminar una transacción.
- [ ] Configurar `AppDatabase` (RoomDatabase).
- [ ] Crear la clase `TransactionRepository` para abstraer el DAO.

## Fase 3: Lógica de Negocio (ViewModels)
- [ ] Crear `MainViewModel`.
- [ ] Implementar `StateFlow` para observar las transacciones por mes.
- [ ] Implementar la lógica para cambiar el mes seleccionado (Navegación superior).
- [ ] Implementar funciones para manejar eventos (Agregar, Editar, Borrar transacciones).
- [ ] Calcular y exponer los datos para la pantalla de Status (Ingresos, Gastos, Ahorro mes, Ahorro lifetime, Ahorro objetivo).

## Fase 4: Componentes de UI Reutilizables
- [ ] Crear componente `TransactionCard` (Muestra el item individual con nombre, monto, categoría y fecha).
- [ ] Crear componente `MonthSelector` (Flechas laterales y texto del mes actual).
- [ ] Crear componente `TransactionForm` (Formulario para agregar/editar nombre, monto y categoría).
- [ ] Crear componente `ConfirmDeleteDialog` (Popup de confirmación para eliminar).

## Fase 5: Estructura Principal
- [ ] Configurar la `MainActivity`.
- [ ] Implementar `HorizontalPager` para las 3 páginas.
- [ ] Crear un indicador visual (TabRow o PagerIndicator personalizado) para saber en qué pantalla se encuentra el usuario.

## Fase 6: Pantalla de Gastos e Ingresos
- [ ] Implementar `GastosScreen`:
    - [ ] Posicionar el `MonthSelector` en la parte superior.
    - [ ] Implementar `LazyColumn` en el centro para renderizar las `TransactionCard`.
    - [ ] Agregar `FloatingActionButton` (FAB) en la parte inferior derecha.
    - [ ] Conectar el FAB para abrir el `TransactionForm` (como ModalBottomSheet o Dialog).
    - [ ] Implementar menú contextual (o swipe-to-dismiss/long press) en `TransactionCard` para Editar/Borrar.
- [ ] Implementar `IngresosScreen` (Reutilizar la lógica y estructura de `GastosScreen` filtrando por tipo de transacción).

## Fase 7: Pantalla de Status
- [ ] Crear Card resumen del mes actual (Total Ingresos vs Total Gastos).
- [ ] Crear Card de progreso de Ahorro del Mes (Barra de progreso hacia el Ahorro Objetivo).
- [ ] Crear Card de Ahorro Histórico (Lifetime).

## Fase 8: Refinamiento de UI/UX y QA
- [ ] Aplicar colores distintivos (ej. verde para ingresos, rojo para gastos) usando Material 3.
- [ ] Agregar `HapticFeedback` a los botones flotantes y al momento de confirmar un guardado o eliminación.
- [ ] Comprobar que los teclados mostrados sean los correctos (Numérico para monto, Texto para nombre).
- [ ] Probar la aplicación sin conexión a internet (Modo Avión) para asegurar 100% de funcionalidad offline.
- [ ] Ajustar la UI para evitar superposiciones con la barra de navegación del sistema y el notch de la pantalla.