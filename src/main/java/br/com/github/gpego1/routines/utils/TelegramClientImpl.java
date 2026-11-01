package br.com.github.gpego1.routines.utils;

import br.com.github.gpego1.routines.dto.requests.TelegramMessageRequest;
import br.com.github.gpego1.routines.dto.responses.TelegramMessageResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component

public class TelegramClientImpl implements TelegramClient {
    private final RestClient restClient;

    public TelegramClientImpl (@Qualifier("telegramRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        if (chatId == null || text == null || text.isBlank()) {
            throw new IllegalArgumentException("ChatId and Text are required arguments");
        }
        var request = new TelegramMessageRequest(chatId, text);

        TelegramMessageResponse response = restClient
                .post()
                .uri("sendMessage")
                .body(request)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (httpRequest, httpResponse) -> {
                            throw new IllegalStateException(
                                    "HTTP failure with Telegram "
                                            + httpResponse
                                            .getStatusCode()
                                            .value()
                            );
                        }
                )
                .body(TelegramMessageResponse.class);

        if (response == null || !response.ok()) {
            throw new IllegalStateException("Telegram didnt confirm the message");
        }
    }
}
