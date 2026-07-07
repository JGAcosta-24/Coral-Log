## Especificación de Requisitos de Software Jesús Acosta

Versión 1.1


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## Histórico de Revisiones

| Fecha | Versión | Descripción | Autor(es) |
| --- | --- | --- | --- |
| 03/jul/26 | 1.0 | Contenido de los sprints y requerimientos | Jesús Acosta |
| 06/jul/26 | 1.1 | Cambio de los identificadores de las HU (los de la | Jesús Acosta |
|   |   | épica 2 pasaron a ser de la 1 y viceversa) |   |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## Tabla de Contenidos


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## Especificación de Requisitos de Software Coral Log

Este artefacto detalla los requisitos de software para el Sistema <Nombre del Sistema>, según dos grandes aspectos claves para su desarrollo: Las Historias de Usuario en cada Sprint con su especificación según el modelo Canvas, y las Especificaciones Suplementarias. Toda esta información establece los lineamientos y las restricciones que debe considerar el equipo de desarrollo del proyecto para el desarrollo del sistema.

## 1. Sprint

## 1.1 Sprint 1

Historias del Calendario y Registro (HU-04, 05, 06).

- 1.1.1 HU-01 Calendario mensual interactivo :

| Historia de usuario Como usuaria, quiero ver un calendario mensual interactivo para tener una perspectiva global de mi mes. |   | Conversación Desarrollador: ¿El calendario tendrá desplazamiento infinito hacia abajo o se navegará mes a mes deslizando horizontalmente? Product Owner: Será paginado mes a mes (desplazamiento horizontal), de este modo recargamos la vista de Jetpack Compose de forma más eficiente. |   | Criterios de aceptación Dado que la usuaria navega hacia la vista de historial, cuando cargue la pantalla, entonces debe observar el mes actual renderizado en una cuadrícula de 7 columnas que incluye todos los días correspondientes a ese mes. |   |
| --- | --- | --- | --- | --- | --- |
|   |   | Desarrollador: ¿Qué día será |   | Dado que la usuaria desliza el dedo hacia la derecha o izquierda sobre la cuadrícula, cuando ocurra el gesto, entonces el calendario debe transicionar velozmente (en menos de 1 segundo) al mes anterior o siguiente. |   |
|   |   | tomado como el inicio de la semana? |   |   |   |
|   |   | Product Owner: Dependerá de la región (Locale) del dispositivo, pero de manera predeterminada el calendario debe posicionar el Lunes en la primera columna. |   |   |   |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## 1.1.2 HU-02 Registro de sangrado, flujo y cólicos :

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero poder | Desarrollador: ¿Cuántas opciones | Dado que el calendario está en |
| seleccionar un día en el calendario | de intensidad manejamos en flujo y | pantalla, cuando la usuaria toque |
| y registrar si hubo sangrado, el | dolor para no saturar la UI? | el cuadro numérico de cualquier |
| nivel de flujo y la intensidad de los | Product Owner: Solo tres opciones | día, entonces se desplegará una |
| cólicos, para llevar un historial | categóricas: Leve, Moderado y | ventana (Bottom Sheet de |
| preciso. | Fuerte. | Compose) mostrando los selectores |
|   | Desarrollador: ¿Qué ocurre si la | de flujo y cólicos. |
|   | usuaria se equivocó de día y quiere |   |
|   | borrar el registro? | Dado que la usuaria ajusta las |
|   | Product Owner: El formulario debe | opciones de registro en el cuadro de |
|   | tener un botón para "Limpiar | diálogo, cuando cierre la ventana o |
|   | datos" o permitir deseleccionar | guarde los datos, entonces la |
|   | todo, regresando ese día a un | aplicación debe persistir esos datos |
|   | estado neutro en la base de datos | de manera inmediata en la base de |
|   | local. | datos local (Room) bajo esa fecha |
|   |   | específica. |
|   |   | Dado que un día tiene datos |
|   |   | guardados, cuando la usuaria vacíe |
|   |   | todas las opciones de dicho día, |
|   |   | entonces la base de datos debe |
|   |   | eliminar los síntomas asociados y la |
|   |   | interfaz debe remover el sangrado |
|   |   | de esa fecha. |

