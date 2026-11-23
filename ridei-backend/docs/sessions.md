# Sessions (`ridei-sessions`)

Registro de tandas en circuito y análisis para mejorar como piloto. Es el backend de las pantallas
02, 04–09, 11, 12, 14–16 y 22–22E del diseño *Ridei: Coach de circuito móvil*: historial, resumen de
sesión, vuelta ideal, gráfico velocidad–distancia, ficha de curva, ranking de tiempo perdido,
visor de trazada, récords, la vuelta de referencia del HUD y los tiempos manuales de antes de
Ridei (§6).

| | |
|---|---|
| Módulo / paquete | `ridei-sessions` / `com.ridei.sessions` |
| Puerto | `8084` |
| Base de datos | `ridei_sessions` en `ridei-postgres` (Postgres 16, sin PostGIS) |
| Depende de | `ridei-circuits` (trazado, meta y dibujo) y `ridei-garage` (la moto es tuya), solo al **subir**, **recalcular**, **apuntar** tiempos manuales e **importar** notas |

## 1. Flujo

```
App (logger GPS/IMU)                 ridei-sessions                       circuits / garage
──────────────────                   ──────────────                       ─────────────────
elige trazado (circuits) y moto
genera sessionId (UUID) al grabar
graba muestras {t, lat, lon, speed…}
PUT /api/v1/sessions/{sessionId} ──▶ ¿ya existe? → 200 (idempotente)
                                     garage: GET /motorbikes ─────────▶ (token del piloto)
                                     circuits: GET /venues/{venueId} ─▶ (token del piloto)
                                     congela meta + dibujo del trazado
                                     cronometra vueltas y sectores
                                     guarda sesión + telemetría
                     ◀── 201 resumen con vueltas
GET …/{id}, /ideal-lap, /corners…    todo se calcula desde lo guardado (no llama a nadie)
```

- **Idempotencia**: el `sessionId` lo genera la app al empezar a grabar. Si la subida falla
  (cobertura en el paddock), se reintenta con el mismo id sin duplicar. Otro piloto con el mismo
  id → `409`.
- **Snapshot**: la meta y el dibujo del trazado se copian en la sesión. Si el catálogo cambia
  (p. ej. se corrige la meta), los tiempos guardados no cambian hasta `POST …/recalculate`.
- **Token del piloto**: sessions llama a circuits y garage reenviando el `Authorization` de la
  petición. Así garage decide qué motos son tuyas y circuits aplica su revocación online; no hay
  credencial de servicio compartida.

## 2. Dominio

```
Session (agregado)
├── RiderId (del JWT)                 ├── LayoutSnapshot: venue, track, layout + TrackGeometry
├── MotorbikeSnapshot (garage)        ├── Conditions (temperatura, DRY/DAMP/WET), notas
└── LapTiming: Lap[] (tiempo, sectores, distancia, LapValidity), reversedDirection
Telemetry (aparte; grande): TelemetrySample[] — t, posición, velocidad, precisión, inclinación
```

Servicios de dominio (Java puro, sin Spring):

| Clase | Qué hace |
|---|---|
| `LapTimer` → `TimedRun` | Crono GPS: pasos por meta y puertas de sector, vueltas, validez; trazas por distancia y vuelta ideal cosida |
| `CornerAnalyzer` | Detecta curvas en la mejor vuelta y mide cada paso por ellas |
| `PersonalRecords` | Récord por (trazado, moto): mejor vuelta GPS, ideal histórica, mejor tiempo manual, evolución |
| `ManualTimeNotesParser` | Notas pegadas → circuito, fecha, tiempo(s), condiciones (§6.2) |
| `CatalogMatcher` | Dentro de un complejo, elige pista y trazado según el texto (§6.2) |

### 2.1 Cronometraje (`LapTimer`)

Pensado para el GPS de un móvil (1 Hz habitual, unos metros de error) y para loggers de 10–25 Hz:

- **Cruce interpolado**: el instante del paso por meta se interpola dentro del tramo entre dos
  fixes (a 1 Hz y 150 km/h hay ~40 m entre fixes). Error en tests: < 0,25 s a 1 Hz con ±3 m de
  ruido, < 0,05 s a 10 Hz.
