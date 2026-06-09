package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class App {
    public String getGreeting() {
        return "Hello MCP Server World!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }

    public void setupMCPServer() {

        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(new ObjectMapper());

        // Create a server with custom configuration
        McpSyncServer syncServer = McpServer.sync(transportProvider)
                .serverInfo("my-server", "1.0.0")
                .capabilities(ServerCapabilities.builder()
                        .resources(false, true) // Enable resource support
                        .tools(true) // Enable tool support
                        .prompts(true) // Enable prompt support
                        .logging() // Enable logging support
                        .completions() // Enable completions support
                        .build())
                .build();

        // Register tools, resources, and prompts
        // SyncToolSpecification syncToolSpecification = getSyncToolSpecification();
        // syncServer.addTool(syncToolSpecification);
        // syncServer.addResource(syncResourceSpecification);
        // syncServer.addPrompt(syncPromptSpecification);

        // Close the server when done
        syncServer.close();
    }

    // private SyncToolSpecification getSyncToolSpecification() {
    //     // Sync tool specification
    //     var schema = """
    //             {
    //               "type" : "object",
    //               "id" : "urn:jsonschema:Operation",
    //               "properties" : {
    //                 "operation" : {
    //                   "type" : "string"
    //                 },
    //                 "a" : {
    //                   "type" : "number"
    //                 },
    //                 "b" : {
    //                   "type" : "number"
    //                 }
    //               }
    //             }
    //             """;
    //     SyncToolSpecification syncToolSpecification = new McpServerFeatures.SyncToolSpecification(
    //             new Tool("calculator", "Basic calculator", schema),
    //             (exchange, arguments) -> {
    //                 // Tool implementation
    //                 return new CallToolResult(result, false);
    //             });

    //     return syncToolSpecification;
    // }

}
