# CLAUDE.md

This file provides guidance to Claude Code when working with code in this project.

## Overview

Kotlin project exploring the [Koog AI agents framework](https://docs.koog.ai/) with local LLMs (Ollama, LM Studio). Part of the `afterhours` repository — project `02_koog`.

## Build & Run

```bash
./gradlew build
./gradlew test
```

Each sample has its own `main()` in a separate package under `src/main/kotlin/org/example/`. Run a specific sample with:
```bash
./gradlew run -PmainClass=org.example.<package>.MainKt
```

## Project Structure

```
src/main/kotlin/org/example/
├── common/utils.kt              # Shared model config (ollama_model) and event handler
├── basic/Main.kt                # Minimal agent example
├── calc/Main.kt                 # Calculator agent with custom tools + structured output
├── structured/Main.kt, model.kt # Structured LLM output (no agent, direct executor)
├── structured_strategy/Main.kt  # Structured output via agent strategy
├── miejskibike/                 # Bike location agent (miejski.bike API tool)
└── lmstudio/                    # Custom LLMClient for LM Studio backend
```

## Architecture

- **Kotlin 2.1, JVM 23, Gradle 8.10** with kotlinx.serialization
- Key dependency: `ai.koog:koog-agents:0.3.0`
- All Ollama samples share the model defined in `common/utils.kt` — change `ollama_model` to switch models
- Koog pattern: define `strategy` (node graph), configure `AIAgentConfig` (prompt + model), register tools in `ToolRegistry`, create `AIAgent`
- Custom tools extend `Tool<Args, Result>` with `ToolDescriptor` and `execute()`
- Structured output uses `JsonStructuredData.createJsonStructure<T>()` with `requestLLMStructured()`

## Prerequisites

Requires Ollama running locally with models pulled (`ollama pull llama3.2`, `ollama pull gpt-oss:20b`).

## Known Issues

- `lmstudio/Main.kt` has a hardcoded ngrok URL — should be parameterized
- `miejskibike/MiejskiBikeTool.kt` has a placeholder Authorization header — should use env var
