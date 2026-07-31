# **Brief**

# **Coral Log**

# 

# **Versión 1.2**

# **Histórico de Revisiones**

| Fecha | Versión | Descripción | Autor(es) |
| ----- | ----- | ----- | ----- |
| *02/jul/26* | *1.0* | *Creación del Brief, nombre de coral log, Necesidad, Backlog, Restricciones y Rangos de Calidad.* | *Jesús Acosta* |
| *06/jul/26* | *1.1* | *Intercambio del contenido de la épica 2 con la 1 y viceversa* | *Jesús Acosta* |
| *28/jul/26* | *1.2* | *Ajuste de error por la plantilla* | *Jesús Acosta* |
|  |  |  |  |

# **Tabla de Contenidos**

[**1\. Necesidad	4**](#necesidad)

[**2\. Backlog	6**](#backlog)

[Épica 1: Registro Histórico y Calendario	7](#épica-1:-registro-histórico-y-calendario)

[Épica 2: Control Rápido del Ciclo	7](#épica-2:-control-rápido-del-ciclo)

[Épica 3: Análisis de Salud (Dashboard de Métricas)	7](#épica-3:-análisis-de-salud-\(dashboard-de-métricas\))

[Épica 4: Apropiación Visual (Personalización)	8](#épica-4:-apropiación-visual-\(personalización\))

[**3\. Restricciones	8**](#restricciones)

[**4\. Rangos de Calidad	8**](#rangos-de-calidad)

# 	**Brief**

*El propósito de este documento es recolectar, analizar y definir las necesidades a un alto nivel utilizando un Mapa de Impacto, del \<Nombre del Sistema\>. Con base en ella se prepara el Backlog (Users Story Map). Se especifican las restricciones de la aplicación y los criterios de aceptación aplicables al caso. Finalmente se identifican los rangos de calidad deseados.*

1. # **Necesidad** {#necesidad}

| *Meta* | *Personas* | *Impacto* | *Entregable* |
| :---- | :---- | :---- | :---- |
| ***Control rápido del ciclo*** | *Usuarias* | *Podrán visualizar rápidamente el estado de su ciclo actual para prever sus días fértiles y síntomas esperados de un solo vistazo.* | ***Estado del ciclo a día de hoy** • Mostrar tiempo restante para el siguiente periodo. • Mostrar fase actual. • Mostrar síntomas/comportamientos típicos. • Mostrar tiempo hasta el siguiente periodo fértil.* |
| ***Registro histórico y seguimiento*** | *Usuarias* | *Podrán registrar de manera sencilla su sangrado y síntomas diarios para mantener un historial detallado sin depender de la memoria.* | ***Calendario interactivo** • Vista de un mes a la vez. • Marcar días de sangrado, nivel de flujo e intensidad de cólicos. • Colorear días según la fase menstrual.* |
| ***Análisis de salud personal*** | *Usuarias* | *Entenderán mejor los patrones de su cuerpo mediante el análisis automático que hace la app de su historial menstrual.* | ***Dashboard de métricas** • Calcular y mostrar la duración promedio del ciclo. • Mostrar nivel de flujo promedio. • Mostrar nivel de dolor promedio.* |
| ***Apropiación y comodidad visual*** | *Usuarias* | *Podrán adaptar la interfaz a sus gustos para lograr una experiencia más cómoda, personal y discreta.* | ***Pantalla de personalización** • Cambiar los colores del tema (ej. colores asignados a cada fase). • Cambiar fuentes tipográficas de la aplicación.* |

2. # **Backlog** {#backlog}

| *Actividad Principal (Meta)* | *Control Rápido del Ciclo* | *Registro Histórico (Calendario)* | *Análisis de Salud* | *Apropiación Visual* |
| :---- | :---- | :---- | :---- | :---- |
| ***Tarea de Usuario*** | *Consultar el estado actual* | *Registrar y visualizar días* | *Revisar promedios históricos* | *Ajustar interfaz* |
| ***MVP (Iteración 1\)*** | *• Ver días restantes para el próximo periodo. • Ver fase menstrual actual. • Ver tiempo hasta el próximo periodo fértil. • Leer síntomas típicos de la fase.* | *• Visualizar el mes actual en cuadrícula. • Marcar días de sangrado (Sí/No). • Registrar nivel de flujo (Leve/Mod/Fuerte). • Registrar intensidad de cólicos (Leve/Mod/Fuerte). • Ver días coloreados según la fase.* | *(No incluido en MVP para agilizar lanzamiento)* | *(No incluido en MVP para agilizar lanzamiento)* |
| ***Release 2 (Iteración 2\)*** |  |  | *• Ver duración promedio del ciclo. • Ver nivel de flujo promedio del mes. • Ver nivel de dolor promedio.* | *• Seleccionar temas/colores para las fases. • Cambiar la fuente tipográfica general.* |

### **Épica 1: Registro Histórico y Calendario** {#épica-1:-registro-histórico-y-calendario}

* **HU-01:** Como usuaria, quiero ver un calendario mensual interactivo para tener una perspectiva global de mi mes.  
* **HU-02:** Como usuaria, quiero poder seleccionar un día en el calendario y registrar si hubo sangrado, el nivel de flujo y la intensidad de los cólicos, para llevar un historial preciso.  
* **HU-03:** Como usuaria, quiero que los días del calendario tengan colores distintos dependiendo de la fase del ciclo en la que caen, para identificar rápidamente mis ventanas fértiles o de menstruación.

### **Épica 2: Control Rápido del Ciclo** {#épica-2:-control-rápido-del-ciclo}

* **HU-04:** Como usuaria, quiero ver en la pantalla principal cuántos días faltan para mi próximo periodo para poder planificar mis actividades de la semana.  
* **HU-05:** Como usuaria, quiero visualizar en qué fase de mi ciclo me encuentro actualmente para entender los cambios en mi cuerpo.  
* **HU-06:** Como usuaria, quiero leer una breve descripción de los síntomas y estados de ánimo típicos de mi fase actual para validar cómo me siento.

### **Épica 3: Análisis de Salud (Dashboard de Métricas)**  {#épica-3:-análisis-de-salud-(dashboard-de-métricas)}

* **HU-07:** Como usuaria, quiero ver la **duración promedio de mi ciclo menstrual** en una pantalla de métricas, para entender si mis ciclos tienen un patrón regular o irregular a lo largo del tiempo.  
* **HU-08:** Como usuaria, quiero visualizar el **nivel de flujo promedio** que he registrado en mis últimos meses, para conocer mi tendencia normal y estar mejor preparada.  
* **HU-09:** Como usuaria, quiero observar el **nivel de intensidad de cólicos promedio**, para identificar si el dolor durante mi periodo está aumentando, disminuyendo o manteniéndose estable.  
* **HU-10:** Como usuaria, quiero que la aplicación me muestre un **mensaje informativo si no tengo suficientes ciclos registrados** (ej. menos de 2 meses), para evitar confundirme con promedios matemáticamente inexactos o irreales.

### **Épica 4: Apropiación Visual (Personalización)**  {#épica-4:-apropiación-visual-(personalización)}

* **HU-11:** Como usuaria, quiero poder elegir entre **diferentes temas de colores predefinidos** (ej. Tema Océano, Tema Coral) en una pantalla de configuración, para que la interfaz se adapte a mis gustos personales sin que los colores de las fases pierdan sentido o legibilidad.  
* **HU-12:** Como usuaria, quiero tener la opción de **seleccionar entre distintas fuentes tipográficas** para la aplicación, para hacer que mi experiencia de lectura en el diario sea más cómoda y personalizada.

3. # **Restricciones** {#restricciones}

* ***Almacenamiento y Privacidad (Offline-First):** Toda la información registrada por la usuaria se almacenará exclusivamente de forma local en el almacenamiento interno del dispositivo (mediante Room/SQLite). No existirá un servidor backend, base de datos en la nube, ni recolección externa de datos personales.*  
* ***Idiomas Soportados:** El proyecto estará orientado y restringido exclusivamente a los idiomas español e inglés. El sistema deberá detectar y adaptarse automáticamente al idioma (Locale) configurado en el sistema operativo del dispositivo.*  
* ***Sincronización Multidispositivo:** Debido a la arquitectura de almacenamiento local continuo, la aplicación no soportará sincronización automática en tiempo real entre múltiples dispositivos (por ejemplo, usarla en un teléfono y una tablet simultáneamente con la misma data).*  
* ***Entorno Monousuario:** La aplicación operará bajo el supuesto de un único usuario por dispositivo. No se desarrollará un sistema de autenticación (login/registro) ni soporte para múltiples perfiles.*  
* ***Respaldo de Datos:** Al carecer de nube propia, la recuperación de datos en caso de pérdida, robo o cambio de dispositivo móvil no será automática. Quedará restringida a las capacidades de respaldo del sistema operativo (Google One Backup) o a futuras funciones de exportación manual de archivos.*  
* ***Plataforma:** El producto será accesible únicamente mediante dispositivos móviles con sistema operativo Android. No se plantean versiones para iOS, web o escritorio.*

4. # **Rangos de Calidad** {#rangos-de-calidad}

* ***Usabilidad y Adaptabilidad:** La interfaz debe ser fácil de entender, usar y navegar, priorizando un diseño discreto. El sistema debe adaptarse automáticamente al idioma (Inglés o Español) y al tema (Claro u Oscuro) configurado a nivel de sistema operativo en el dispositivo del usuario, sin requerir intervención manual.*  
* ***Privacidad y Seguridad por Diseño:** Al ser una aplicación enfocada en datos sensibles de salud, la privacidad es absoluta. El 100% de la información generada por la usuaria debe persistir únicamente en la base de datos local del dispositivo, garantizando que ninguna métrica personal sea transmitida a terceros.*  
* ***Time Behaviour:** Las transiciones entre pantallas y la carga de datos históricos en el calendario deben ser imperceptibles para el usuario (tiempo de respuesta menor a 1 segundo al cambiar de mes). Además, al no depender de sincronización en red, la aplicación debe mantener un consumo de batería mínimo.*  
* ***Confiabilidad y Tolerancia a Fallas:** La aplicación debe ser confiable en el manejo del ciclo de vida de Android. No debe ocurrir pérdida de datos o cierres inesperados (Crash/ANR) durante los cambios de configuración del dispositivo, como la rotación de pantalla o la minimización repentina de la aplicación.*  
* ***Mantenibilidad (Flexibilidad):** La arquitectura del código base (implementando patrones como MVVM) debe ser lo suficientemente limpia y modular para ser flexible a cambios, mejoras y añadidos de funcionalidades en el futuro, específicamente para integrar sin fricciones los módulos de la segunda iteración (Dashboard de métricas y Personalización).*

