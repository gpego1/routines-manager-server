package br.com.github.gpego1.routines.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramMessageRequest(
        @JsonProperty("chat_id") Long chatId,
        String text
) {
}
