# Koog AI Agents

Kotlin samples exploring the [Koog AI agents framework](https://docs.koog.ai/) (v1.0.0) with local LLMs via LM Studio and Ollama.

## Stack

- Kotlin 2.3 / JVM 21 / Gradle 8.10
- `ai.koog:koog-agents:1.0.0`
- Ktor Client (CIO) + kotlinx.serialization
- LM Studio (primary) / Ollama as LLM backends

## Samples

All samples live in `src/main/kotlin/pl/wtopolski/koog/samples/`.

| File | Description | Key concepts |
|------|-------------|--------------|
| `Chat.kt` | Interactive chat loop with `/exit` to quit | `llm.writeSession`, `appendPrompt`, `requestLLM`, loop inside a single node |
| `Calc.kt` | Calculator agent with add/subtract tools | Annotation-based `@Tool` / `ToolSet`, `strategy` DSL, direct result parsing from `ReceivedToolResults` |
| `Structured.kt` | Weather forecast via structured LLM output | `executeStructured<T>`, `requestLLMStructured<T>`, `StructureFixingParser`, `@LLMDescription` annotations |
| `MiejskiBike.kt` | Interactive bike-points chat for [miejski.bike](https://miejski.bike) | Ktor HTTP tool, enum parameters, interactive loop, ASCII table output |
| `utils.kt` | Shared config and utilities | `LmStudioOpenAIClient` (Qwen reasoning fix), `lmStudioExecutor()`, `ollamaExecutor()`, `SayToUser` tool |

## Architecture

Koog agents follow a node-graph strategy pattern:

```
nodeStart → nodeLLMRequest → (text) → nodeFinish
                           → (tool calls) → nodeExecuteTools → nodeParseResult → nodeFinish
```

- **Strategy** — a directed graph of typed nodes connected by conditional edges
- **Nodes** — typed `Input → Output` units; built-in helpers: `nodeLLMRequest`, `nodeExecuteTools`
- **Edges** — `onTextMessage { }` / `onToolCalls { }` conditions select which path to take
- **Tools** — implement `Tool<Args, Result>` with a `ToolDescriptor`; or use `@Tool` annotations on a `ToolSet`
- **Structured output** — `requestLLMStructured<T>()` inside `llm.writeSession { }` asks the LLM to emit a JSON-schema-validated `T`

## Prerequisites

### LM Studio (primary backend)

1. Install [LM Studio](https://lmstudio.ai/) and load a model
2. Start the local API server (default: `http://localhost:1234`)
3. The samples use `qwen/qwen3.5-35b-a3b` — set in `utils.kt`:
   ```kotlin
   const val LM_STUDIO_MODEL_ID = "qwen/qwen3.5-35b-a3b"
   ```

### Ollama (alternative)

```bash
ollama pull gpt-oss:20b
```

Switch the active model in `utils.kt`:
```kotlin
val ollama_model = gpt_oss_20b
```

## Build & Run

```bash
./gradlew build
./gradlew test
```

Run samples from your IDE by executing the `main()` function in each file, or via Gradle application plugin with the fully-qualified class name:

| Sample | Main class |
|--------|-----------|
| Chat | `pl.wtopolski.koog.samples.ChatKt` |
| Calc | `pl.wtopolski.koog.samples.CalcKt` |
| Structured | `pl.wtopolski.koog.samples.StructuredKt` |
| MiejskiBike | `pl.wtopolski.koog.samples.MiejskiBikeKt` |

## MiejskiBike Chat

Interactive bike-points assistant for Polish cities. Queries the `storage.miejski.bike` API for real-time data.

**Supported zones:** `lodz`, `warszawa`, `poznan`  
**Supported types:** `rack`, `wrench`, `workshop`

```
    __  ____        _      __   _    ____  _ __           ________          __
   /  |/  (_)__    (_)____/ /__(_)  / __ )(_) /_____     / ____/ /_  ____ _/ /_
  / /|_/ / / _ \  / / ___/ //_/ /  / __  / / //_/ _ \   / /   / __ \/ __ `/ __/
 / /  / / /  __/ / (__  ) ,< / /  / /_/ / / ,< /  __/  / /___/ / / / /_/ / /_
/_/  /_/_/\___/_/ /____/_/|_/_/  /_____/_/_/|_|\___/   \____/_/ /_/\__,_/\__/

You: show me racks in warszawa

  > [miejski.bike] type=rack  zone=warszawa
  > [miejski.bike] 5 result(s) found

RACK in WARSZAWA — 5 point(s)
+---+------------------------------+------------+------------+
| # | Name                         | Latitude   | Longitude  |
+---+------------------------------+------------+------------+
| 1 | Stacja Veturilo Centrum      | 52.229676  | 21.012229  |
...
+---+------------------------------+------------+------------+

You: /exit
```

## Known Issues & Quirks

### Qwen reasoning content (`LmStudioOpenAIClient`)

LM Studio routes Qwen's thinking output into `reasoning_content` and leaves `content` empty. This breaks two things:

1. **Structured output** — Koog's parser calls `.single()` on `MessagePart.Text` and fails when there are no text parts. Fix: promote `reasoning_content` to a `Text` part when no text is present.
2. **Tool calls** — if reasoning is promoted to text unconditionally, messages containing both reasoning and tool calls get a `Text` part added, causing the `onTextMessage` edge to fire before `onToolCalls`. Fix: skip promotion when `MessagePart.Tool.Call` parts are already present.

Both fixes live in `LmStudioOpenAIClient.promoteReasoningToText()` in `utils.kt`.

### Direct result parsing vs. `requestLLMStructured`

`requestLLMStructured<T>` is suited for having the LLM *generate* structured data. When a tool already returns typed objects (e.g. `BikeResult`), pass the data through directly by mapping `ReceivedToolResults` in a custom node — letting the LLM reformat it risks truncation.