- **Meta alargada 15 m por la izquierda**. La meta del catálogo acaba en el muro de boxes; por la
  derecha no se alarga para que pasar por el pit lane **no** cuente como vuelta.
- **Sentido**: solo cuentan los cruces en el sentido de la marcha. Si la mayoría van al revés
  que el catálogo, se cronometra al revés y la sesión sale con `reversedDirection = true`. Lo he
  hecho así porque el sentido de Calafat es inferido (ver [database.md](database.md) §3): si fuera
  al revés, verías tus vueltas igualmente, y el flag indicaría que el sentido del catálogo está mal.
- **Vuelta de salida y de entrada**: lo que va antes del primer paso por meta y después del último
  no son vueltas.
- **Validez** (`LapValidity`), según el recorrido frente a la longitud del trazado:
  `TOO_SHORT` < 85 % (atajo), `TOO_LONG` > 115 % (meta no detectada: dos vueltas en una, o paso por
  boxes), `GPS_GAP` (hueco > 5 s entre fixes) y `VALID`. Solo las válidas cuentan para la mejor
  vuelta, la ideal, las curvas y los récords.
- **Distancia recorrida**: se integra la velocidad GPS (Doppler) si viene en las muestras. Sumar
  distancias entre posiciones con ruido infla el recorrido, y a 10 Hz invalidaba todas las vueltas.
  **La app debe enviar `speed`.**
- **Sectores**: el catálogo aún no define sectores. Se usan **4 sectores de igual longitud** del
  dibujo (`TrackGeometry.DEFAULT_SECTOR_COUNT`), con puertas perpendiculares de 40 m. Así los
  límites son estables entre sesiones, que es lo que necesita la ideal histórica. Una puerta solo
  cuenta cerca de donde le toca (±15 % de la vuelta), para que las horquillas de Calafat no
  confundan. Si no se detecta una puerta, ese sector queda `null` y la vuelta no entra en la ideal.
- **Fixes** con `accuracy` > 25 m se descartan.

### 2.2 Trazas por distancia

`TimedRun.trace(n)` devuelve la vuelta indexada por **distancia sobre el trazado** (0 → longitud).
Se normaliza por tramos, anclada en la meta y en cada puerta de sector, así dos vueltas con
líneas distintas se comparan punto a punto. De ahí salen el visor (15/16), el gráfico con delta
acumulado (07) y la **vuelta ideal cosida** (06): cada sector sale de la vuelta en la que se hizo,
desplazado en el tiempo.

### 2.3 Curvas (`CornerAnalyzer`)

- **Curva**: un mínimo de velocidad de la mejor vuelta, con al menos 15 km/h de frenada antes y
  10 km/h de aceleración después. Se fusionan mínimos a menos de 80 m. Se numeran T1, T2… desde la
  meta, y el sentido (izq./der.) sale de la geometría.
- **Zona de cada curva**: del punto de frenada (máxima velocidad antes del vértice) al de la
  siguiente. Las zonas cubren la vuelta entera, así la suma de pérdidas es lo recuperable por curvas.
- **Paso** (por vuelta válida): tiempo en la zona, velocidad de entrada, mínima y de salida
  (80 m después del vértice), metros de frenada antes del vértice e inclinación máxima.
- **Pérdida**: el tiempo de la mejor vuelta en la zona menos el mejor paso de la sesión.
- **Causa** de la mejor vuelta frente al mejor paso:
  - `BRAKING`: frena > 10 m antes.
  - `MIN_SPEED`: el vértice es > 3 km/h más lento.
  - `EXIT`: la salida es > 3 km/h más lenta.
  - `LINE`: nada de lo anterior, con lo que el tiempo se pierde en la línea.
  - `NONE`: la mejor vuelta ya hace su mejor paso.

A 1 Hz hay un punto cada ~40 m, así que los mínimos y los puntos de frenada son aproximados. La
ficha de curva es fiable con 10 Hz o más.

## 3. API

Todas las rutas requieren `Authorization: Bearer <access token>`. El piloto sale siempre del
token. Una sesión de otro piloto responde `404`, igual que una que no existe. Los tiempos van en
**milisegundos** y las distancias en metros.

