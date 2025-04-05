package com.taskmanager.application.port.outbound.asistance;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("removal")
public class ChatLanguageModelMock implements ChatLanguageModel {

    private static final String MESSAGE = "Hello, I'm Stub. How can I help you?";

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        return ChatResponse.builder()
                .aiMessage(AiMessage.aiMessage(MESSAGE))
                .build();
    }

    @Override
    public String chat(String userMessage) {
        return MESSAGE;
    }

    @Override
    public ChatResponse chat(ChatMessage... messages) {
        return ChatResponse.builder()
                .aiMessage(AiMessage.aiMessage(MESSAGE))
                .build();
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return ChatResponse.builder()
                .aiMessage(AiMessage.aiMessage(MESSAGE))
                .build();
    }

    @Override
    public List<ChatModelListener> listeners() {
        return Collections.emptyList();
    }

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        return ChatResponse.builder()
                .aiMessage(AiMessage.aiMessage(MESSAGE))
                .build();
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return ChatRequestParameters.builder().build();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return Collections.emptySet();
    }

    @Override
    public String generate(String userMessage) {
        return MESSAGE;
    }

    @Override
    public Response<AiMessage> generate(ChatMessage... messages) {
        return Response.from(AiMessage.aiMessage(MESSAGE));
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> list) {
        return Response.from(AiMessage.aiMessage(MESSAGE));
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
        return Response.from(AiMessage.aiMessage(MESSAGE));
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
        return Response.from(AiMessage.aiMessage(MESSAGE));
    }
}
