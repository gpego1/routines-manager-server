package br.com.github.gpego1.routines.dto.requests;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUpdate(@JsonProperty("update_id") Long updateId, Message message) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String text, Chat chat, Sender from) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Chat(Long id, String type) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sender(Long id, @JsonProperty("is_bot") boolean bot) {}
}