## 1.1.3 HU-03 Identificación visual de fases en el calendario :

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero que los días | Desarrollador: ¿Los colores | Dado que la vista del mes procesa |
| del calendario tengan colores | rellenarán toda la celda del | los días renderizados contra el |
| distintos dependiendo de la fase del | calendario o se usarán pequeños | historial de ciclos guardado, |
| ciclo en la que caen, para identificar | puntos indicadores? | cuando el calendario se dibuje en |
| rápidamente mis ventanas fértiles o | Product Owner: Se rellenará todo | pantalla, entonces las celdas de |
| de menstruación. | el fondo del cuadro numérico para | días de menstruación y de ventana |
|   | destacar ventanas completas (ej. | fértil deben colorearse con sus |
|   | cinco días contiguos de ventana | paletas de colores |
|   | fértil en color suave). | correspondientes. |
|   | Desarrollador: ¿Cómo |   |
|   | garantizamos la accesibilidad visual | Dado que el sistema operativo del |
|   | al cambiar entre tema claro y | teléfono cambie a "Modo Oscuro", |
|   | oscuro en el teléfono? | cuando la aplicación se repinte, |
|   | Product Owner: Se usará una | entonces el fondo de los colores de |
|   | paleta de colores de baja opacidad | las fases en el calendario debe |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |
| adaptativa en colors.xml (o en la | adaptarse conservando legibilidad |   |
| definición del tema de Jetpack | sin causar molestias visuales por |   |
| Compose) que contraste | colores saturados. |   |
| correctamente con la fuente |   |   |
| tipográfica blanca o negra en |   |   |
| ambos modos. |   |   |

## 1.2 Sprint 2

Historias de la Pantalla Principal (HU-01, 02, 03).

## 1.2.1 HU-04 Días faltantes para el próximo periodo :

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero ver en la | Desarrollador: ¿Qué mostramos si | Dado que la app tiene acceso a los |
| pantalla principal cuántos días | la usuaria acaba de instalar la app | datos del ciclo (históricos o de |
| faltan para mi próximo periodo | y no tiene historial? | onboarding), cuando la usuaria |
| para poder planificar mis | Product Owner: Durante el | ingrese a la pantalla principal, |
| actividades de la semana. | onboarding, se le pedirá la fecha de | entonces debe visualizar el número |
|   | su último periodo y la duración | exacto de días restantes para su |
|   | promedio de su ciclo. Usaremos | próximo ciclo. |
|   | esos datos para el cálculo temporal. |   |
|   | Desarrollador: ¿Cómo debe | Dado que la fecha actual coincide o |
|   | reaccionar el texto si la usuaria | supera la fecha predicha sin un |
|   | tiene un retraso? | registro de nuevo periodo, cuando |
|   | Product Owner: Si llega el día | la usuaria vea el panel principal, |
|   | previsto, debe decir "Tu periodo | entonces el texto debe cambiar |
|   | debería iniciar hoy". Si la fecha se | automáticamente a estado de aviso |
|   | supera sin registro de sangrado, el | (hoy) o cálculo de días de retraso. |
|   | texto debe cambiar a advertencia |   |
|   | (ej. "Tienes X días de retraso") en |   |
|   | un color que destaque el estado de |   |
|   | alerta. |   |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## 1.2.2 HU-05: Visualización de la fase actual

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero visualizar en | Desarrollador: ¿Cuáles son las | Dado que el motor lógico de la app |
| qué fase de mi ciclo me encuentro | fases exactas que el sistema va a | calcula las fechas del ciclo en base |
| actualmente para entender el | manejar? | al calendario, cuando la usuaria |
| estado de mi cuerpo. | Product Owner: Serán cuatro: | revise su estado en el día actual, |
|   | Menstrual, Folicular, Ovulación y | entonces debe poder leer de forma |
|   | Lútea. | clara el título de la fase menstrual |
|   | Desarrollador: Al no tener histórico | en la que se encuentra (Menstrual, |
|   | al inicio, ¿cómo ajustamos las | Folicular, Ovulación o Lútea). |
|   | ventanas de cada fase? |   |
|   | Product Owner: Al principio |   |
|   | usaremos una fórmula médica |   |
|   | estándar restando días hacia atrás |   |
|   | (ej. la fase lútea tiene una duración |   |
|   | promedio de 14 días previos a la |   |
|   | menstruación). |   |