| Método y ruta | Pantalla | Respuesta |
|---|---|---|
| `PUT /api/v1/sessions/{sessionId}` | subir al acabar la tanda | `201` nueva · `200` ya existía |
| `GET /api/v1/sessions?motorbikeId=&layoutId=&categoryGroup=` | 02 historial | lista, más reciente primero |
| `GET /api/v1/sessions/{id}` | 05 resumen + vueltas | detalle |
| `PATCH /api/v1/sessions/{id}` | renombrar, condiciones, notas | detalle |
| `DELETE /api/v1/sessions/{id}` | | `204` |
| `POST /api/v1/sessions/{id}/recalculate` | 03 tras corregir la meta | detalle |
| `GET /api/v1/sessions/{id}/ideal-lap` | 06 vuelta ideal | sectores y de qué vuelta sale cada uno |
| `GET /api/v1/sessions/{id}/ideal-lap/trace` | 06/07 | traza de la ideal |
| `GET /api/v1/sessions/{id}/laps/{n}/trace` | 15/16 visor | posición, velocidad e inclinación por distancia |
| `GET /api/v1/sessions/{id}/laps/{n}/comparison?compareTo=ideal\|best\|{n}` | 07 gráfico + delta | |
| `GET /api/v1/sessions/{id}/corners` | 08 ficha de curva · 09 ranking | |
| `GET /api/v1/sessions/records?layoutId=&motorbikeId=` | 11 mejor por circuito · 14 ficha de trazado | |
| `GET /api/v1/sessions/reference-lap?layoutId=&motorbikeId=` | 04 HUD (delta en vivo) | `200` · `204` si aún no hay récord |
| `…/manual-times…` | 22–22E tiempos manuales | ver §6.1 |

### 3.1 Subir una sesión

```http
PUT /api/v1/sessions/7b0c2f3e-5d1a-4c55-9a0e-2f8d1e6b9c41
Content-Type: application/json

{
  "venueId":  "10000000-0000-4000-8000-000000000009",
  "layoutId": "30000000-0000-4000-8000-000000000006",
  "motorbikeId": "<id de la moto en garage>",
  "name": "Tanda tarde",
  "conditions": { "temperatureCelsius": 24, "trackCondition": "DRY" },
  "notes": null,
  "samples": [
    { "t": 1795521600000, "lat": 40.9322170, "lon": 0.8394240, "speed": 41.2, "accuracy": 3.5, "lean": -12.0 },
    …
  ]
}
```

| Campo de muestra | | |
|---|---|---|
| `t` | obligatorio | instante **del fix GPS** (epoch ms), no el reloj del sistema |
| `lat`, `lon` | obligatorio | grados |
| `speed` | muy recomendado | m/s (velocidad GPS/Doppler) |
| `accuracy` | recomendado | precisión horizontal en metros |
| `lean` | opcional | inclinación en grados, + derecha / − izquierda (IMU) |

Entre 10 y 250 000 muestras por sesión y 6 h como máximo. No importa el orden: se ordenan y se
quitan los duplicados. El backend tolera lo que los móviles marcan como no válido: una velocidad
negativa (`-1` en iOS) o fuera de rango se ignora, igual que una inclinación fuera de rango. Un fix
sin `t`, `lat` o `lon`, en `(0, 0)` o con `accuracy` negativa se descarta. Ninguno de estos casos
rechaza la sesión. Calafat es `venueId 10000000-…-0009`, `layoutId 30000000-…-0006`
(Principal). El trazado *Corto* no tiene meta ni dibujo, así que devuelve `422`.

Respuesta (detalle de sesión, también en `GET /{id}`):

```json
{
  "id": "…", "name": "Tanda tarde", "startedAt": "…", "endedAt": "…",
  "venue": { "id": "…", "name": "Circuit de Calafat" }, "trackName": "Velocidad",
  "layout": { "id": "…", "name": "Principal", "lengthMeters": 3240, "sectorCount": 4, "reversedDirection": false },
  "motorbike": { "id": "…", "name": "Yamaha R6", "category": "…", "categoryGroup": "ASPHALT" },
  "conditions": { "temperatureCelsius": 24, "trackCondition": "DRY" }, "notes": null,
  "personalBest": true,
  "bestLap": { "number": 8, "timeMillis": 102318 },
  "idealLap": { "timeMillis": 101633, "gainMillis": 685 },
  "lapCount": 18, "validLapCount": 17,
  "laps": [
    { "number": 8, "startedAt": "…", "timeMillis": 102318,
      "sectorsMillis": [24110, 31880, 27450, 18878], "bestSectors": [false, false, true, true],
      "distanceMeters": 3236, "validity": "VALID", "best": true, "deltaToBestMillis": 0 }
  ]
}
```

