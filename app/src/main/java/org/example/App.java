package org.example;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class App {

    private McpSyncServer syncServer;
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Hello MCP-Server World!");

        App app = new App();
        app.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.shutdownServer();
        }));
    }

    public void startServer() {
        log.info("Starting Server");

        // Create process-based transport using stdin/stdout
        McpJsonMapper jsonMapper = new JacksonMcpJsonMapperSupplier().get();
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(jsonMapper);

        // Create a server with custom configuration
        syncServer = McpServer.sync(transportProvider)
                .serverInfo("my-server", "1.0.0")
                .capabilities(ServerCapabilities.builder()
                        .resources(false, true) // Resource support: subscribe=false, listChanged=true
                        .tools(true) // Enable tool support with list changes
                        .prompts(true) // Enable prompt support with list changes
                        .completions() // Enable completions support
                        .logging() // Enable logging support
                        .build())
                .build();

        // Register tools, resources, and prompts
        syncServer.addTool(createToolSpec());
        syncServer.addResource(createResourceSpec());
        syncServer.addPrompt(createPromptSpec());

    }

    public void shutdownServer() {
        log.info("Shutting Down Server");
        if (syncServer != null)
            syncServer.close();
    }

    private SyncToolSpecification createToolSpec() {

        JsonSchema inputSchema = new JsonSchema(
                "object",
                Map.of(
                        "operation", Map.of(
                                "type", "string",
                                "description", "The arithmetic operation to perform",
                                "enum", List.of("add", "subtract", "multiply", "divide")),
                        "a", Map.of(
                                "type", "integer",
                                "description", "The first operand"),
                        "b", Map.of(
                                "type", "integer",
                                "description", "The second operand")),
                List.of("operation", "a", "b"),
                false,
                null,
                null);

        SyncToolSpecification syncToolSpecification = SyncToolSpecification.builder()
                .tool(Tool.builder()
                        .name("calculator")
                        .description("Basic calculator")
                        .inputSchema(inputSchema)
                        .build())
                .callHandler((exchange, request) -> {
                    // Access arguments via request.arguments()
                    String operation = (String) request.arguments().get("operation");
                    int a = (int) request.arguments().get("a");
                    int b = (int) request.arguments().get("b");
                    // Tool implementation
                    String result = getResult(operation, a, b);

                    return CallToolResult.builder()
                            .content(List.of(new McpSchema.TextContent("Result: " + result)))
                            .build();
                })
                .build();
        return syncToolSpecification;
    }

    private String getResult(String operation, int a, int b) {
    return switch (operation.toLowerCase()) {
        case "add"      -> String.valueOf(a + b);
        case "subtract" -> String.valueOf(a - b);
        case "multiply" -> String.valueOf(a * b);
        case "divide"   -> b == 0 ? "Error: Division by zero" : String.valueOf(a / b);
        default         -> "Error: Invalid operation";
    };
}

    private SyncResourceSpecification createResourceSpec() {
        SyncResourceSpecification syncResourceSpecification = new McpServerFeatures.SyncResourceSpecification(
                Resource.builder()
                        .uri("custom://resource")
                        .name("name")
                        .description("description")
                        .mimeType("text/plain")
                        .build(),
                (exchange, request) -> {
                    // Resource read implementation
                    return new ReadResourceResult(null);
                });
        return syncResourceSpecification;
    }

    private SyncPromptSpecification createPromptSpec() {
        SyncPromptSpecification syncPromptSpecification = new McpServerFeatures.SyncPromptSpecification(
                new Prompt("greeting", "description", List.of(
                        new PromptArgument("name", "description", true))),
                (exchange, request) -> {
                    // Prompt implementation
                    return new GetPromptResult(null, null);
                });
        return syncPromptSpecification;
    }

}