## 1.2.3 HU-06: Descripción de síntomas de la fase

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero leer una | Desarrollador: Si la app es offline, | Dado que la pantalla principal ha |
| breve descripción de los síntomas y | ¿dónde alojaremos los textos de los | identificado la fase actual de la |
| estados de ánimo típicos de mi fase | síntomas para que sean dinámicos | usuaria, cuando renderice la |
| actual para validar cómo me siento. | y bilingües? | información complementaria, |
|   |   | entonces se debe mostrar una lista |
|   | Product Owner: Los textos estarán | de al menos tres síntomas físicos o |
|   | almacenados localmente (ya sea | emocionales comunes de dicha |
|   | pre-poblados en Room Database o | fase. |
|   | usando recursos de cadenas | Dado que la usuaria cambie el |
|   | strings.xml de Android) | idioma principal de su dispositivo |
|   | garantizando que no haya que | Android (de Español a Inglés), |
|   | hacer peticiones a una API externa. | cuando abra Coral Log, entonces |
|   |   | las descripciones de los síntomas |
|   |   | deben mostrarse automáticamente |
|   |   | en el idioma seleccionado. |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## 1.3 Sprint 3

Historias del Análisis de Salud (HU-07, 08, 09, 10)

- 1.3.1 HU-07: Promedio de duración del ciclo:

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero ver la duración promedio de mi ciclo menstrual en una pantalla de métricas, para entender si mis ciclos tienen un patrón regular o irregular a lo largo del tiempo. | Desarrollador: ¿Cómo calculamos | Dado que la usuaria accede a la |
|   | el promedio si un mes el ciclo es | pestaña de métricas y posee |
|   | anormalmente largo por estrés (ej. | registros suficientes, cuando el |
|   | 45 días)? Eso dañaría el promedio | motor lógico calcule la duración, |
|   | matemático de los meses normales. | entonces debe mostrar en pantalla |
|   | Product Owner: Tienes razón. | un número entero que represente el |
|   | Debemos implementar una regla de | promedio, ignorando de manera |
|   | exclusión de valores atípicos | automática los ciclos registrados |
|   | (outliers): los ciclos menores a 21 | como atípicos (<21 días o >40 días). |
|   | días o mayores a 40 días se |   |
|   | ignorarán en la sumatoria, o en su |   |
|   | defecto, utilizaremos la mediana |   |
|   | estadística en lugar de la media |   |
|   | pura. |   |
|   | Desarrollador: ¿Y mostramos |   |
|   | decimales? |   |
|   | Product Owner: No, la vista debe |   |
|   | mostrar el número de días |   |
|   | redondeado al entero más cercano |   |
|   | (ej. "28 días"). |   |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## 1.3.2 HU-08: Nivel de flujo promedio

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero visualizar el | Desarrollador: El flujo se guarda en | Dado que la usuaria revisa su |
| nivel de flujo promedio que he | Room como una categoría (Leve, | dashboard, cuando el sistema |
| registrado en mis últimos meses, | Moderado, Fuerte). ¿Cómo | procese el histórico de flujo de los |
| para conocer mi tendencia normal y | sacamos un promedio de palabras? | últimos 6 meses, entonces debe |
| estar mejor preparada. | Product Owner: Asignaremos un | mostrar en pantalla el nivel |
|   | valor numérico por debajo de la | categórico predominante (Leve, |
|   | interfaz (Leve=1, Moderado=2, | Moderado o Fuerte) basándose en |
|   | Fuerte=3). Calcularemos la "Moda" | la medida estadística de moda. |
|   | (el valor que más se repite) en los |   |
|   | últimos 6 ciclos, y devolveremos a |   |
|   | la interfaz la palabra |   |
|   | correspondiente. |   |

