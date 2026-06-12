# miejski-bike MCP Server

A [Model Context Protocol](https://modelcontextprotocol.io/) server, written in Kotlin, that exposes [miejski.bike](https://miejski.bike) data (Polish public bike-share zones, racks, and POI types) as tools an MCP-compatible client (e.g. Claude) can call.

The server communicates over **stdio** and is distributed as a self-contained fat JAR.

## Exposed tools

| Tool | Description | Arguments |
|---|---|---|
| `get_zones` | Returns all available miejski.bike zones (city/area ids and display names). | – |
| `get_zone_types` | Returns the POI types available within a given zone (e.g. `rack`, `wrench`, `workshop`). | `zoneId` (string) |
| `get_bike_points` | Returns up to 10 bike rack points (name + GPS) for a given zone. | `zoneId` (string, e.g. `poznan`, `warszawa`, `wroclaw`) |

## Requirements

- JDK 21+
- Gradle (the included `./gradlew` wrapper works out of the box)

## Build

Build the fat JAR (includes all dependencies):

```bash
./gradlew fatJar
```

The artifact is written to:

```
build/libs/003-kotlin-mcp-1.0-SNAPSHOT-all.jar
```

## Configuration

The server requires one environment variable:

| Variable | Description |
|---|---|
| `MIEJSKI_BIKE_API_TOKEN` | Bearer token for the miejski.bike REST API. The server exits with a clear error if this is not set. |

## Run standalone

```bash
MIEJSKI_BIKE_API_TOKEN=<your-token> java -jar build/libs/003-kotlin-mcp-1.0-SNAPSHOT-all.jar
```

The process speaks MCP over stdin/stdout — it is meant to be launched by an MCP client, not used interactively.

## Register with Claude

After building the fat JAR, register the server with Claude and pass the API token via `--env`. Run this command from the project root so that `$(pwd)` resolves to the correct location:

```bash
claude mcp add --scope user --transport stdio miejski-bike \
  -e MIEJSKI_BIKE_API_TOKEN=<your-token> \
  -- java -jar "$(pwd)/build/libs/003-kotlin-mcp-1.0-SNAPSHOT-all.jar"
```

Claude stores the environment variable alongside the server configuration so the token is injected automatically every time the server starts.

Once registered, the tools above become available in Claude as `mcp__miejski-bike__get_zones`, `mcp__miejski-bike__get_zone_types`, and `mcp__miejski-bike__get_bike_points`.

## Project layout

```
src/main/kotlin/technical/thursdays/
└── main.kt    # MCP server setup, tool definitions, HTTP client, data classes
```

## Tech stack

- Kotlin 2.1 / JVM 21
- [kotlin-sdk](https://github.com/modelcontextprotocol/kotlin-sdk) for MCP
- Ktor CIO client + kotlinx.serialization for the miejski.bike REST API
