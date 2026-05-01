# Tool

```
On Before Agent Started: AgentStartContext(agent=ai.koog.agents.core.agent.AIAgent@1eb1984e, runId=159293a2-3360-447c-a1f0-69c26f7fc60b, strategy=ai.koog.agents.core.agent.entity.AIAgentStrategy@89c3c9, feature=ai.koog.agents.features.eventHandler.feature.EventHandler@22ccfba3, context=ai.koog.agents.core.agent.context.AIAgentContext@2e4079f)

On Before Agent Started: AgentStartContext(agent=ai.koog.agents.core.agent.AIAgent@1eb1984e, runId=159293a2-3360-447c-a1f0-69c26f7fc60b, strategy=ai.koog.agents.core.agent.entity.AIAgentStrategy@89c3c9, feature=ai.koog.agents.features.eventHandler.feature.EventHandler@22ccfba3, context=ai.koog.agents.core.agent.context.AIAgentContext@2e4079f)

On Before Node: NodeBeforeExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.StartNode@5e403b4a, input=subtract one from nine, inputType=kotlin.String)

On After Node: NodeAfterExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.StartNode@5e403b4a, input=subtract one from nine, output=subtract one from nine, inputType=kotlin.String, outputType=kotlin.String)

On Before Node: NodeBeforeExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@5117dd67, input=subtract one from nine, inputType=kotlin.String)

On After LLM Call: AfterLLMCallContext(runId=159293a2-3360-447c-a1f0-69c26f7fc60b, prompt=Prompt(messages=[System(content=You are a simple calculator assistant.
You can add and subtract two numbers together using the calculator tool.
When the user provides input, extract the numbers they want to add.
The input might be in various formats like "add 5 and 7", "5 + 7", or just "5 7".
Extract the two numbers and use the calculator tool to add or subtract them.
Always respond with a clear, friendly message showing the calculation and result., metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:26:11.985742Z)), User(content=subtract one from nine, metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:26:12.389927Z), attachments=[])], id=simple-calculator, params=LLMParams(temperature=null, numberOfChoices=null, speculation=null, schema=null, toolChoice=null, user=null)), model=LLModel(provider=Ollama, id=gpt-oss:20b, capabilities=[Temperature, Simple, Tools]), tools=[ToolDescriptor(name=calculator_plus, description=Add two numbers together, requiredParameters=[ToolParameterDescriptor(name=num1, description=First number to add, type=Integer), ToolParameterDescriptor(name=num2, description=Second number to add, type=Integer)], optionalParameters=[]), ToolDescriptor(name=calculator_minus, description=Subtract two numbers, requiredParameters=[ToolParameterDescriptor(name=num1, description=First number to subtract, type=Integer), ToolParameterDescriptor(name=num2, description=Second number to subtract, type=Integer)], optionalParameters=[])], responses=[Call(id=ollama_tool_call_1671075908, tool=calculator_minus, content={"num1":9,"num2":1}, metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:25.164847Z, totalTokensCount=345, inputTokensCount=282, outputTokensCount=63, additionalInfo={}))], moderationResponse=null)

On After Node: NodeAfterExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@5117dd67, input=subtract one from nine, output=Call(id=ollama_tool_call_1671075908, tool=calculator_minus, content={"num1":9,"num2":1}, metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:25.164847Z, totalTokensCount=345, inputTokensCount=282, outputTokensCount=63, additionalInfo={})), inputType=kotlin.String, outputType=ai.koog.prompt.message.Message.Response)

On Before Node: NodeBeforeExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@4ccfc7a8, input=Call(id=ollama_tool_call_1671075908, tool=calculator_minus, content={"num1":9,"num2":1}, metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:25.164847Z, totalTokensCount=345, inputTokensCount=282, outputTokensCount=63, additionalInfo={})), inputType=ai.koog.prompt.message.Message.Tool.Call)

On Tool Call: ToolCallContext(runId=159293a2-3360-447c-a1f0-69c26f7fc60b, toolCallId=ollama_tool_call_1671075908, tool=org.example.calc.CalculatorSubtractTool@1ead6023, toolArgs=CalcArgs(num1=9, num2=1))

Perform a simple subtract operation
On After Node: NodeAfterExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@4ccfc7a8, input=Call(id=ollama_tool_call_1671075908, tool=calculator_minus, content={"num1":9,"num2":1}, metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:25.164847Z, totalTokensCount=345, inputTokensCount=282, outputTokensCount=63, additionalInfo={})), output=ReceivedToolResult(id=ollama_tool_call_1671075908, tool=calculator_minus, content=The result is: 8, result=Result(sum=8)), inputType=ai.koog.prompt.message.Message.Tool.Call, outputType=ai.koog.agents.core.environment.ReceivedToolResult)

On Before Node: NodeBeforeExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@7020f3ba, input=ReceivedToolResult(id=ollama_tool_call_1671075908, tool=calculator_minus, content=The result is: 8, result=Result(sum=8)), inputType=ai.koog.agents.core.environment.ReceivedToolResult)

On After LLM Call: AfterLLMCallContext(runId=159293a2-3360-447c-a1f0-69c26f7fc60b, prompt=Prompt(messages=[System(content=You are a simple calculator assistant.
You can add and subtract two numbers together using the calculator tool.
When the user provides input, extract the numbers they want to add.
The input might be in various formats like "add 5 and 7", "5 + 7", or just "5 7".
Extract the two numbers and use the calculator tool to add or subtract them.
Always respond with a clear, friendly message showing the calculation and result., metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:26:11.985742Z)), User(content=subtract one from nine, metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:26:12.389927Z), attachments=[]), Call(id=ollama_tool_call_1671075908, tool=calculator_minus, content={"num1":9,"num2":1}, metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:25.164847Z, totalTokensCount=345, inputTokensCount=282, outputTokensCount=63, additionalInfo={})), Result(id=ollama_tool_call_1671075908, tool=calculator_minus, content=The result is: 8, metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:27:25.208279Z))], id=simple-calculator, params=LLMParams(temperature=null, numberOfChoices=null, speculation=null, schema=null, toolChoice=null, user=null)), model=LLModel(provider=Ollama, id=gpt-oss:20b, capabilities=[Temperature, Simple, Tools]), tools=[ToolDescriptor(name=calculator_plus, description=Add two numbers together, requiredParameters=[ToolParameterDescriptor(name=num1, description=First number to add, type=Integer), ToolParameterDescriptor(name=num2, description=Second number to add, type=Integer)], optionalParameters=[]), ToolDescriptor(name=calculator_minus, description=Subtract two numbers, requiredParameters=[ToolParameterDescriptor(name=num1, description=First number to subtract, type=Integer), ToolParameterDescriptor(name=num2, description=Second number to subtract, type=Integer)], optionalParameters=[])], responses=[Assistant(content=Sure thing!  
9 − 1 = **8**  
Let me know if you’d like to do another calculation., metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:41.194302Z, totalTokensCount=355, inputTokensCount=323, outputTokensCount=32, additionalInfo={}), attachments=[], finishReason=null)], moderationResponse=null)

On After Node: NodeAfterExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@7020f3ba, input=ReceivedToolResult(id=ollama_tool_call_1671075908, tool=calculator_minus, content=The result is: 8, result=Result(sum=8)), output=Assistant(content=Sure thing!  
9 − 1 = **8**  
Let me know if you’d like to do another calculation., metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:41.194302Z, totalTokensCount=355, inputTokensCount=323, outputTokensCount=32, additionalInfo={}), attachments=[], finishReason=null), inputType=ai.koog.agents.core.environment.ReceivedToolResult, outputType=ai.koog.prompt.message.Message.Response)

On Before Node: NodeBeforeExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@57851151, input=Assistant(content=Sure thing!  
9 − 1 = **8**  
Let me know if you’d like to do another calculation., metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:41.194302Z, totalTokensCount=355, inputTokensCount=323, outputTokensCount=32, additionalInfo={}), attachments=[], finishReason=null), inputType=ai.koog.prompt.message.Message.Response)

On After LLM Call: AfterLLMCallContext(runId=159293a2-3360-447c-a1f0-69c26f7fc60b, prompt=Prompt(messages=[System(content=You are a simple calculator assistant.
You can add and subtract two numbers together using the calculator tool.
When the user provides input, extract the numbers they want to add.
The input might be in various formats like "add 5 and 7", "5 + 7", or just "5 7".
Extract the two numbers and use the calculator tool to add or subtract them.
Always respond with a clear, friendly message showing the calculation and result., metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:26:11.985742Z)), User(content=subtract one from nine, metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:26:12.389927Z), attachments=[]), Assistant(content={"tool_call_id":"ollama_tool_call_1671075908","tool_name":"calculator_minus","tool_args":{"num1":9,"num2":1}}, metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:25.164847Z, totalTokensCount=345, inputTokensCount=282, outputTokensCount=63, additionalInfo={}), attachments=[], finishReason=null), User(content={"tool_call_id":"ollama_tool_call_1671075908","tool_name":"calculator_minus","tool_result":"The result is: 8"}, metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:27:25.208279Z), attachments=[]), Assistant(content=Sure thing!  
9 − 1 = **8**  
Let me know if you’d like to do another calculation., metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:41.194302Z, totalTokensCount=355, inputTokensCount=323, outputTokensCount=32, additionalInfo={}), attachments=[], finishReason=null), User(content=## NEXT MESSAGE OUTPUT FORMAT
The output in the next message MUST ADHERE TO CalcOperation format.
DEFINITION OF CalcOperation
The CalcOperation format is defined only and solely with JSON, without any additional characters, backticks or anything similar.
You must adhere to the following JSON schema:
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "$id": "CalcOperation",
  "$defs": {
    "SimpleWeatherForecast": {
      "type": "object",
      "description": "Simple weather forecast for a location",
      "properties": {
        "num1": {
          "type": "integer",
          "description": "First number of operation"
        },
        "num2": {
          "type": "integer",
          "description": "Second number of operation"
        },
        "result": {
          "type": "integer",
          "description": "The result of operation"
        }
      },
      "required": [
        "num1",
        "num2",
        "result"
      ]
    }
  },
  "$ref": "#/defs/SimpleWeatherForecast",
  "type": "object"
}
Here are some examples of valid responses:
{
  "num1": 1,
  "num2": 3,
  "result": 4
}
{
  "num1": 5,
  "num2": 5,
  "result": 10
}
, metaInfo=RequestMetaInfo(timestamp=2025-08-23T14:27:41.209230Z), attachments=[])], id=simple-calculator, params=LLMParams(temperature=null, numberOfChoices=null, speculation=null, schema=null, toolChoice=null, user=null)), model=LLModel(provider=Ollama, id=gpt-oss:20b, capabilities=[Temperature, Simple, Tools]), tools=[], responses=[Assistant(content={
  "num1": 9,
  "num2": 1,
  "result": 8
}, metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:30:50.214616Z, totalTokensCount=809, inputTokensCount=583, outputTokensCount=226, additionalInfo={}), attachments=[], finishReason=null)], moderationResponse=null)

On After Node: NodeAfterExecuteContext(context=ai.koog.agents.core.agent.context.AIAgentContext@265a2230, node=ai.koog.agents.core.agent.entity.AIAgentNode@57851151, input=Assistant(content=Sure thing!  
9 − 1 = **8**  
Let me know if you’d like to do another calculation., metaInfo=ResponseMetaInfo(timestamp=2025-08-23T14:27:41.194302Z, totalTokensCount=355, inputTokensCount=323, outputTokensCount=32, additionalInfo={}), attachments=[], finishReason=null), output=CalcOperation(num1=9, num2=1, result=8), inputType=ai.koog.prompt.message.Message.Response, outputType=org.example.calc.CalcOperation)

On Agent Finished: AgentFinishedContext(agentId=3be59655-0d71-4c46-a353-4a0c161770ae, runId=159293a2-3360-447c-a1f0-69c26f7fc60b, result=CalcOperation(num1=9, num2=1, result=8), resultType=kotlin.Any)

Input: 9
Input: 1
Result: 8
```