## 1.3.3 HU-09: Nivel de intensidad de cólicos promedio

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero observar el | Desarrollador: ¿El promedio de | Dado que la aplicación analiza los |
| nivel de intensidad de cólicos | dolor evalúa todo el mes o solo los | registros de dolor en Room |
| promedio, para identificar si el | días específicos de menstruación? | Database, cuando se renderice la |
| dolor durante mi periodo está |   | métrica en la interfaz, entonces el |
| aumentando, disminuyendo o | Product Owner: Para que el dato | resultado debe calcularse |
| manteniéndose estable. | sea útil, la consulta a la base de | excluyendo cualquier día del |
|   | datos (Query) debe filtrar y evaluar | calendario que no esté marcado |
|   | únicamente los niveles de dolor | explícitamente como "día de |
|   | registrados en los días marcados | sangrado". |
|   | con sangrado activo. |   |

## 1.3.4 HU-10: Mensaje por datos insuficientes

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero que la | Desarrollador: ¿Ocultamos toda la | Dado que la usuaria entra al |
| aplicación me muestre un mensaje | pestaña de métricas si no hay | dashboard, cuando la base de |
| informativo si no tengo suficientes | datos? | datos retorne el histórico y detecte |
| ciclos registrados, para evitar | Product Owner: No, la pestaña | menos de 2 ciclos menstruales |
| confundirme con promedios | debe ser accesible para que sepa | cerrados, entonces las tarjetas de |
| matemáticamente inexactos o | que la función existe. Mostraremos | métricas numéricas deben |
| irreales. | un diseño de "Empty State" (estado | ocultarse y ser reemplazadas por |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

vacío) con un texto amigable y un ícono indicando que necesitamos más tiempo.

un componente informativo que explique: "Necesitamos al menos 2 ciclos válidos (duración regular) registrados para mostrar estas métricas".

## 1.4 Sprint 4

Historias de Apropiación Visual (HU-11, 12)

1.4.1 HU-11: Selección de temas de colores predefinidos

| Historia de usuario Como usuaria, quiero poder elegir entre diferentes temas de colores predefinidos (ej. Tema Océano, Tema Coral) en una pantalla de configuración, para que la interfaz se adapte a mis gustos personales. |   | Conversación Desarrollador: ¿Guardamos las preferencias de tema en Room Database? Product Owner: No hace falta. Como es una configuración simple clave-valor y no un dato relacional, usaremos Jetpack DataStore. Desarrollador: ¿Hay que reiniciar la app para aplicar el tema? Product Owner: No, implementaremos un estado reactivo (StateFlow) para que al tocar el tema, toda la paleta de colores de Jetpack Compose cambie instantáneamente en tiempo real. |   | Criterios de aceptación Dado que la usuaria se encuentra en la pantalla de personalización, cuando seleccione una de las paletas de colores predefinidas, entonces la interfaz gráfica debe actualizar sus variables de color al instante sin requerir el reinicio de la aplicación. |   |
| --- | --- | --- | --- | --- | --- |
|   |   |   |   | Dado que la usuaria cierra forzosamente la aplicación, cuando vuelva a abrirla, entonces la interfaz debe leer DataStore y renderizar el último tema seleccionado. |   |


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## 1.4.2 HU-12: Selección de fuente tipográfica

