package br.com.github.gpego1.routines.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
@Component
@ConfigurationProperties(prefix = "telegram")
@Getter
@Setter
public class TelegramProperties {
    private String botToken = "";
    private String botUsername = "";
    private String webhookSecret = "";
    private String webhookUrl = "";
    private String apiBaseUrl = "https://api.telegram.org";
    private ZoneId defaultTimezone = ZoneId.of("America/Sao_Paulo");
}
