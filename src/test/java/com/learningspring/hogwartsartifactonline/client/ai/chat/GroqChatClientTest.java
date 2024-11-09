package com.learningspring.hogwartsartifactonline.client.ai.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatRequest;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatResponse;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.Choice;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(GroqChatClient.class)
class GroqChatClientTest {

    @Autowired
    private GroqChatClient groqChatClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private String url;

    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        this.url = "https://api.groq.com/openai/v1/chat/completions";

        this.chatRequest = new ChatRequest("llama3-8b-8192", List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", "A json array.")
        ));
    }

    @Test
    void testGenerateSuccess() throws JsonProcessingException {
        // Given
        ChatResponse chatResponse = new ChatResponse(
                List.of(new Choice(0, new Message("assistant", "The summary includes six artifacts, owned by three different wizards")))
        );

        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", startsWith("Bearer ")))
                .andExpect(content().json(this.objectMapper.writeValueAsString(this.chatRequest)))
                .andRespond(withSuccess(this.objectMapper.writeValueAsString(chatResponse), MediaType.APPLICATION_JSON));

        // When
        ChatResponse generatedChatResponse = this.groqChatClient.generate(this.chatRequest);

        // Then
        this.mockServer.verify(); // Verify that all expected requests set up via expect and andExpect were indeed performed
        assertThat(generatedChatResponse.choices().get(0).message().content()).isEqualTo("The summary includes six artifacts, owned by three different wizards");
    }

    @Test
    void testGenerateUnauthorizedRequest() {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withUnauthorizedRequest());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.groqChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockServer.verify();
        assertThat(thrown).isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void testGenerateQuotaExceeded() {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withTooManyRequests());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.groqChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockServer.verify();
        assertThat(thrown).isInstanceOf(HttpClientErrorException.TooManyRequests.class);
    }

    @Test
    void testGenerateServerError() {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.groqChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockServer.verify();
        assertThat(thrown).isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    @Test
    void testGenerateServerOverloaded() {
        // Given
        this.mockServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServiceUnavailable());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.groqChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockServer.verify();
        assertThat(thrown).isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }

}