| Historia de usuario | Conversación | Criterios de aceptación |
| --- | --- | --- |
| Como usuaria, quiero tener la | Desarrollador: ¿Dejamos que | Dado que la usuaria abre el menú |
| opción de seleccionar entre | importe fuentes desde su teléfono? | de tipografías, cuando pulse sobre |
| distintas fuentes tipográficas para | Product Owner: No, eso complica el | una de las opciones predefinidas |
| la aplicación, para hacer que mi | manejo de archivos locales. | (ej. Sans-Serif, Rounded, Serif), |
| experiencia de lectura sea más | Ofreceremos un catálogo cerrado | entonces la propiedad FontFamily |
| cómoda y personalizada. | de 3 a 4 tipografías | de la tipografía base de Jetpack |
|   | pre-empaquetadas en la carpeta | Compose debe actualizarse, |
|   | res/font del proyecto Android para | aplicándose a todos los textos de la |
|   | asegurar que los márgenes y pesos | app de forma reactiva. |
|   | visuales no se rompan. |   |

## 2. Requisitos suplementarios

## 2.1 Usabilidad

- visual establecido (Modo Claro o Modo Oscuro) en el sistema operativo Android del dispositivo, garantizando que las paletas cromáticas mantengan los contrastes adecuados sin requerir intervención del usuario. 2.1.1 Accesibilidad La interfaz de la aplicación debe heredar y adaptarse de manera automática al tema

- detectando el Locale del dispositivo móvil para cargar dinámicamente el idioma correcto de la interfaz de manera automática. 2.1.2 Operabilidad El sistema debe proveer una experiencia completamente bilingüe (Español e Inglés)

## 2.2 Compatibilidad

- autónoma (offline-first). No debe depender de interfaces de programación de aplicaciones (APIs) externas, bases de datos en la nube ni requerir conexión a internet para el funcionamiento pleno de ninguna de sus características. 2.2.1 Interoperabilidad Al ser una herramienta con enfoque en la privacidad, la aplicación debe ser 100%

## 2.3 Fiabilidad

- sistema operativo (como la rotación física de la pantalla o la minimización repentina), garantizando que no ocurra pérdida de datos temporales en pantalla ni cierres inesperados (Crashes/ANR). 2.3.1 Tolerancia a fallos El sistema debe gestionar de manera robusta los cambios de configuración del

- datos local (Room) deben ser atómicas. Si ocurre una interrupción abrupta en el dispositivo, la base de datos no debe quedar en un estado corrupto. 2.3.2 Madurez Todas las operaciones de inserción, actualización o eliminación de registros en la base de


| Especificación de Requisitos de Software | Versión: | 1.1 |
| --- | --- | --- |
| Coral Log | Fecha: | 03/jul/26 |
| ERS |   |   |

## 2.4 Eficiencia de Desempeño

- renderizado de los datos históricos de los ciclos en la vista de calendario deben ejecutarse fluidamente con un tiempo de respuesta menor a 1 segundo. 2.4.1 Comportamiento temporal Las transiciones de navegación entre pantallas y, en especial, el

- delegando los cálculos algorítmicos complejos y las lecturas a la base de datos a hilos de ejecución secundarios (mediante el uso de Corrutinas), manteniendo el hilo principal libre. 2.4.2 Utilización de recursos La aplicación debe minimizar el consumo de batería del dispositivo

## 2.5 Seguridad

- manera exclusiva dentro del espacio de almacenamiento aislado de la aplicación (App Sandbox). Queda estrictamente prohibida la recolección, telemetría o transmisión de estos datos hacia servidores de terceros. 2.5.1 Confidencialidad Toda la información sensible de salud registrada por la usuaria debe persistir de

## 2.6 Mantenibilidad

- arquitectónico Model-View-ViewModel (MVVM) en conjunto con Jetpack Compose, asegurando una separación total entre la lógica del cálculo del ciclo menstrual y la capa de presentación visual. 2.6.1 Modularidad El código base de la aplicación debe estructurarse estrictamente bajo el patrón

- permitiendo que en el futuro se puedan agregar las tablas necesarias para las analíticas de salud (Release 2) sin riesgo de sobreescribir o borrar el historial menstrual ya registrado por la usuaria. 2.6.2 Modificabilidad La base de datos local debe implementarse con un sistema previsor de migraciones,
