# Koog AI Agents

Kotlin samples exploring the [Koog AI agents framework](https://docs.koog.ai/) (v0.3.0) with local LLMs via Ollama and LM Studio.

## Stack

- Kotlin 2.1 / JVM 23 / Gradle 8.10
- Koog Agents 0.3.0
- Ktor Client (CIO) + kotlinx.serialization
- Ollama / LM Studio as LLM backends

## Samples

| Package | Description | Key concepts |
|---------|-------------|--------------|
| `basic` | Minimal agent — "Hello, how can you help me?" | `AIAgent`, `SayToUser` tool, `simpleOllamaAIExecutor` |
| `calc` | Calculator with add/subtract tools and structured output | Custom `Tool<Args, Result>`, `strategy` DSL, `requestLLMStructured`, node graph with edges |
| `structured` | Weather forecast via structured LLM output (no agent) | `JsonStructuredData`, `executeStructured`, JSON schema generation |
| `structured_strategy` | Weather forecast via agent strategy + structured output | Agent strategy with `requestLLMStructured` inside a custom node |
| `miejskibike` | Bike location agent querying miejski.bike API | HTTP tool with Ktor client, enum parameters, structured response parsing |
| `lmstudio` | Agent using LM Studio backend via custom `LLMClient` | Custom `LLMClient` implementation, ngrok tunneling, OpenAI-compatible API |

## Prerequisites

- [Ollama](https://ollama.com/) running locally with models pulled:
  ```
  ollama pull llama3.2
  ollama pull gpt-oss:20b
  ```
- For the `lmstudio` sample: [LM Studio](https://lmstudio.ai/) with a model loaded and API server running

## Build & Run

```bash
./gradlew build
./gradlew test
```

Each sample has its own `main()`. Run a specific one via:
```bash
./gradlew run -PmainClass=org.example.basic.MainKt
./gradlew run -PmainClass=org.example.calc.MainKt
./gradlew run -PmainClass=org.example.miejskibike.MainKt
./gradlew run -PmainClass=org.example.structured.MainKt
./gradlew run -PmainClass=org.example.structured_strategy.MainKt
./gradlew run -PmainClass=org.example.lmstudio.MainKt
```

## Configuration

The active Ollama model is set globally in `src/main/kotlin/org/example/common/utils.kt`:
```kotlin
val ollama_model = gpt_oss_20b // switch to llama3_2 etc.
```
