# Cuestionario — Parte A del examen de la Unidad IV

> **Cómo se llena este archivo.** Responda **dentro de este mismo archivo**, debajo de cada pregunta, en el bloque marcado como `**Respuesta:**`. No borre ni reescriba los enunciados: el evaluador compara pregunta por pregunta. No añada ni quite secciones.
>
> **Este archivo se versiona en el repositorio.** Debe existir en la raíz, llamarse exactamente `Cuestionario.md`, y sus respuestas deben llegar por *commits* sucesivos hechos cuando el docente lo indique. Un archivo que aparece completo en un único *commit* al final de la sesión no cumple el protocolo y se trata según el criterio de piso 4 del examen.
>
> Se valora la precisión técnica y la justificación, **no la extensión**. Una respuesta correcta de seis líneas vale más que una página imprecisa. Cuando la pregunta pida referirse al proyecto base, hágalo con nombres concretos de clases o de *endpoints*.

---

## Datos del estudiante

| Campo | Valor                       |
|---|-----------------------------|
| Apellidos y nombres | Loor Medranda Marlon Taylor |
| Número de carnet | 0928087469                   |
| Correo institucional | mloorm14@uteq.edu.ec         |
| Fecha | 28/08/2026                  |
| URL del repositorio |       https://github.com/mloorm14/biblioteca-u4-base.git                      |

---

## A1. Restricciones de REST aplicadas a un caso concreto — 8 puntos

**a) Enuncie las seis restricciones del estilo arquitectónico REST según Fielding. (3 puntos)**

**Respuesta:**
1. Cliente-Servidor (Client-Server)
2. Sin estado (Stateless)
3. Cacheable (Cacheability)
4. Interfaz uniforme (Uniform Interface)
5. Sistema de capas (Layered System)
6. Código bajo demanda (Code on Demand)

**b) El proyecto base expone `GET /api/v1/autores` y guarda el estado de la sesión del usuario solo en el JWT que el cliente envía en cada petición. Explique qué restricción concreta se está cumpliendo con esa decisión y qué consecuencia práctica tiene para escalar el sistema a varios servidores detrás de un balanceador. (3 puntos)**

**Respuesta:**
Se está cumpliendo la restricción **Sin estado (Stateless)**. La consecuencia práctica es que facilita enormemente el escalamiento horizontal. Como el servidor no guarda el estado de la sesión en su memoria local, cualquier instancia del servidor detrás del balanceador de carga puede atender la petición del cliente leyendo la información directamente desde el JWT.

**c) De las seis restricciones, indique cuál es opcional y dé un ejemplo real de una API que la use. (2 puntos)**

**Respuesta:**
La restricción opcional es **Código bajo demanda (Code on Demand)**. Un ejemplo real es el servicio de Google reCAPTCHA, cuya API devuelve un script de JavaScript que el navegador del cliente debe ejecutar para resolver el desafío de validación.

---

## A2. Anatomía y ciclo de vida de un JWT — 8 puntos

**a) Un JWT tiene tres partes separadas por puntos. Nómbrelas en orden e indique qué contiene cada una. (3 puntos)**

**Respuesta:**
1. **Header (Encabezado):** Contiene el tipo de token (JWT) y el algoritmo de firma criptográfica utilizado (ej. HS256).
2. **Payload (Carga útil):** Contiene los *claims* o declaraciones (datos del usuario, roles, fecha de emisión, fecha de expiración).
3. **Signature (Firma):** Un hash generado a partir del Header, el Payload y el secreto del servidor, utilizado para verificar que el token no ha sido alterado.

**b) Un compañero afirma: «como el JWT va firmado, puedo guardar en el *payload* la contraseña del usuario sin riesgo». Explique por qué está equivocado, precisando la diferencia entre firmar y cifrar. (2 puntos)**

**Respuesta:**
Está equivocado porque firmar no es lo mismo que cifrar. Firmar únicamente garantiza la integridad y autenticidad del mensaje (que no ha sido alterado y viene de una fuente confiable), pero el *payload* de un JWT estándar solo está codificado en Base64Url, no cifrado. Cualquiera con acceso al token puede decodificarlo y leer su contenido en texto plano. Cifrar, por el contrario, ocultaría el contenido, requiriendo una clave para leerlo.

**c) El JWT es *stateless* por diseño, lo que genera un problema conocido: no se puede invalidar un token antes de que expire. Describa dos estrategias distintas para revocarlo y señale la desventaja de cada una. (3 puntos)**

**Respuesta:**
1. **Lista negra (Blacklist) en memoria/caché:** Guardar el ID (jti) de los tokens revocados en una base de datos rápida como Redis hasta que expiren. *Desventaja:* Introduce estado al servidor (rompiendo el principio stateless puro) y añade latencia/costo por la consulta a Redis en cada petición.
2. **Tiempos de expiración muy cortos (Short TTL) con Refresh Tokens:** Emitir el JWT con una vida útil de pocos minutos y usar un token de refresco para obtener uno nuevo. *Desventaja:* No es una revocación inmediata; deja una "ventana de vulnerabilidad" durante esos minutos en los que el JWT comprometido sigue siendo válido.

---

## A3. SOAP frente a REST — 8 puntos

**a) Complete la tabla comparativa con seis criterios entre SOAP y REST. (5 puntos)**

**Respuesta:**

| Criterio | SOAP | REST |
|---|---|---|
| Formato del mensaje | Exclusivamente XML | Múltiples (JSON, XML, HTML, texto plano) |
| Contrato de descripción | WSDL (estricto y obligatorio) | OpenAPI / Swagger (opcional) |
| Sobrecarga de serialización | Alta (envolturas pesadas Envelope, Body) | Baja (ligero, uso directo de HTTP) |
| Tipado | Estricto (definido por el XML Schema) | Dinámico / Débil (depende de JSON) |
| Facilidad de consumo desde un cliente móvil | Baja / Compleja | Alta / Sencilla (JSON nativo en móviles) |
| Manejo de errores | SOAP Faults (incluidos dentro del XML) | Códigos de estado HTTP estándar |

