package br.com.github.gpego1.routines.config;
import br.com.github.gpego1.routines.dto.responses.TelegramMessageResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.util.List;
import java.util.Map;
@Component
public class TelegramWebhookRegistration implements ApplicationRunner {
    private final TelegramProperties properties;
    private final RestClient client;
    public TelegramWebhookRegistration(TelegramProperties properties, @Qualifier("telegramRestClient") RestClient client) {
        this.properties = properties;
        this.client = client;
    }
    @Override
    public void run(ApplicationArguments args) {
        if (properties.getWebhookUrl().isBlank()) return;
        URI url;
        try { url = URI.create(properties.getWebhookUrl()); }
        catch (IllegalArgumentException invalidUrl) {
            throw new IllegalStateException("TELEGRAM_WEBHOOK_URL deve ser uma URL HTTPS valida");
        }
        if (!"https".equals(url.getScheme()) || url.getHost() == null
                || !"/v1/telegram/webhook".equals(url.getPath()) || url.getQuery() != null)
            throw new IllegalStateException("TELEGRAM_WEBHOOK_URL deve usar HTTPS e terminar em /v1/telegram/webhook, sem query string");
        if (properties.getBotToken().isBlank())
            throw new IllegalStateException("TELEGRAM_BOT_TOKEN deve estar preenchido");
        if (properties.getBotUsername().isBlank())
            throw new IllegalStateException("TELEGRAM_BOT_USERNAME deve estar preenchido, sem @");
        if (!properties.getWebhookSecret().matches("[A-Za-z0-9_-]{1,256}"))
            throw new IllegalStateException("TELEGRAM_WEBHOOK_SECRET deve conter de 1 a 256 caracteres: letras, numeros, _ ou -");
        try {
            var response = client.post().uri("setWebhook").body(Map.of("url", properties.getWebhookUrl(),
                    "secret_token", properties.getWebhookSecret(), "allowed_updates", List.of("message")))
                    .retrieve().body(TelegramMessageResponse.class);
            if (response == null || !response.ok()) throw new IllegalStateException();
        } catch (RuntimeException failure) {
            throw new IllegalStateException("Telegram webhook registration failed; verify bot configuration and connectivity");
        }
    }
}