### 3.2 Resto de respuestas (resumen)

- **`/ideal-lap`**: `{timeMillis, bestLapNumber, bestLapMillis, gainMillis, sectors:[{index, fromMeters, toMeters, timeMillis, fromLap, gainVsBestLapMillis}]}`.
- **`/laps/{n}/trace`** y **`/ideal-lap/trace`**: `{lap, timeMillis, lengthMeters, minSpeedKmh, maxSpeedKmh, points:[{d, t, v, lat, lon, lean}]}`. `d` es la distancia en m, `t` los ms desde la meta y `v` los km/h. `lap = 0` es la ideal.
- **`/laps/{n}/comparison`**: `{lap, lapTimeMillis, referenceLap, referenceTimeMillis, finalDeltaMillis, lengthMeters, points:[{d, v, refV, delta}]}`. Un `delta` positivo es que vas por detrás.
- **`/corners`**: `{referenceLap, totalRecoverableMillis, ranking:[4, 7, …], corners:[{number, label:"T4", direction, apexDistanceMeters, apex, zoneStartMeters, zoneEndMeters, bestPassLap, bestLapLossMillis, averageLossMillis, cause, bestLapPass, bestPass, passes:[{lap, timeMillis, entrySpeedKmh, minSpeedKmh, exitSpeedKmh, brakingMetersBeforeApex, maxLeanDegrees}]}]}`.
- **`/records`**: `[{venueId, venueName, layoutId, layoutName, motorbike, bestLap:{sessionId, lap, timeMillis, date}, idealLap:{timeMillis, sectors:[{index, timeMillis, sessionId, lap}]}, bestManual:{manualTimeId, timeMillis, precision, date, source}, sessionCount, validLapCount, manualTimeCount, evolution:[{source, sessionId, manualTimeId, startedAt, date, bestLapMillis, precision}]}]`.
  Hay un récord por (trazado, moto) con alguna vuelta GPS válida **o** algún tiempo manual, así que
  `bestLap` e `idealLap` pueden ser `null` (solo manuales) y `bestManual` es `null` si no hay
  manuales. `evolution` mezcla las dos fuentes (`source` `GPS` / `MANUAL`) por fecha; los manuales
  no tienen `startedAt`, solo `date`.
- **`/reference-lap`**: `{sessionId, timeMillis, sectorsMillis, finishLine:[left, right], sectorGates:[[left, right], …], trace}`. Las puertas son las de cronometrar, ya orientadas en el sentido en que se rodó. Con ellas el HUD da los mismos sectores que el servidor.

### 3.3 Errores

| Estado | Cuándo |
|---|---|
| `400` | JSON mal formado, validación, muestras inválidas (< 10 fixes útiles, coordenadas fuera de rango…) |
| `401` | sin token, o token revocado según circuits o garage |
| `404` | sesión, vuelta o tiempo manual inexistente (o de otro piloto) |
| `409` | `sessionId` o id de tiempo manual usado por otro piloto; modificación concurrente. Dos subidas simultáneas del mismo piloto con el mismo id se resuelven como un reintento (`200`) |
| `422` | moto que no está en tu garaje, trazado inexistente, trazado sin meta o dibujo (solo GPS), sesión sin vueltas válidas (ideal, curvas, comparación) |
| `503` | circuits o garage no responden al subir, recalcular, apuntar o importar. La app debe reintentar la subida más tarde con el mismo id |

## 4. Persistencia

`V1__create_sessions.sql`:

- `riding_session`: snapshot del trazado (`layout_path`, `finish_line` en JSONB) y de la moto.
  También guarda desnormalizados `best_lap_ms`, `ideal_lap_ms` y `valid_lap_count`.
