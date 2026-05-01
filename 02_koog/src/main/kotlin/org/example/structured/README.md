# Structured

Sample response for: "What is the weather forecast for Skierniewice in Poland?"

println(structuredResponse)

```
Success(StructuredResponse(structure=SimpleWeatherForecast(location=Skierniewice, temperature=-5, conditions=Frosty with scattered snow showers), raw={
  "$schema": "http://json-schema.org/draft-07/schema#",
  "$id": "SimpleWeatherForecast",
  "$defs": {
    "SimpleWeatherForecast": {
      "type": "object",
      "description": "Simple weather forecast for a location",
      "properties": {
        "location": {
          "type": "string",
          "description": "Location name"
        },
        "temperature": {
          "type": "integer",
          "description": "Temperature in Celsius"
        },
        "conditions": {
          "type": "string",
          "description": "Weather conditions (e.g., sunny, cloudy, rainy)"
        }
      },
      "required": [
        "location",
        "temperature",
        "conditions"
      ]
    }
  },
  "$ref": "#/defs/SimpleWeatherForecast",
  "type": "object",
  "location": "Skierniewice",
  "temperature": -5,
  "conditions": "Frosty with scattered snow showers"
}))
```

println(structuredResponse.getOrNull()?.structure)

```kotlin
SimpleWeatherForecast(location=Skierniewice, temperature=-5, conditions=Frosty with scattered snow showers)
```