package com.learningspring.hogwartsartifactonline.client.ai.chat;

import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatRequest;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GroqChatClient implements ChatClient {

    private final RestClient restClient;

    public GroqChatClient(
            @Value("${ai.groq.endpoint}") String endpoint,
            @Value("${ai.groq.api-key}") String apiKey,
            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(endpoint)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public ChatResponse generate(ChatRequest chatRequest) {
        return this.restClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(chatRequest)
                .retrieve()
                .body(ChatResponse.class);
    }

}
