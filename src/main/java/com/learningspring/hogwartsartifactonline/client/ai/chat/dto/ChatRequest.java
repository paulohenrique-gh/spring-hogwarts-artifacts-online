package com.learningspring.hogwartsartifactonline.client.ai.chat.dto;

import java.util.List;

public record ChatRequest(String model, List<Message> messages) {
}
