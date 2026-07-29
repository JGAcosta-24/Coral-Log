# **Especificación de Requisitos de Software**

# **Jesús Acosta**

# 

# **Versión 1.3**

# **Histórico de Revisiones**

| Fecha | Versión | Descripción | Autor(es) |
| ----- | ----- | ----- | ----- |
| *03/jul/26* | *1.0* | *Contenido de los sprints y requerimientos* | *Jesús Acosta* |
| *06/jul/26* | *1.1* | *Cambio de los identificadores de las HU (los de la épica 2 pasaron a ser de la 1 y viceversa)* | *Jesús Acosta* |
| *25/jul/26* | *1.2* | *Cambio de los identificadores de las HU en el título del sprint.* | *Jesús Acosta* |
| *27/jul/26* | *1.3* | *Inclusión de los coágulos como variable de 3 niveles, ahora los cólicos y el flujo tienen 5 niveles. Se añadió criterio re reinicio de ciclo a la HU-03* | *Jesús Acosta* |

# **Tabla de Contenidos**

[**1\. Sprint	4**](#sprint)

[1.1 Sprint 1	4](#sprint-1)

[1.1.1 HU-01 Calendario mensual interactivo :	4](#hu-01-calendario-mensual-interactivo-:)

[1.1.2 HU-02 Registro de sangrado, flujo y cólicos :	5](#hu-02-registro-de-sangrado,-flujo-y-cólicos-:)

[1.1.3 HU-03 Identificación visual de fases en el calendario :	5](#hu-03-identificación-visual-de-fases-en-el-calendario-:)

[1.2 Sprint 2	6](#sprint-2)

[1.2.1 HU-04 Días faltantes para el próximo periodo :	6](#hu-04-días-faltantes-para-el-próximo-periodo-:)

[1.2.2 HU-05: Visualización de la fase actual	7](#hu-05:-visualización-de-la-fase-actual)

[1.2.3 HU-06: Descripción de síntomas de la fase	7](#hu-06:-descripción-de-síntomas-de-la-fase)

[1.3 Sprint 3	8](#sprint-3)

[1.3.1 HU-07: Promedio de duración del ciclo:	8](#hu-07:-promedio-de-duración-del-ciclo:)

[1.3.2 HU-08: Nivel de flujo promedio	9](#hu-08:-nivel-de-flujo-promedio)

[1.3.3 HU-09: Nivel de intensidad de cólicos promedio	9](#hu-09:-nivel-de-intensidad-de-cólicos-promedio)

[1.3.4 HU-10: Mensaje por datos insuficientes	9](#hu-10:-mensaje-por-datos-insuficientes)

[1.4 Sprint 4	10](#sprint-4)

[1.4.1 HU-11: Selección de temas de colores predefinidos	10](#hu-11:-selección-de-temas-de-colores-predefinidos)

[1.4.2 HU-12: Selección de fuente tipográfica	11](#hu-12:-selección-de-fuente-tipográfica)

[**2\. Requisitos suplementarios	11**](#requisitos-suplementarios)

[2.1 Usabilidad	11](#2.1-usabilidad)

[2.2 Compatibilidad	11](#2.2-compatibilidad)

[2.3 Fiabilidad	11](#2.3-fiabilidad)

[2.4 Eficiencia de Desempeño	12](#2.4-eficiencia-de-desempeño)

[2.5 Seguridad	12](#2.5-seguridad)

[2.6 Mantenibilidad	12](#2.6-mantenibilidad)

# 	

# **Especificación de Requisitos de Software**

# **Coral Log**

Este artefacto detalla los requisitos de software para el Sistema \<Nombre del Sistema\>, según dos grandes aspectos claves para su desarrollo: Las Historias de Usuario en cada Sprint con su especificación según el modelo Canvas, y las Especificaciones Suplementarias. Toda esta información establece los lineamientos y las restricciones que debe considerar el equipo de desarrollo del proyecto para el desarrollo del sistema.

1. # **Sprint** {#sprint}

   1. ## **Sprint 1** {#sprint-1}

*Historias del Calendario y Registro (HU-01, 02, 03).* 

1. ### *HU-01 Calendario mensual interactivo :* {#hu-01-calendario-mensual-interactivo-:}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero ver un calendario mensual interactivo para tener una perspectiva global de mi mes.*  | ***Desarrollador:** ¿El calendario tendrá desplazamiento infinito hacia abajo o se navegará mes a mes deslizando horizontalmente? **Product Owner:** Será paginado mes a mes (desplazamiento horizontal), de este modo recargamos la vista de Jetpack Compose de forma más eficiente. **Desarrollador:** ¿Qué día será tomado como el inicio de la semana? **Product Owner:** Dependerá de la región (Locale) del dispositivo, pero de manera predeterminada el calendario debe posicionar el Lunes en la primera columna.* | ***Dado que** la usuaria navega hacia la vista de historial, **cuando** cargue la pantalla, **entonces** debe observar el mes actual renderizado en una cuadrícula de 7 columnas que incluye todos los días correspondientes a ese mes. **Dado que** la usuaria desliza el dedo hacia la derecha o izquierda sobre la cuadrícula, **cuando** ocurra el gesto, **entonces** el calendario debe transicionar velozmente (en menos de 1 segundo) al mes anterior o siguiente.* |

### 

2. ### *HU-02 Registro de sangrado, flujo y cólicos :* {#hu-02-registro-de-sangrado,-flujo-y-cólicos-:}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero poder seleccionar un día en el calendario y registrar si hubo sangrado, el nivel de flujo y la intensidad de los cólicos, para llevar un historial preciso.*  | ***Desarrollador:** ¿Cuántas opciones de intensidad manejamos en flujo y dolor para no saturar la UI? **Product Owner:** Solo tres opciones categóricas: Leve, Moderado y Fuerte. **Desarrollador:** ¿Qué ocurre si la usuaria se equivocó de día y quiere borrar el registro? **Product Owner:** El formulario debe tener un botón para "Limpiar datos" o permitir deseleccionar todo, regresando ese día a un estado neutro en la base de datos local.* | ***Dado que** el calendario está en pantalla, **cuando** la usuaria toque el cuadro numérico de cualquier día, **entonces** se desplegará una ventana (Bottom Sheet de Compose) mostrando los selectores de flujo y cólicos. **Dado que** la usuaria ajusta las opciones de registro en el cuadro de diálogo, **cuando** cierre la ventana o guarde los datos, **entonces** la aplicación debe persistir esos datos de manera inmediata en la base de datos local (Room) bajo esa fecha específica. **Dado que** un día tiene datos guardados, **cuando** la usuaria vacíe todas las opciones de dicho día, **entonces** la base de datos debe eliminar los síntomas asociados y la interfaz debe remover el sangrado de esa fecha.* |

   3. ### *HU-03 Identificación visual de fases en el calendario :* {#hu-03-identificación-visual-de-fases-en-el-calendario-:}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero que los días del calendario tengan colores distintos dependiendo de la fase del ciclo en la que caen, para identificar rápidamente mis ventanas fértiles o de menstruación.*  | ***Desarrollador:** ¿Los colores rellenarán toda la celda del calendario o se usarán pequeños puntos indicadores? **Product Owner:** Se rellenará todo el fondo del cuadro numérico para destacar ventanas completas (ej. cinco días contiguos de ventana fértil en color suave). **Desarrollador:** ¿Cómo garantizamos la accesibilidad visual al cambiar entre tema claro y oscuro en el teléfono? **Product Owner:** Se usará una paleta de colores de baja opacidad adaptativa en* colors.xml *(o en la definición del tema de Jetpack Compose) que contraste correctamente con la fuente tipográfica blanca o negra en ambos modos.*  | ***Dado que** la vista del mes procesa los días renderizados contra el historial de ciclos guardado, **cuando** el calendario se dibuje en pantalla, **entonces** las celdas de días de menstruación y de ventana fértil deben colorearse con sus paletas de colores correspondientes. Interpreta los días de la siguiente manera: Si la usuaria registra sangrado y han pasado menos de 21 días desde el inicio de su última regla, el sistema lo ignora para el cálculo de las fases (lo asume como manchado). Si han pasado 21 días o más, el sistema asume que es una regla adelantada o puntual y reinicia el ciclo. **Dado que** el sistema operativo del teléfono cambie a "Modo Oscuro", **cuando** la aplicación se repinte, **entonces** el fondo de los colores de las fases en el calendario debe adaptarse conservando legibilidad sin causar molestias visuales por colores saturados.* |

   2. ## **Sprint 2** {#sprint-2}

*Historias de la Pantalla Principal (HU-04, 05, 06).* 

1. ### *HU-04 Días faltantes para el próximo periodo :* {#hu-04-días-faltantes-para-el-próximo-periodo-:}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero ver en la pantalla principal cuántos días faltan para mi próximo periodo para poder planificar mis actividades de la semana.*  | ***Desarrollador:** ¿Qué mostramos si la usuaria acaba de instalar la app y no tiene historial? **Product Owner:** Durante el onboarding, se le pedirá la fecha de su último periodo y la duración promedio de su ciclo. Usaremos esos datos para el cálculo temporal. **Desarrollador:** ¿Cómo debe reaccionar el texto si la usuaria tiene un retraso? **Product Owner:** Si llega el día previsto, debe decir "Tu periodo debería iniciar hoy". Si la fecha se supera sin registro de sangrado, el texto debe cambiar a advertencia (ej. "Tienes X días de retraso") en un color que destaque el estado de alerta.* | ***Dado que** la app tiene acceso a los datos del ciclo (históricos o de onboarding), **cuando** la usuaria ingrese a la pantalla principal, **entonces** debe visualizar el número exacto de días restantes para su próximo ciclo. **Dado que** la fecha actual coincide o supera la fecha predicha sin un registro de nuevo periodo, **cuando** la usuaria vea el panel principal, **entonces** el texto debe cambiar automáticamente a estado de aviso (hoy) o cálculo de días de retraso.* |

### 

2. ### *HU-05: Visualización de la fase actual*  {#hu-05:-visualización-de-la-fase-actual}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero visualizar en qué fase de mi ciclo me encuentro actualmente para entender el estado de mi cuerpo.*  | ***Desarrollador:** ¿Cuáles son las fases exactas que el sistema va a manejar? **Product Owner:** Serán cuatro: Menstrual, Folicular, Ovulación y Lútea. **Desarrollador:** Al no tener histórico al inicio, ¿cómo ajustamos las ventanas de cada fase? **Product Owner:** Al principio usaremos una fórmula médica estándar restando días hacia atrás (ej. la fase lútea tiene una duración promedio de 14 días previos a la menstruación).* | ***Dado que** el motor lógico de la app calcula las fechas del ciclo en base al calendario, **cuando** la usuaria revise su estado en el día actual, **entonces** debe poder leer de forma clara el título de la fase menstrual en la que se encuentra (Menstrual, Folicular, Ovulación o Lútea).*  |

   3. ### *HU-06: Descripción de síntomas de la fase*  {#hu-06:-descripción-de-síntomas-de-la-fase}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero leer una breve descripción de los síntomas y estados de ánimo típicos de mi fase actual para validar cómo me siento.*  | ***Desarrollador:** Si la app es offline, ¿dónde alojaremos los textos de los síntomas para que sean dinámicos y bilingües? **Product Owner:** Los textos estarán almacenados localmente (ya sea pre-poblados en Room Database o usando recursos de cadenas* strings.xml *de Android) garantizando que no haya que hacer peticiones a una API externa. *  | ***Dado que** la pantalla principal ha identificado la fase actual de la usuaria, **cuando** renderice la información complementaria, **entonces** se debe mostrar una lista de al menos tres síntomas físicos o emocionales comunes de dicha fase. **Dado que** la usuaria cambie el idioma principal de su dispositivo Android (de Español a Inglés), **cuando** abra Coral Log, **entonces** las descripciones de los síntomas deben mostrarse automáticamente en el idioma seleccionado.* |

## 

3. ## **Sprint 3** {#sprint-3}

*Historias del Análisis de Salud (HU-07, 08, 09, 10\)*

1. ### *HU-07: Promedio de duración del ciclo:* {#hu-07:-promedio-de-duración-del-ciclo:}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero ver la duración promedio de mi ciclo menstrual en una pantalla de métricas, para entender si mis ciclos tienen un patrón regular o irregular a lo largo del tiempo.*  | ***Desarrollador:** ¿Cómo calculamos el promedio si un mes el ciclo es anormalmente largo por estrés (ej. 45 días)? Eso dañaría el promedio matemático de los meses normales. **Product Owner:** Tienes razón. Debemos implementar una regla de exclusión de valores atípicos (outliers): los ciclos menores a 21 días o mayores a 40 días se ignorarán en la sumatoria, o en su defecto, utilizaremos la mediana estadística en lugar de la media pura. **Desarrollador:** ¿Y mostramos decimales? **Product Owner:** No, la vista debe mostrar el número de días redondeado al entero más cercano (ej. "28 días").* | ***Dado que** la usuaria accede a la pestaña de métricas y posee registros suficientes, **cuando** el motor lógico calcule la duración, **entonces** debe mostrar en pantalla un número entero que represente el promedio, ignorando de manera automática los ciclos registrados como atípicos (\<21 días o \>40 días).*  |

### 

2. ### *HU-08: Nivel de flujo promedio*  {#hu-08:-nivel-de-flujo-promedio}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero visualizar el nivel de flujo y coágulos promedio que he registrado en mis últimos meses, para conocer mi tendencia normal y estar mejor preparada.*  | ***Desarrollador:** El flujo se guarda en Room como una categoría (Mínimo, Leve, Moderado, Alto, Intenso). ¿Cómo sacamos un promedio de palabras? **Product Owner:** Asignaremos un valor numérico por debajo de la interfaz (Mínimo=1, Leve=2, Moderado=3, Alto=4, Intenso=5). Calcularemos la "Moda" (el valor que más se repite) en los últimos 6 ciclos, y devolveremos a la interfaz la palabra correspondiente.* | ***Dado que** la usuaria revisa su dashboard, **cuando** el sistema procese el histórico de flujo de los últimos 6 meses, **entonces** debe mostrar en pantalla el nivel categórico predominante basándose en la medida estadística de moda.*  |

   3. ### *HU-09: Nivel de intensidad de cólicos promedio*  {#hu-09:-nivel-de-intensidad-de-cólicos-promedio}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero observar el nivel de intensidad de cólicos promedio, para identificar si el dolor durante mi periodo está aumentando, disminuyendo o manteniéndose estable.*   | ***Desarrollador:** ¿El promedio de dolor evalúa todo el mes o solo los días específicos de menstruación? **Product Owner:** Para que el dato sea útil, la consulta a la base de datos (Query) debe filtrar y evaluar **únicamente** los niveles de dolor registrados en los días marcados con sangrado activo. *  | ***Dado que** la aplicación analiza los registros de dolor en Room Database, **cuando** se renderice la métrica en la interfaz, **entonces** el resultado debe calcularse excluyendo cualquier día del calendario que no esté marcado explícitamente como "día de sangrado".*  |

      4. ### *HU-10: Mensaje por datos insuficientes*  {#hu-10:-mensaje-por-datos-insuficientes}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero que la aplicación me muestre un mensaje informativo si no tengo suficientes ciclos registrados, para evitar confundirme con promedios matemáticamente inexactos o irreales.*  | ***Desarrollador:** ¿Ocultamos toda la pestaña de métricas si no hay datos? **Product Owner:** No, la pestaña debe ser accesible para que sepa que la función existe. Mostraremos un diseño de "Empty State" (estado vacío) con un texto amigable y un ícono indicando que necesitamos más tiempo. *  | ***Dado que** la usuaria entra al dashboard, **cuando** la base de datos retorne el histórico y detecte menos de 2 ciclos menstruales cerrados, **entonces** las tarjetas de métricas numéricas deben ocultarse y ser reemplazadas por un componente informativo que explique: "Necesitamos al menos 2 ciclos válidos (duración regular) registrados para mostrar estas métricas".*  |

   4. ## **Sprint 4** {#sprint-4}

*Historias de Apropiación Visual (HU-11, 12\)*

1. ### *HU-11: Selección de temas de colores predefinidos*  {#hu-11:-selección-de-temas-de-colores-predefinidos}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero poder elegir entre diferentes temas de colores predefinidos (ej. Tema Océano, Tema Coral) en una pantalla de configuración, para que la interfaz se adapte a mis gustos personales.*  | ***Desarrollador:** ¿Guardamos las preferencias de tema en Room Database? **Product Owner:** No hace falta. Como es una configuración simple clave-valor y no un dato relacional, usaremos **Jetpack DataStore**. **Desarrollador:** ¿Hay que reiniciar la app para aplicar el tema? **Product Owner:** No, implementaremos un estado reactivo (*StateFlow*) para que al tocar el tema, toda la paleta de colores de Jetpack Compose cambie instantáneamente en tiempo real.*  | ***Dado que** la usuaria se encuentra en la pantalla de personalización, **cuando** seleccione una de las paletas de colores predefinidas, **entonces** la interfaz gráfica debe actualizar sus variables de color al instante sin requerir el reinicio de la aplicación. **Dado que** la usuaria cierra forzosamente la aplicación, **cuando** vuelva a abrirla, **entonces** la interfaz debe leer DataStore y renderizar el último tema seleccionado.*  |

### 

2. ### *HU-12: Selección de fuente tipográfica*  {#hu-12:-selección-de-fuente-tipográfica}

| *Historia de usuario* | *Conversación* | *Criterios de aceptación* |
| :---- | :---- | :---- |
| *Como usuaria, quiero tener la opción de seleccionar entre distintas fuentes tipográficas para la aplicación, para hacer que mi experiencia de lectura sea más cómoda y personalizada.*  | ***Desarrollador:** ¿Dejamos que importe fuentes desde su teléfono? **Product Owner:** No, eso complica el manejo de archivos locales. Ofreceremos un catálogo cerrado de 3 a 4 tipografías pre-empaquetadas en la carpeta* res/font *del proyecto Android para asegurar que los márgenes y pesos visuales no se rompan.* | ***Dado que** la usuaria abre el menú de tipografías, **cuando** pulse sobre una de las opciones predefinidas (ej. Sans-Serif, Rounded, Serif), **entonces** la propiedad* FontFamily *de la tipografía base de Jetpack Compose debe actualizarse, aplicándose a todos los textos de la app de forma reactiva.*  |

2. # **Requisitos suplementarios** {#requisitos-suplementarios}

### **2.1 Usabilidad** {#2.1-usabilidad}

**2.1.1 Accesibilidad** La interfaz de la aplicación debe heredar y adaptarse de manera automática al tema visual establecido (Modo Claro o Modo Oscuro) en el sistema operativo Android del dispositivo, garantizando que las paletas cromáticas mantengan los contrastes adecuados sin requerir intervención del usuario.

**2.1.2 Operabilidad** El sistema debe proveer una experiencia completamente bilingüe (Español e Inglés) detectando el *Locale* del dispositivo móvil para cargar dinámicamente el idioma correcto de la interfaz de manera automática.

### **2.2 Compatibilidad** {#2.2-compatibilidad}

**2.2.1 Interoperabilidad** Al ser una herramienta con enfoque en la privacidad, la aplicación debe ser 100% autónoma (offline-first). No debe depender de interfaces de programación de aplicaciones (APIs) externas, bases de datos en la nube ni requerir conexión a internet para el funcionamiento pleno de ninguna de sus características.

### **2.3 Fiabilidad** {#2.3-fiabilidad}

**2.3.1 Tolerancia a fallos** El sistema debe gestionar de manera robusta los cambios de configuración del sistema operativo (como la rotación física de la pantalla o la minimización repentina), garantizando que no ocurra pérdida de datos temporales en pantalla ni cierres inesperados (Crashes/ANR).

**2.3.2 Madurez** Todas las operaciones de inserción, actualización o eliminación de registros en la base de datos local (Room) deben ser atómicas. Si ocurre una interrupción abrupta en el dispositivo, la base de datos no debe quedar en un estado corrupto.

### **2.4 Eficiencia de Desempeño** {#2.4-eficiencia-de-desempeño}

**2.4.1 Comportamiento temporal** Las transiciones de navegación entre pantallas y, en especial, el renderizado de los datos históricos de los ciclos en la vista de calendario deben ejecutarse fluidamente con un tiempo de respuesta menor a 1 segundo.

**2.4.2 Utilización de recursos** La aplicación debe minimizar el consumo de batería del dispositivo delegando los cálculos algorítmicos complejos y las lecturas a la base de datos a hilos de ejecución secundarios (mediante el uso de Corrutinas), manteniendo el hilo principal libre.

### **2.5 Seguridad** {#2.5-seguridad}

**2.5.1 Confidencialidad** Toda la información sensible de salud registrada por la usuaria debe persistir de manera exclusiva dentro del espacio de almacenamiento aislado de la aplicación (*App Sandbox*). Queda estrictamente prohibida la recolección, telemetría o transmisión de estos datos hacia servidores de terceros.

### **2.6 Mantenibilidad** {#2.6-mantenibilidad}

**2.6.1 Modularidad** El código base de la aplicación debe estructurarse estrictamente bajo el patrón arquitectónico Model-View-ViewModel (MVVM) en conjunto con Jetpack Compose, asegurando una separación total entre la lógica del cálculo del ciclo menstrual y la capa de presentación visual.

**2.6.2 Modificabilidad** La base de datos local debe implementarse con un sistema previsor de migraciones, permitiendo que en el futuro se puedan agregar las tablas necesarias para las analíticas de salud (Release 2\) sin riesgo de sobreescribir o borrar el historial menstrual ya registrado por la usuaria.