**b) El Servicio de Rentas Internas del Ecuador expone la autorización de comprobantes electrónicos mediante servicios SOAP. Explique dos razones técnicas por las que una institución de ese tipo mantiene SOAP en lugar de migrar a REST. (3 puntos)**

**Respuesta:**
1. **Seguridad y transaccionalidad robustas:** SOAP incluye estándares maduros como WS-Security, que permiten encriptación y firma digital a nivel de mensaje (partes específicas del XML) y garantizan el no repudio, aspectos críticos para operaciones tributarias legales.
2. **Contratos estrictos:** El uso de WSDL obliga a los sistemas de miles de contribuyentes a cumplir un contrato rígido y tipado antes de enviar la petición, reduciendo errores de integración y garantizando la compatibilidad estricta de las estructuras de las facturas.

---

## A4. Cache-aside sobre un servicio externo — 8 puntos

> El proyecto base define en `CacheConfig` dos espacios de caché: `libros` con TTL de 2 minutos y `openlibrary` con TTL de 24 horas.

**a) Describa el patrón *cache-aside* en sus cuatro pasos, desde que llega la petición hasta que se responde. (3 puntos)**

**Respuesta:**
1. La aplicación recibe la petición y busca primero el dato en la caché (usando una clave).
2. Si el dato existe (*Cache Hit*), lo devuelve inmediatamente al cliente y termina el proceso.
3. Si el dato no existe (*Cache Miss*), la aplicación consulta la fuente original de la verdad (la base de datos o API externa).
4. La aplicación guarda el dato obtenido en la caché (con su TTL) y lo devuelve al cliente.

**b) Justifique técnicamente por qué el TTL de `openlibrary` es doce veces mayor que el de `libros`, y qué criterio general debe guiar la elección de un TTL. (3 puntos)**

**Respuesta:**
El TTL de `openlibrary` es mayor porque los metadatos de un libro (como el autor o año de publicación en una API externa) cambian con muy escasa frecuencia, y consultar esa API conlleva latencia de red. El caché de `libros` tiene un TTL bajo porque representa el inventario local, el cual es altamente volátil debido a préstamos y devoluciones concurrentes. El criterio general para elegir un TTL es equilibrar el nivel de tolerancia de la aplicación a datos obsoletos frente al costo (en tiempo o procesamiento) de recalcular o volver a obtener ese dato.

**c) Explique por qué nunca debe almacenarse en caché la respuesta de un fallo del servicio externo, y describa qué le ocurriría al sistema si se hiciera. (2 puntos)**

**Respuesta:**
Nunca se debe almacenar un fallo porque produciría un "envenenamiento de caché". Si se guarda un error (como un 504 de la API externa), el sistema seguiría devolviendo ese error a todas las peticiones subsecuentes durante el tiempo que dure el TTL, incluso si el servicio externo ya se ha recuperado. Esto degradaría artificial e innecesariamente la disponibilidad del sistema.

---

## A5. Diagnóstico de códigos de estado y contrato de errores — 8 puntos

> Todos los errores del proyecto base salen en formato *Problem Details* conforme a la RFC 9457, que obsoleta a la RFC 7807.

Para cada escenario indique el código HTTP correcto y explique en una línea por qué. **Cada fila vale 1 punto** (0,5 por el código y 0,5 por la justificación); el literal g) vale 2 puntos.

| # | Escenario | Código | Justificación (una línea) |
|---|---|---|---|
| a | `GET /api/v1/libros/999999` y ese identificador no existe | 404 | El recurso solicitado específicamente por su identificador URI no fue encontrado en el servidor. |
| b | `POST /api/v1/libros` sin cabecera `Authorization` | 401 | El cliente no proporcionó credenciales de autenticación válidas para acceder al recurso protegido. |
| c | Usuario autenticado con rol `LECTOR` envía `POST /api/v1/libros` | 403 | El cliente está autenticado pero sus credenciales no tienen los permisos (rol) necesarios para esa operación. |
| d | `POST /api/v1/libros` con el campo `titulo` vacío | 400 | La petición está mal formada y no pasa las reglas de validación semántica o de sintaxis del cliente (`@Valid`). |
| e | Prestar un libro a un socio que ya tiene tres préstamos activos | 409 | La solicitud no puede procesarse porque genera un conflicto con el estado actual del negocio en el servidor. |
| f | La API de Open Library no responde dentro del *timeout* configurado | 504 | El servidor, actuando como puerta de enlace (gateway), no recibió respuesta a tiempo del servicio *upstream*. |

**g) Explique por qué devolver `200 OK` con un cuerpo `{"success": false}` es un error de diseño, y qué restricción de REST se incumple al hacerlo. (2 puntos)**

**Respuesta:**
Es un error de diseño porque oculta el verdadero resultado de la operación en la capa de aplicación, forzando al cliente a parsear el cuerpo JSON para saber si la petición realmente fue exitosa o no. Esto incumple la restricción de **Interfaz Uniforme**, específicamente la semántica autodescriptiva de los mensajes, ya que el protocolo HTTP ya provee códigos de estado estandarizados (4xx o 5xx) diseñados exactamente para comunicar la naturaleza de estos errores.

---

## Declaración de honestidad académica

Marque con una `x` y complete:

- [x] Declaro que estas respuestas son de mi autoría, redactadas durante la sesión de examen, sin asistencia de inteligencia artificial ni comunicación con terceros.

Firma (nombre completo): Loor Medranda Marlon Taylor