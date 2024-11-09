package com.learningspring.hogwartsartifactonline.client.ai.chat;

import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatRequest;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatResponse;

public interface ChatClient {

    ChatResponse generate(ChatRequest chatRequest);

}
