## Getting Started
- Setup environment var. in docker-compose.yml
- Create vehicleCapacities.json and set it as `volumes` in docker-compose.yml config
----------------------------------------------------------
### Examples:
###### security:
###### user: "user"
###### password: "password"
####### If would like to use source of data from file not from REST api
######  path-to-json-counts-file: "./outer.json"
######  interval-to-upload-json-file: "30"
#######For dev only
###### name-mapping: "5:8"
----------------------------------------------------------
## Build Docker
```shell
./gradlew assemble
```

## Run
Requirement:
- `docker` - installed.

Command:
```shell
docker compose up -d
```
## Commands

```shell
# Build
./gradlew build

# Run tests
./gradlew test

# Run app
./gradlew bootRun

```

The app runs on port 8080. Check health: `http://localhost:8080/v1/busloads` (requires Basic Auth).

## Architecture

Spring Boot 3.x REST API that collects Bluetooth-based passenger counts from two types of devices and exposes per-vehicle occupancy as a percentage.

### Two data-source pipelines

**v1 — Raspberry Pi sniffer** (`POST /v1/upload-json`, Basic Auth required):
1. Pi posts newline-delimited JSON journald logs; each line is a `LogEntryDto` with `__REALTIME_TIMESTAMP` (microseconds) and `MESSAGE`
2. `MESSAGE` format: `"<vehicleName> <count>"` — parsed in `BusLoadDto.parseMessage()` via regex
3. Controller buffers all lines, hands them to `CounterService.asyncParseJsonFile()` (`@Async`), which keeps only the newest entry by timestamp
4. Result is stored in `PeopleCountRepository.updatesAboutLoads` (keyed by vehicle name)

**v2 — Arduino/ESP32 sniffer** (`POST /v2/devices`, **no auth required**):
1. Device posts a structured `ArduinoDeviceSnapshotDto` (device_id, timestamp, devices list, summary)
2. `CounterService.saveDeviceSnapshot()` persists it to both `deviceSnapshots` and `updatesAboutLoads` maps
3. `currentCount` for v2 is taken from `summary.phones` (only phone-category BT devices counted)

Both pipelines produce a `BusLoadDto` and land in the same `updatesAboutLoads` map, so `GET /v1/busloads` shows data from both sources merged.

### Occupancy calculation

`BusLoadDto.getFullness()` looks up the vehicle name in `CAPACITY_CONFIGS` (loaded from `vehicleCapacities.json`) and computes `(currentCount / capacity) * 100`. Returns `Float.NaN` if the vehicle is not in the config.

`vehicleCapacities.json` structure — key is the integer capacity divisor, value is a list of vehicle names:
```json
{"vehicles": {"133": ["vehicle_name"], "56": ["other_name"]}}
```

### Storage and eviction

`PeopleCountRepository` is a manual singleton (`ConcurrentHashMap`). `GET /v1/busloads` injects a `"time"` sentinel entry on every call and evicts records older than 30 minutes or with `currentFullness > 150`.

### Security

- `POST /v2/devices` — public (no auth)
- All other endpoints — HTTP Basic Auth required (`api.security.user` / `api.security.password`)

### Async executor

`AsyncConfig` provides a `ThreadPoolTaskExecutor` (4 core threads, 10 max, queue capacity 50, `CallerRunsPolicy` on rejection) for all `@Async` methods.

### Optional file-based mode

When `api.path-to-json-counts-file` is set, `CountsFromFileScheduler` polls that file every N seconds (default 15) via `ScheduleTasksService`. Both classes are `@ConditionalOnProperty` gated on that property.

## Key files

| File | Purpose                                                                                               |
|------|-------------------------------------------------------------------------------------------------------|
| `src/main/resources/application.yml` | All config; `api.name-mapping` (`oldName:newName`) is dev-only vehicle ID remapping                   |
| `src/main/resources/vehicleCapacities.json` | Vehicle name → capacity divisor mapping (read from app/resources for ability to mapping in container) |
| `docker-compose.yml` | Volume-mounts the JSON config and sets env vars                                                       |
| `src/test/resources/application-test.yml` | Test profile — stubs out upstream URLs                                                                |
