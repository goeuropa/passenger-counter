## Getting Started
- Setup environment var. in docker-compose.yml
- Create vehicleCapacities.json and set it as `volumes` in docker-compose.yml config
----------------------------------------------------------
### examples (required):
##### video-url: http://video.eu/
##### video-account: "api"
##### video-key: "key"
##### tc-url: https://tc.eu/api/v1/key/ 
##### key-agency: "key/agency/agency_id"
----------------------------------------------------------
## Build 
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
The app runs on port **8081**. Single endpoint: `GET /v1/busloads`.

## Architecture

Spring Boot 3.3.3 service that bridges two external systems to expose real-time bus occupancy data:

- **Video/APC system** (`RestClient` bean: `"video"`) — proprietary API for automatic passenger counting hardware on buses.
- **TC (Transit Control) system** (`RestClient` bean: `"tc"`) — transit management API that provides the list of active vehicle IDs.

### Data flow

1. `VehicleIdsScheduler` (daily + on startup) — fetches active vehicle list from TC, appends a configurable APC suffix (`-APC` or `-APC2` for vehicles whose name starts with the configured prefix), stores comma-joined string in `PeopleCountRepository`.
2. `JsessionScheduler` (daily + on startup) — authenticates with the video API and stores the `JSESSIONID` cookie in `PeopleCountRepository`.
3. `PeopleDetailScheduler` (every `api.poll-rate-ms` ms, default 15 s) — polls passenger counts for all known vehicle IDs using the stored JSESSION. Maps raw `IncomeInfoDto` → `BusLoadDto`, calculating occupancy % using capacity divisors from `vehicleCapacities.json`. If the session has expired (response `result == 5`), it immediately calls `JsessionScheduler.getJSessionId()` to renew.
4. `CounterService.getCountDetailsWithTimeCheck()` — on each API request, purges stale entries (older than `api.stale-ttl-minutes` minutes or occupancy > `api.max-fullness-percent`%) and appends a `"time"` sentinel entry.

### Key design points

- **`PeopleCountRepository`** is a **manual singleton** (`getInstance()`), not a Spring bean. It holds all shared mutable state: vehicle ID set, JSESSION string, and the live `ConcurrentHashMap<String, BusLoadDto>` of occupancy records.
- **`PeopleCountRepository.CAPACITY_CONFIGS`** and **`SUFFIX_STRIP_REGEX`** are static (non-final) fields. They are populated at startup by `AppConfig.initRepository()` (`@PostConstruct`) once the Spring context is ready — do **not** access them before Spring initialization completes.
- **`CapacitiesConfig`** is a Spring `@Component`. It receives the file path via `@Value("${api.capacities-file}")` and loads it via `ResourceLoader`, which supports both `file:` and `classpath:` prefixes. In production the path is `file:/app/resources/vehicleCapacities.json`; in tests it is `classpath:vehicleCapacities.json`.
- **`vehicleCapacities.json`** structure: `{ "vehicles": { "<divisor>": ["vehicleId", ...] } }`. The divisor is the total seat capacity used to compute `currentFullness = (currentCount / divisor) * 100`.
- Scheduling is **disabled in the `test` profile** via `@Profile("!test")` on `SchedulerConfig`. Tests use `application-test.yml` with blank external URLs.

### Configuration (`application.yml`)

All operational parameters live under the `api.*` prefix and are bound to `ApiProperties`:

| Property | Default | Purpose |
|---|---|---|
| `api.capacities-file` | `file:/app/resources/vehicleCapacities.json` | Path to capacity config (supports `file:` / `classpath:`) |
| `api.apc-suffix` | `-APC` | Suffix appended to most vehicle IDs |
| `api.apc2-suffix` | `-APC2` | Suffix appended to vehicles matching the prefix below |
| `api.apc2-vehicle-prefix` | `ME` | Vehicle name prefix that triggers the APC2 suffix |
| `api.excluded-vehicle-ids` | `[ME-8-APC2]` | Vehicle IDs removed after suffix assignment |
| `api.stale-ttl-minutes` | `35` | Minutes before a bus record is considered stale |
| `api.max-fullness-percent` | `150` | Fullness % above which a record is purged |
| `api.poll-rate-ms` | `15000` | Scheduler polling interval in milliseconds |
| `api.login-path` | `StandardApiAction_login.action` | APC login endpoint path |
| `api.people-detail-path` | `PeopleAction_peopleDetail.action` | APC passenger-count endpoint path |
| `api.page-records` | `10000` | Page size passed to the APC API |

### Environment variables (required at runtime)

| Variable | Purpose |
|---|---|
| `APC_URL` | Base URL of the video/APC system |
| `ACCOUNT` | Login account for the video system |
| `APC_KEY` | Password/key for the video system |
| `TC_URL` | Base URL of the TC system |
| `KEY-AGENCY` | Path segment for the TC API (e.g. `key/agency/agency_id`) |
| `TZ` | Timezone for the container |

`vehicleCapacities.json` must be mounted into the container at `/app/resources/vehicleCapacities.json` (see `docker-compose.yml` volumes).