- `session_lap`: `sector_ms BIGINT[]`.
- `manual_lap_time` (`V2`): tiempos manuales, con el trazado y la moto congelados como en
  `riding_session` pero sin geometría. `lap_date DATE`, `lap_precision`, `time_source`,
  `other_laps_ms BIGINT[]`.
- `session_telemetry`: muestras en binario comprimido (`TelemetryCodec`, `ridei-bin-v1`, unos 21
  bytes por muestra). Una tanda de 30 min a 10 Hz ocupa ~380 KB.

Los ids de otros contextos se guardan sin FK. El análisis (trazas, curvas) no se persiste: se
recalcula desde la telemetría en cada petición, en milisegundos.

## 5. Limitaciones conocidas y siguientes pasos

- **Tanda libre y generación de trazado** (03–03G): todavía no están. Para cronometrar, el trazado
  necesita meta y dibujo en el catálogo; Calafat Principal los tiene (V8 de circuits).
- **"Usar como referencia"** (06): de momento la referencia del HUD es siempre el récord personal.
- **Sectores** fijos (4 iguales) hasta que circuits los defina por trazado.
- **Revocación del token**: sessions solo verifica la firma, como garage. La revocación online se
  aplica indirectamente al subir, porque circuits valida el token contra identity.
- La copia de la pantalla 01 ("Las sesiones se guardan en local. Nada sube a la nube") ya no es
  cierta con este backend. Hay que cambiarla en la app.
- **Tiempos manuales**: no se pueden editar (se borran y se apuntan de nuevo). 22B enseña
  "Récord del trazado", que es un récord entre pilotos: no existe en ningún backend todavía. Que no
  compitan en clasificaciones públicas no requiere nada mientras no existan esas clasificaciones.

## 6. Tiempos manuales (antes de Ridei)

Pantallas 22–22E. Tiempos de tandas pasadas, apuntados de uno en uno (22B) o importados pegando las
notas (22C). Un tiempo manual (`ManualLapTime`, agregado aparte de `Session`) va siempre a un
trazado y a una moto, como una sesión, y lleva su origen: `OFFICIAL_TIMING`, `LAPTIMER`,
`STOPWATCH` o `APPROXIMATE`. No tiene GPS, así que:

- cuenta para el historial (22E), la evolución y el **mejor tiempo manual** de `/records` (22D);
- **no** cuenta para el récord GPS, `personalBest`, la vuelta ideal (de sesión o histórica), los
  sectores ni la vuelta de referencia del HUD. Un manual más rápido que el récord GPS no se lo quita;
- vale para trazados **sin meta ni dibujo** (Calafat *Corto*): solo se comprueba que el trazado
  exista en el catálogo;
- la moto debe estar en el garaje del piloto, pero puede ser una que ya no tiene (`disposalDate`):
  garage las sigue devolviendo. "Añádela como anterior" (22B) se hace en garage.

Se guarda la **precisión** con que se apuntó (`SECONDS` 1:52 · `TENTHS` 1:49.8 · `HUNDREDTHS`
1:14.22 · `THOUSANDTHS` 1:45.300) para mostrar el tiempo sin inventar decimales. La fecha es un día
(`LocalDate`), no puede ser futura ni anterior a 1950. Entre 10 s y 60 min por vuelta.

### 6.1 API

| Método y ruta | Pantalla | Respuesta |
|---|---|---|
| `PUT /api/v1/sessions/manual-times/{id}` | 22B "Guardar tiempo" | `201` nuevo · `200` ya existía (idempotente, el id lo genera la app) |
| `POST /api/v1/sessions/manual-times/import/preview` | 22C detectar | líneas interpretadas; no guarda nada |
| `POST /api/v1/sessions/manual-times/batch` | 22C "Guardar 3 tiempos" | todos o ninguno; `201` si alguno es nuevo · `200` |
| `GET /api/v1/sessions/manual-times?layoutId=&motorbikeId=` | 22E "Antes de Ridei" · 22D historial | lista, fecha más reciente primero |
| `GET /api/v1/sessions/manual-times/{id}` | | detalle |
| `DELETE /api/v1/sessions/manual-times/{id}` | | `204` |

```http
PUT /api/v1/sessions/manual-times/5f0d6c1e-8a43-4f1b-9d7e-1c2b3a4d5e6f

{
  "venueId": "10000000-0000-4000-8000-000000000009",
  "layoutId": "30000000-0000-4000-8000-000000000006",
  "motorbikeId": "<id de la moto en garage>",
  "date": "2024-05-12",
  "bestLapMillis": 105300,
  "precision": "TENTHS",
  "source": "LAPTIMER",
  "lapCount": 12, "trackCondition": "DRY", "tyres": "Pirelli SC1",
  "otherLapsMillis": [106100, 107400], "notes": null
}
```

`precision` es opcional (si falta, la menos precisa compatible: 105300 → `TENTHS`). Todo lo que va
después de `source` es "Más datos · opcional". `otherLapsMillis` no puede tener vueltas más rápidas
que `bestLapMillis`, y `lapCount`, si se da, ha de cubrirlas. El lote es
`{"times": [{"id": "…", …mismos campos}]}`, como máximo 100.

Respuesta: `{id, venue:{id, name}, trackName, layout:{id, name}, motorbike, date, bestLapMillis,
precision, source, lapCount, trackCondition, tyres, otherLapsMillis, notes, createdAt}`.

### 6.2 Importar desde las notas

`POST …/import/preview` con `{"notes": "…"}`. El texto tiene como máximo 20 000 caracteres, 100
líneas y 30 circuitos distintos. Cada línea es una tanda con circuito, fecha y tiempo **en
cualquier orden**:

```
Calafat 12/05/24 1:45.3                 → Calafat · Velocidad · Principal, 12 may 2024, 1:45.3
Castellolí 2/6/24 1:14,22 (lluvia)      → … 2 jun 2024, 1:14.22, WET
Montmeló nacional 20-9-24 1.26.10       → … trazado Nacional, 20 sep 2024, 1:26.10
Alcarras 3/3/23 1:52                    → NEEDS_REVIEW: TRACK_AMBIGUOUS (3 pistas)
```

- **Fechas**: día/mes/año con `/`, `-` o `.`, año de 2 o 4 cifras (si las 2 cifras dan un año
  futuro, es del siglo pasado), o ISO `2024-05-12`. `12.05.24` también puede ser un tiempo: es
  fecha solo si no hay otra fecha y sí otro tiempo en la línea.
- **Tiempos**: `1:52`, `1:45.3`, `1:14,22`, `1.26.10`, `48.3` (karting). Con varios tiempos, el
  más rápido es la mejor vuelta y el resto `otherLapsMillis`.
- **Condiciones**: lluvia/mojado/pluja/wet → `WET`, húmedo/humit/damp → `DAMP`, seco/sec/dry →
  `DRY`, sueltas o entre paréntesis. El resto del paréntesis va a `notes`.
- **Circuito**: lo que queda con letras. Se busca en circuits (`GET /venues?q=`, con el token del
  piloto), primero el texto entero y luego sin la última palabra. Dentro del complejo,
  `CatalogMatcher` elige la pista y el trazado que nombre el texto. Si no nombra ninguno, coge la
  única pista y su trazado por defecto. Si hay varias pistas, no adivina.

Respuesta: `{detectedCount, readyCount, needsReviewCount, lines:[{lineNumber, text, status,
problems, circuitText, venue, trackName, layout, date, bestLapMillis, precision, otherLapsMillis,
trackCondition, notes, tracks}]}`. `status` es `READY` o `NEEDS_REVIEW`. `problems` puede traer
`MISSING_CIRCUIT`, `CIRCUIT_NOT_FOUND`, `TRACK_AMBIGUOUS`, `LAYOUT_AMBIGUOUS`, `NO_LAYOUTS`,
`MISSING_DATE`, `INVALID_DATE`, `MISSING_TIME` o `TIME_OUT_OF_RANGE`. Si hay que elegir pista o
trazado ("Elegir pista"), `tracks` trae las pistas del complejo con sus trazados
(`{name, discipline, layouts:[{id, name, isDefault}]}`).

La app enseña la lista, deja corregir lo dudoso y elegir la moto y el origen para todos ("Moto para
todos · origen"). Después guarda con `POST …/batch`.

